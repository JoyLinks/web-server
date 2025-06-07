package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentEncoding;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.ETag;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Range;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.webdav.elements.ActiveLock;
import com.joyzl.webserver.webdav.elements.Collection;
import com.joyzl.webserver.webdav.elements.LockDiscovery;
import com.joyzl.webserver.webdav.elements.LockInfo;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Property;
import com.joyzl.webserver.webdav.elements.PropertyUpdate;
import com.joyzl.webserver.webdav.elements.Propfind;
import com.joyzl.webserver.webdav.elements.Propstat;

public class FileWEBDAVServlet extends WEBDAVServlet {

	private final static int XML = 1, JSON = 2;
	private final LinkOption[] options = new LinkOption[] { LinkOption.NOFOLLOW_LINKS };

	/** 资源锁 */
	private final Locks LOCKS = new Locks();
	/** 允许所有支持的属性 */
	private final boolean allProperty;
	/** 基础URI */
	private final String base;
	/** 根目录 */
	private final Path root;

	public FileWEBDAVServlet(String base, String root) {
		this(base, root, false);
	}

	public FileWEBDAVServlet(String base, String root, boolean all) {
		this.base = Utility.correctBase(base);
		this.root = Path.of(root);
		this.allProperty = all;
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		locate(request, response, true);
	}

	@Override
	protected void head(Request request, Response response) throws Exception {
		locate(request, response, false);
	}

	/** locate -> response */
	private void locate(Request request, Response response, boolean content) throws IOException {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (Files.exists(path, options)) {
			if (Files.isDirectory(path, options)) {
				response.setStatus(HTTPStatus.NO_CONTENT);
			} else {
				final FileTime time = Files.getLastModifiedTime(path, options);
				final String etag = ETag.makeWeak(Files.size(path), time.toMillis());
				final String modified = Date.toText(time.toMillis());

				response.addHeader(ContentType.NAME, contentType(path));
				response.addHeader(CacheControl.NAME, CacheControl.NO_CACHE);
				response.addHeader(HTTP1.Content_Location, Utility.resolvePath(root, base, path));
				response.addHeader(HTTP1.Last_Modified, modified);
				response.addHeader(ETag.NAME, etag);

				// ETAG不同则返回资源 RFC7232
				String value = request.getHeader(HTTP1.If_None_Match);
				if (Utility.noEmpty(value)) {
					if (Utility.equal(value, etag)) {
						response.setStatus(HTTPStatus.NOT_MODIFIED);
					} else {
						response(response, path, content);
					}
					return;
				}

				// ETAG相同则返回资源 RFC7232
				value = request.getHeader(HTTP1.If_Match);
				if (Utility.noEmpty(value)) {
					if (Utility.equal(value, etag)) {
						response(response, path, content);
					} else {
						response.setStatus(HTTPStatus.PRECONDITION_FAILED);
					}
					return;
				}

				// 修改时间有更新返回文件内容
				value = request.getHeader(HTTP1.If_Modified_Since);
				if (Utility.noEmpty(value)) {
					if (Utility.equal(value, modified)) {
						response.setStatus(HTTPStatus.NOT_MODIFIED);
					} else {
						response(response, path, content);
					}
					return;
				}

				// 修改时间未更新返回文件内容
				value = request.getHeader(HTTP1.If_Unmodified_Since);
				if (Utility.noEmpty(value)) {
					if (Utility.equal(value, modified)) {
						response(response, path, content);
					} else {
						response.setStatus(HTTPStatus.PRECONDITION_FAILED);
					}
					return;
				}

				response(response, path, content);
			}
		} else {
			response.setStatus(HTTPStatus.NOT_FOUND);
		}
	}

	/** locate -> response */
	private void response(Response response, Path path, boolean content) throws IOException {
		final long length = Files.size(path);

		// Content-Encoding: identity
		response.addHeader(ContentEncoding.NAME, ContentEncoding.IDENTITY);

		if (length < HTTP1Coder.BLOCK_BYTES) {
			// Content-Length:9
			response.addHeader(ContentLength.NAME, Long.toString(length));
		} else {
			// Transfer-Encoding: chunked
			response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
			// Accept-Ranges: bytes
			response.addHeader(HTTP1.Accept_Ranges, Range.UNIT);
		}
		if (content) {
			response.setContent(Files.newInputStream(path, options));
		}
	}

	@Override
	protected void put(Request request, Response response) throws Exception {
		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (root.equals(path)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		final If IF = If.parse(request.getHeader(If.NAME));
		if (IF != null) {
			if (ifLock(IF, path, null)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			if (LOCKS.lock(path)) {
				response.setStatus(HTTPStatus.LOCKED);
				return;
			}
		}

		if (Files.exists(path, options)) {
			if (Files.isDirectory(path, options)) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
				return;
			} else {
				// response.setStatus(HTTPStatus.CREATED);
				try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
					if (request.hasContent()) {
						final DataBuffer buffer = (DataBuffer) request.getContent();
						buffer.transfer(channel);
					} else {
						channel.truncate(0);
					}
				} catch (FileAlreadyExistsException e) {
					response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
				} catch (AccessDeniedException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
				} catch (IOException e) {
					e.printStackTrace();
					response.setStatus(HTTPStatus.CONFLICT);
				}
			}
		} else {
			ByteChannel channel = null;
			response.setStatus(HTTPStatus.CREATED);
			try {
				Files.createFile(path);
				if (request.hasContent()) {
					channel = Files.newByteChannel(path, StandardOpenOption.WRITE);
					if (channel.isOpen()) {
						final DataBuffer buffer = (DataBuffer) request.getContent();
						buffer.transfer(channel);
					}
				}
			} catch (FileAlreadyExistsException e) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
			} catch (AccessDeniedException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				e.printStackTrace();
				response.setStatus(HTTPStatus.CONFLICT);
			} finally {
				if (channel != null) {
					channel.close();
				}
			}
		}
	}

	@Override
	protected void propfind(Request request, Response response) throws Exception {
		final int type = checkType(request);
		final Propfind propfind;
		if (request.hasContent()) {
			try {
				if (type == XML) {
					propfind = XMLCoder.read(Propfind.class, request);
				} else if (type == JSON) {
					propfind = JSONCoder.read(Propfind.class, request);
				} else {
					// 未指定请求接口类型JSON/XML
					// 请求和响应均需要此格式
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
			} catch (Exception e) {
				// 格式错误
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			propfind = null;
		}

		if (propfind != null) {
			if (propfind.isAllprop()) {
				if (propfind.isPropname()) {
					response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
					return;
				}
				if (propfind.hasProp()) {
					response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
					return;
				}
			}
		}

		final int depth = Depth.get(request);
		if (depth < 0) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		final String host = request.getHeader(HTTP1.Host);
		final Multistatus multistatus = new Multistatus(request.getVersion());
		Path path = Utility.resolveFile(root, base, request.getPath());
		com.joyzl.webserver.webdav.elements.Response r;

		if (propfind == null) {
			// 返回死属性和定义的活属性

			r = response(multistatus, path, host);
			defaultAttributes(r, path, null);
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								defaultAttributes(r, path, null);
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								defaultAttributes(r, path, null);
							}
						}
					}
				}
			}
		} else if (propfind.isAllprop()) {
			// 返回死属性和定义的活属性 (include)

			r = response(multistatus, path, host);
			defaultAttributes(r, path, propfind.getInclude());
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								defaultAttributes(r, path, propfind.getInclude());
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								defaultAttributes(r, path, propfind.getInclude());
							}
						}
					}
				}
			}
		} else if (propfind.isPropname()) {
			// 获取所有属性名称(不含属性值)

			r = response(multistatus, path, host);
			attributeNames(r, path);
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								attributeNames(r, path);
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								attributeNames(r, path);
							}
						}
					}
				}
			}
		} else if (propfind.hasProp()) {
			// 获取指定属性值

			r = response(multistatus, path, host);
			attributes(r, path, propfind.getProp());
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								attributes(r, path, propfind.getProp());
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus, path, host);
								attributes(r, path, propfind.getProp());
							}
						}
					}
				}
			}
		} else {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		response.setStatus(HTTPStatus.MULTI_STATUS);
		if (type == XML) {
			XMLCoder.write(multistatus, response);
		} else if (type == JSON) {
			JSONCoder.write(multistatus, response);
		}
	}

	@Override
	protected void proppatch(Request request, Response response) throws Exception {
		final int type = checkType(request);
		final PropertyUpdate update;
		if (request.hasContent()) {
			try {
				if (type == XML) {
					update = XMLCoder.read(PropertyUpdate.class, request);
				} else if (type == JSON) {
					update = JSONCoder.read(PropertyUpdate.class, request);
				} else {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (root.equals(path)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		final If IF = If.parse(request.getHeader(If.NAME));
		if (IF != null) {
			if (ifLock(IF, path, null)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			if (LOCKS.lock(path)) {
				response.setStatus(HTTPStatus.LOCKED);
				return;
			}
		}

		if (update != null) {
			if (Files.exists(path, options)) {
				final UserDefinedFileAttributeView user = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class, options);
				if (user == null) {
					response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
					return;
				}

				final String host = request.getHeader(HTTP1.Host);
				final Multistatus multistatus = new Multistatus(request.getVersion());
				com.joyzl.webserver.webdav.elements.Response r = response(multistatus, path, host);

				if (update.hasProp()) {
					Propstat propstat1 = new Propstat(r.version());
					Propstat propstat2 = new Propstat(r.version());

					for (Property property : update.prop()) {
						if (WEBDAV.PROPERTIES.contains(property.getName())) {
							propstat2.prop().add(property);
						} else {
							propstat1.prop().add(property);
						}
						if (propstat2.hasProp()) {
							property.setValue(null);
						}
					}

					if (propstat2.hasProp()) {
						propstat1.setStatus(HTTPStatus.FAILED_DEPENDENCY);
						propstat2.setStatus(HTTPStatus.FORBIDDEN);
						r.getPropstats().add(propstat2);
						r.getPropstats().add(propstat1);
					} else {
						propstat1.prop().clear();
						for (Property property : update.prop()) {
							if (property.setting()) {
								if (property.getValue() == null) {
									propstat1.prop().add(property);
									continue;
								}
								try {
									user.write(property.getName(), convertByteBuffer(property.getValue()));
									propstat1.prop().add(property);
								} catch (SecurityException e) {
									propstat2.setStatus(HTTPStatus.FORBIDDEN);
									propstat2.prop().add(property);
								} catch (NoSuchFileException e) {
									// 如果文件确定存在则为属性不存在
								} catch (IOException e) {
									propstat2.setStatus(HTTPStatus.CONFLICT);
									propstat2.prop().add(property);
								} finally {
									property.setValue(null);
								}
								continue;
							}
							if (property.removing()) {
								try {
									user.delete(property.getName());
									propstat1.prop().add(property);
								} catch (SecurityException e) {
									propstat2.setStatus(HTTPStatus.FORBIDDEN);
									propstat2.prop().add(property);
								} catch (NoSuchFileException e) {
									// 如果文件确定存在则为属性不存在
									propstat1.prop().add(property);
								} catch (IOException e) {
									propstat2.setStatus(HTTPStatus.CONFLICT);
									propstat2.prop().add(property);
								} finally {
									property.setValue(null);
								}
							}
						}
						if (propstat1.hasProp()) {
							r.getPropstats().add(propstat1);
						}
						if (propstat2.hasProp()) {
							r.getPropstats().add(propstat2);
						}
					}
				}

				response.setStatus(HTTPStatus.MULTI_STATUS);
				if (type == XML) {
					XMLCoder.write(multistatus, response);
				} else if (type == JSON) {
					JSONCoder.write(multistatus, response);
				}
			} else {
				response.setStatus(HTTPStatus.NOT_FOUND);
			}
		}
	}

	@Override
	protected void mkcol(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (root.equals(path)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}

		response.setStatus(HTTPStatus.CREATED);
		try {
			Files.createDirectory(path);
		} catch (FileAlreadyExistsException e) {
			response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
		} catch (SecurityException e) {
			response.setStatus(HTTPStatus.FORBIDDEN);
		} catch (IOException e) {
			response.setStatus(HTTPStatus.CONFLICT);
		}
	}

	@Override
	protected void delete(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (root.equals(path)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Files.notExists(path, options)) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}
		final If IF = If.parse(request.getHeader(If.NAME));
		if (IF != null) {
			if (ifLock(IF, path, null)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			if (LOCKS.lock(path)) {
				response.setStatus(HTTPStatus.LOCKED);
				return;
			}
		}

		response.setStatus(HTTPStatus.NO_CONTENT);
		if (Files.isDirectory(path, options)) {
			final String depth = request.getHeader(Depth.NAME);
			if (depth != null) {
				if (!Depth.INFINITY.equalsIgnoreCase(depth)) {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
			}

			final String host = request.getHeader(HTTP1.Host);
			final Multistatus multistatus = new Multistatus(request.getVersion());
			try {
				Files.walkFileTree(path, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						try {
							Files.delete(file);
						} catch (IOException e) {
							response(multistatus, file, host, e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
						if (e == null) {
							try {
								Files.delete(dir);
							} catch (IOException ex) {
								response(multistatus, dir, host, ex);
							}
						} else {
							response(multistatus, dir, host, e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException e) {
						response(multistatus, file, host, e);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (FileAlreadyExistsException e) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			}

			if (multistatus.getResponses().size() > 0) {
				response.setStatus(HTTPStatus.MULTI_STATUS);
				final int type = checkType(request);
				if (type == XML) {
					XMLCoder.write(multistatus, response);
				} else if (type == JSON) {
					JSONCoder.write(multistatus, response);
				}
			}
		} else {
			try {
				Files.delete(path);
			} catch (NoSuchFileException e) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
			} catch (AccessDeniedException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			}
		}
	}

	@Override
	protected void copy(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		// Destination:url
		final Destination destination = Destination.get(request);
		if (destination == null) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}
		if (!destination.pathStart(base)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}

		final Path source = Utility.resolveFile(root, base, request.getPath());
		final Path target = Utility.resolveFile(root, base, Utility.normalizePath(destination.getPath()));
		if (root.equals(source)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (root.equals(target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (source.equals(target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Files.notExists(source, options)) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}
		final If IF = If.parse(request.getHeader(If.NAME));
		if (IF != null) {
			if (ifLock(IF, null, target)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			if (LOCKS.lock(target)) {
				response.setStatus(HTTPStatus.LOCKED);
				return;
			}
		}

		// Overwrite:T|F
		final boolean overwrite = Overwrite.get(request);
		if (Files.exists(target, options)) {
			if (overwrite) {
				try {
					// 如果存在则应删除，规范要求不能执行目录合并
					if (Files.isDirectory(target, options)) {
						Files.walkFileTree(target, new SimpleFileVisitor<>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
								if (e != null) {
									throw e;
								}
								Files.delete(dir);
								return FileVisitResult.CONTINUE;
							}
						});
					} else {
						Files.delete(target);
					}
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
					return;
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
					return;
				}
				// 预计后续的成功状态
				response.setStatus(HTTPStatus.NO_CONTENT);
			} else {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			// 预计后续的成功状态
			response.setStatus(HTTPStatus.CREATED);
		}

		// Depth:0|infinity
		final int depth = Depth.get(request);
		if (depth == 0) {
			try {
				Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
			} catch (FileAlreadyExistsException e) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			}
		} else if (depth > 1) {
			if (Files.isDirectory(source, options)) {
				final String host = request.getHeader(HTTP1.Host);
				final Multistatus multistatus = new Multistatus(request.getVersion());
				try {
					Files.walkFileTree(source, new SimpleFileVisitor<>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							try {
								Files.copy(dir, target.resolve(source.relativize(dir)), StandardCopyOption.COPY_ATTRIBUTES);
							} catch (IOException e) {
								response(multistatus, dir, host, e);
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							try {
								Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
							} catch (IOException e) {
								response(multistatus, file, host, e);
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException e) {
							response(multistatus, file, host, e);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
				}

				if (multistatus.getResponses().size() > 0) {
					response.setStatus(HTTPStatus.MULTI_STATUS);
					final int type = checkType(request);
					if (type == XML) {
						XMLCoder.write(multistatus, response);
					} else if (type == JSON) {
						JSONCoder.write(multistatus, response);
					}
				}
			} else {
				try {
					Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
				} catch (FileAlreadyExistsException e) {
					response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
				}
			}
		} else {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}
	}

	@Override
	protected void move(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		// Depth:infinity
		final String depth = request.getHeader(Depth.NAME);
		if (depth != null) {
			if (!Depth.INFINITY.equalsIgnoreCase(depth)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		}

		// Destination:url
		final Destination destination = Destination.get(request);
		if (destination == null) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}
		if (!destination.pathStart(base)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}

		final Path source = Utility.resolveFile(root, base, request.getPath());
		final Path target = Utility.resolveFile(root, base, Utility.normalizePath(destination.getPath()));
		if (root.equals(source)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (root.equals(target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (source.equals(target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Files.notExists(source, options)) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}
		final If IF = If.parse(request.getHeader(If.NAME));
		if (IF != null) {
			if (ifLock(IF, source, target)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			if (LOCKS.lock(source) || LOCKS.lock(target)) {
				response.setStatus(HTTPStatus.LOCKED);
				return;
			}
		}

		// Overwrite:T|F
		final boolean overwrite = Overwrite.get(request);
		if (Files.exists(target, options)) {
			if (overwrite) {
				// DELETE Depth:infinity
				try {
					if (Files.isDirectory(target, options)) {
						Files.walkFileTree(target, new SimpleFileVisitor<>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
								if (e != null) {
									throw e;
								}
								Files.delete(dir);
								return FileVisitResult.CONTINUE;
							}
						});
					} else {
						Files.delete(target);
					}
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
					return;
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
					return;
				}
				// 预计后续的成功状态
				response.setStatus(HTTPStatus.NO_CONTENT);
			} else {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}
		} else {
			// 预计后续的成功状态
			response.setStatus(HTTPStatus.CREATED);
		}

		if (Files.isDirectory(source, options)) {
			final String host = request.getHeader(HTTP1.Host);
			final Multistatus multistatus = new Multistatus(request.getVersion());
			try {
				Files.walkFileTree(source, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						try {
							// 测试Windows将目录中文件一并移动了
							// Files.move(dir,target.resolve(source.relativize(dir)));
							Files.createDirectory(target.resolve(source.relativize(dir)));
						} catch (IOException e) {
							response(multistatus, dir, host, e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						try {
							Files.move(file, target.resolve(source.relativize(file)));
						} catch (IOException e) {
							response(multistatus, file, host, e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException e) {
						response(multistatus, file, host, e);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			}

			if (multistatus.getResponses().size() > 0) {
				response.setStatus(HTTPStatus.MULTI_STATUS);
				final int type = checkType(request);
				if (type == XML) {
					XMLCoder.write(multistatus, response);
				} else if (type == JSON) {
					JSONCoder.write(multistatus, response);
				}
			}
		} else {
			try {
				Files.move(source, target);
			} catch (FileAlreadyExistsException e) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			}
		}
	}

	@Override
	protected void lock(Request request, Response response) throws Exception {
		final int type = checkType(request);
		final Path path = Utility.resolveFile(root, base, request.getPath());
		if (root.equals(path)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}

		if (request.hasContent()) {
			// 请求创建锁

			// Depth: 0|infinity
			final int depth = Depth.get(request);
			if (depth < 0 || depth > 0 && depth != Integer.MAX_VALUE) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}

			final LockInfo lockInfo;
			try {
				if (type == XML) {
					lockInfo = XMLCoder.read(LockInfo.class, request);
				} else if (type == JSON) {
					lockInfo = JSONCoder.read(LockInfo.class, request);
				} else {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
			} catch (IOException e) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}

			if (lockInfo.getLockScope() == null || lockInfo.getLockType() == null || Utility.isEmpty(lockInfo.getOwner())) {
				response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
				return;
			}

			if (Files.exists(path, options)) {
				//
			} else {
				try {
					Files.createFile(path);
					response.setStatus(HTTPStatus.CREATED);
				} catch (FileAlreadyExistsException e) {
					response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
					return;
				} catch (AccessDeniedException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
					return;
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
					return;
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
					return;
				}
			}

			final String host = request.getHeader(HTTP1.Host);
			final LockDiscovery lockdiscovery = new LockDiscovery();
			final ActiveLock activeLock = new ActiveLock(lockInfo);
			activeLock.setLockToken("urn:uuid:" + UUID.randomUUID());
			activeLock.setLockRoot("http://" + host + request.getPath());
			activeLock.setDepth(depth);

			// Timeout:
			final Timeout timeout = Timeout.parse(request.getHeader(Timeout.NAME));
			if (timeout == null) {
				// 默认1天
				activeLock.setTimeout(60 * 60 * 24);
			} else {
				long second = timeout.getSecond();
				if (second > 0) {
					activeLock.setTimeout(second);
				} else if (timeout.isInfinite()) {
					activeLock.setTimeout(Long.MAX_VALUE);
				} else {
					activeLock.setTimeout(60 * 60 * 24);
				}
			}

			if (LOCKS.add(path, activeLock)) {
				lockdiscovery.actives().add(activeLock);
				response.addHeader(HTTP1.Lock_Token, activeLock.getLockToken());
			} else {
				response.setStatus(HTTPStatus.LOCKED);
			}
			if (type == XML) {
				XMLCoder.write(lockdiscovery, response);
			} else if (type == JSON) {
				JSONCoder.write(lockdiscovery, response);
			}
		} else {
			// 请求刷新锁

			// If:仅单个锁(Token)
			final If IF = If.parse(request.getHeader(If.NAME));
			if (IF == null) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}

			// 查找锁
			ActiveLock activeLock = null;
			while (IF.nextGroup()) {
				while (IF.nextValue()) {
					if (IF.isToken()) {
						if (!IF.isNot()) {
							activeLock = LOCKS.get(IF.getValue());
							break;
						}
					}
				}
			}
			if (activeLock == null) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				return;
			}

			// Timeout:
			final Timeout timeout = Timeout.parse(request.getHeader(Timeout.NAME));
			if (timeout == null) {
				// 默认延长1天
				activeLock.setTimeout(60 * 60 * 24);
			} else {
				long second = timeout.getSecond();
				if (second > 0) {
					activeLock.setTimeout(second);
				} else if (timeout.isInfinite()) {
					activeLock.setTimeout(Long.MAX_VALUE);
				} else {
					activeLock.setTimeout(60 * 60 * 24);
				}
			}

			final LockDiscovery lockdiscovery = new LockDiscovery();
			lockdiscovery.actives().add(activeLock);
			response.addHeader(HTTP1.Lock_Token, activeLock.getLockToken());
			if (type == XML) {
				XMLCoder.write(lockdiscovery, response);
			} else if (type == JSON) {
				JSONCoder.write(lockdiscovery, response);
			}
		}
	}

	@Override
	protected void unlock(Request request, Response response) throws Exception {
		// Lock-Token:
		String token = request.getHeader(HTTP1.Lock_Token);
		if (token == null) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		} else {
			// "<token>" -> "token"
			int begin = token.indexOf('<');
			int end = token.lastIndexOf('>');
			if (begin >= 0 && end > begin) {
				token = token.substring(begin + 1, end);
			}
		}
		// If:

		final ActiveLock activeLock = LOCKS.remove(token);
		if (activeLock == null) {
			response.setStatus(HTTPStatus.CONFLICT);
		} else {
			response.setStatus(HTTPStatus.NO_CONTENT);
		}
	}

	/** 评估IF和资源锁定，返回true表示资源因锁定而失败 */
	private boolean ifLock(If IF, Path path1, Path path2) {
		// 注意：锁定资源的token出现在If头中，并且If评估为有效，因此不能仅考察评估结果
		// COPY：需求解锁目标资源，源资源未被修改
		// MOVE：需求解锁目标资源和源资源
		// 评估资源锁定无须判断锁是否过期

		final ActiveLock[] locks1 = path1 != null ? LOCKS.get(path1) : null;
		final ActiveLock[] locks2 = path2 != null ? LOCKS.get(path2) : null;

		Path path;
		int k, m = 0;
		ActiveLock[] locks;
		String resource, etag, token;
		boolean or = false, and;

		while (IF.nextGroup()) {
			resource = IF.getTag();
			if (resource == null) {
				path = path1;
				locks = locks1;
			} else {
				path = Utility.resolveFile(root, base, resource);
				if (path1 != null && path1.startsWith(path)) {
					locks = locks1;
				} else if (path2 != null && path2.startsWith(path)) {
					locks = locks2;
				} else {
					locks = locks2;
				}
			}

			and = true;
			etag = null;
			while (IF.nextValue()) {
				if (IF.isToken()) {
					if (locks != null) {
						token = IF.getValue();
						for (k = 0; k < locks.length; k++) {
							if (token.equals(locks[k].getLockToken())) {
								m++;
								break;
							}
						}
						if (k < locks.length) {
							if (IF.isNot()) {
								and = false;
							}
						} else {
							if (!IF.isNot()) {
								and = false;
							}
						}
					} else {
						if (!IF.isNot()) {
							and = false;
						}
					}
				} else if (IF.isETag()) {
					try {
						if (etag == null) {
							etag = ETag.makeWeak(Files.size(path), Files.getLastModifiedTime(path, options).toMillis());
						}
						if (etag.equals(IF.getValue())) {
							if (IF.isNot()) {
								and = false;
							}
						} else {
							if (!IF.isNot()) {
								and = false;
							}
						}
					} catch (IOException e) {
						if (!IF.isNot()) {
							and = false;
						}
					}
				}
			}
			or = or || and;
		}

		if (or) {
			if (locks1 != null && locks2 != null) {
				return m < 2;
			}
			if (locks1 != null || locks2 != null) {
				return m < 1;
			}
			return false;
		} else {
			return true;
		}
	}

	private com.joyzl.webserver.webdav.elements.Response response(Multistatus multistatus, Path path, String host) {
		return response(multistatus, path, host, null);
	}

	private com.joyzl.webserver.webdav.elements.Response response(Multistatus multistatus, Path path, String host, Exception e) {
		final com.joyzl.webserver.webdav.elements.Response response = new com.joyzl.webserver.webdav.elements.Response();
		response.version(multistatus.version());
		response.setHref("http://" + host + Utility.resolvePath(root, base, path));

		if (e != null) {
			e.printStackTrace();
			response.setError(e.getMessage());
		}

		multistatus.getResponses().add(response);
		return response;
	}

	/*-
	 * 系统支持的活属性
	 * BASIC:lastModifiedTime,lastAccessTime,creationTime,size,isRegularFile,isDirectory,isSymbolicLink,isOther,fileKey
	 * POSIX:owner,permissions,group
	 * DOS:readonly,hidden,system,archive
	 * ACL:acl,owner
	 * 注意：
	 * Files.getFileAttributeView(path, BasicFileAttributeView.class) 始终返回实例，即便路径不存在；
	 * PosixFileAttributeView/DosFileAttributeView 视操作系统始终返回其中一个实例，即便路径不存在；
	 * AclFileAttributeView 始终返回实例，即便路径不存在；
	 * 用户自定义属性
	 * USER:UserDefinedFileAttributeView
	 */

	/** 获取指定路径的指定属性 */
	private void attributes(com.joyzl.webserver.webdav.elements.Response response, Path path, Set<String> names) throws IOException {
		if (names == null || names.isEmpty()) {
			return;
		}

		final BasicFileAttributeView basic = Files.getFileAttributeView(path, BasicFileAttributeView.class, options);
		final BasicFileAttributes attributes;
		try {
			attributes = basic.readAttributes();
		} catch (NoSuchFileException e) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		} catch (SecurityException e) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		} catch (IOException e) {
			response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
			return;
		}

		final Propstat propstat1 = new Propstat(response.version());
		if (names.contains(WEBDAV.DISPLAY_NAME)) {
			propstat1.prop().add(new Property(WEBDAV.DISPLAY_NAME, path.getFileName()));
		}
		if (names.contains(WEBDAV.CREATION_DATE)) {
			propstat1.prop().add(new Property(WEBDAV.CREATION_DATE, Date.toText(attributes.creationTime().toMillis())));
		}
		if (attributes.isDirectory()) {
			if (names.contains(WEBDAV.RESOURCE_TYPE)) {
				propstat1.prop().add(new Property(WEBDAV.RESOURCE_TYPE, Collection.INSTANCE));
			}
		} else {
			if (names.contains(WEBDAV.GET_CONTENT_LANGUAGE)) {
				propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_LANGUAGE));
			}
			if (names.contains(WEBDAV.GET_LAST_MODIFIED)) {
				propstat1.prop().add(new Property(WEBDAV.GET_LAST_MODIFIED, Date.toText(attributes.lastModifiedTime().toMillis())));
			}
			if (names.contains(WEBDAV.GET_CONTENT_LENGTH)) {
				propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_LENGTH, attributes.size()));
			}
			if (names.contains(WEBDAV.GET_CONTENT_TYPE)) {
				propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_TYPE, contentType(path)));
			}
			if (names.contains(WEBDAV.GET_ETAG)) {
				propstat1.prop().add(new Property(WEBDAV.GET_ETAG, ETag.makeWeak(attributes.size(), attributes.lastModifiedTime().toMillis())));
			}
		}
		response.getPropstats().add(propstat1);

		if (names.contains(WEBDAV.SUPPORTED_LOCK)) {
			propstat1.prop().add(new Property(WEBDAV.SUPPORTED_LOCK));
		}
		if (names.contains(WEBDAV.LOCK_DISCOVERY)) {
			final LockDiscovery lockDiscovery = new LockDiscovery();
			lockDiscovery.setActives(LOCKS.get(path));
			propstat1.prop().add(lockDiscovery);
		}

		if (!propstat1.hasProp() || propstat1.prop().size() < names.size()) {
			final UserDefinedFileAttributeView user = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class, options);
			if (user != null) {
				try {
					final List<String> ns = user.list();
					if (ns != null && ns.size() > 0) {
						String name;
						ByteBuffer buffer = ByteBuffer.allocate(512);
						for (int index = 0; index < ns.size(); index++) {
							name = ns.get(index);
							if (names.contains(name)) {
								if (user.read(name, buffer.clear()) > 0) {
									propstat1.prop().add(new Property(name, convertCharBuffer(buffer.flip())));
								} else {
									propstat1.prop().add(new Property(name));
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (propstat1.hasProp()) {
			if (propstat1.prop().size() < names.size()) {
				// 部分属性未找到
				final Propstat propstat2 = new Propstat(response.version());
				propstat2.setStatus(HTTPStatus.NOT_FOUND);
				response.getPropstats().add(propstat2);
				for (String name : names) {
					for (Property property : propstat1.prop()) {
						if (name.equals(property.getName())) {
							continue;
						}
					}
					propstat2.prop().add(new Property(name));
				}
			}
		} else {
			// 全部属性未找到
			propstat1.setStatus(HTTPStatus.NOT_FOUND);
			for (String name : names) {
				propstat1.prop().add(new Property(name));
			}
		}
	}

	/** 获取指定路径的默认属性 */
	private void defaultAttributes(com.joyzl.webserver.webdav.elements.Response response, Path path, Set<String> include) {
		final BasicFileAttributeView basic = Files.getFileAttributeView(path, BasicFileAttributeView.class, options);
		final BasicFileAttributes attributes;
		try {
			attributes = basic.readAttributes();
		} catch (NoSuchFileException e) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		} catch (SecurityException e) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		} catch (IOException e) {
			response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
			return;
		}

		final Propstat propstat1 = new Propstat(response.version());
		propstat1.prop().add(new Property(WEBDAV.DISPLAY_NAME, path.getFileName()));
		propstat1.prop().add(new Property(WEBDAV.CREATION_DATE, Date.toText(attributes.creationTime().toMillis())));
		if (attributes.isDirectory()) {
			response.dir(true);
			propstat1.prop().add(new Property(WEBDAV.RESOURCE_TYPE, Collection.INSTANCE));
		} else {
			response.dir(false);
			propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_LANGUAGE));
			propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_LENGTH, attributes.size()));
			propstat1.prop().add(new Property(WEBDAV.GET_CONTENT_TYPE, contentType(path)));
			propstat1.prop().add(new Property(WEBDAV.GET_ETAG, ETag.makeWeak(attributes.size(), attributes.lastModifiedTime().toMillis())));
			propstat1.prop().add(new Property(WEBDAV.GET_LAST_MODIFIED, Date.toText(attributes.lastModifiedTime().toMillis())));
		}

		propstat1.prop().add(new Property(WEBDAV.SUPPORTED_LOCK));
		final LockDiscovery lockDiscovery = new LockDiscovery();
		lockDiscovery.setActives(LOCKS.get(path));
		propstat1.prop().add(lockDiscovery);

		response.getPropstats().add(propstat1);

		final UserDefinedFileAttributeView user = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class, options);
		if (user != null) {
			try {
				final List<String> names = user.list();
				if (names != null && names.size() > 0) {
					String name;
					ByteBuffer buffer = ByteBuffer.allocate(512);
					for (int index = 0; index < names.size(); index++) {
						name = names.get(index);
						if (user.read(name, buffer.clear()) > 0) {
							propstat1.prop().add(new Property(name, convertCharBuffer(buffer.flip())));
						} else {
							propstat1.prop().add(new Property(name));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (include != null && include.size() > 0) {
			// TODO
			Propstat propstat2 = new Propstat(response.version());
			propstat2.setStatus(HTTPStatus.NOT_FOUND);
			response.getPropstats().add(propstat2);
			for (String name : include) {
				propstat2.prop().add(new Property(name));
			}
		}
	}

	/** 获取指定路径的属性名称 */
	private void attributeNames(com.joyzl.webserver.webdav.elements.Response response, Path path) {
		final BasicFileAttributeView basic = Files.getFileAttributeView(path, BasicFileAttributeView.class, options);
		final BasicFileAttributes attributes;
		try {
			attributes = basic.readAttributes();
		} catch (NoSuchFileException e) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		} catch (SecurityException e) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		} catch (IOException e) {
			response.setStatus(HTTPStatus.CONFLICT);
			return;
		}

		final Propstat propstat = new Propstat(response.version());
		propstat.prop().add(new Property(WEBDAV.DISPLAY_NAME));
		propstat.prop().add(new Property(WEBDAV.CREATION_DATE));
		propstat.prop().add(new Property(WEBDAV.RESOURCE_TYPE));
		if (attributes.isDirectory()) {
			response.dir(true);
		} else {
			response.dir(false);
			propstat.prop().add(new Property(WEBDAV.GET_CONTENT_LANGUAGE));
			propstat.prop().add(new Property(WEBDAV.GET_CONTENT_LENGTH));
			propstat.prop().add(new Property(WEBDAV.GET_CONTENT_TYPE));
			propstat.prop().add(new Property(WEBDAV.GET_ETAG));
			propstat.prop().add(new Property(WEBDAV.GET_LAST_MODIFIED));
			propstat.prop().add(new Property(WEBDAV.RESOURCE_TYPE));
		}
		propstat.prop().add(new Property(WEBDAV.LOCK_DISCOVERY));
		propstat.prop().add(new Property(WEBDAV.SUPPORTED_LOCK));
		response.getPropstats().add(propstat);

		final UserDefinedFileAttributeView user = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class, options);
		if (user != null) {
			try {
				final List<String> names = user.list();
				if (names != null && names.size() > 0) {
					for (int index = 0; index < names.size(); index++) {
						propstat.prop().add(new Property(names.get(index)));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (allProperty) {
			propstat.prop().add(new Property(WEBDAV.LAST_ACCESS_TIME));
			final PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			if (posix != null) {
				propstat.prop().add(new Property(WEBDAV.OWNER));
				propstat.prop().add(new Property(WEBDAV.GROUP));
				propstat.prop().add(new Property(WEBDAV.PERMISSIONS));
			}
			final DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
			if (dos != null) {
				propstat.prop().add(new Property(WEBDAV.ARCHIVE));
				propstat.prop().add(new Property(WEBDAV.READONLY));
				propstat.prop().add(new Property(WEBDAV.HIDDEN));
				propstat.prop().add(new Property(WEBDAV.SYSTEM));
			}
			final AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
			if (acl != null) {
				propstat.prop().add(new Property(WEBDAV.OWNER));
				propstat.prop().add(new Property(WEBDAV.ACL));
			}
		}
	}

	/** 获取指定路径(文件)的ContentType:MIME */
	private String contentType(Path path) {
		try {
			final String type = Files.probeContentType(path);
			if (type == null) {
				return MIMEType.APPLICATION_OCTET_STREAM;
			}
			return type;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 检查并返回指定头信息标识的实体格式：XML/JSON<br>
	 * 如何未指定请求接口类型默认为XML，请求和响应均需要此格式。
	 */
	private int checkType(Request request) {
		final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
		if (contentType != null) {
			if (Utility.same(MIMEType.APPLICATION_JSON, contentType.getType())) {
				return JSON;
			} else //
			if (Utility.same(MIMEType.APPLICATION_XML, contentType.getType())) {
				return XML;
			} else //
			if (Utility.same(MIMEType.TEXT_XML, contentType.getType())) {
				return XML;
			}
		}
		// DEFAULT
		return XML;
	}

	private ByteBuffer convertByteBuffer(Object value) {
		return StandardCharsets.UTF_8.encode(value.toString());
	}

	private CharBuffer convertCharBuffer(ByteBuffer value) {
		return StandardCharsets.UTF_8.decode(value);
	}

	public boolean isAllProperty() {
		return allProperty;
	}

	public String getPath() {
		return base;
	}

	public Path getRoot() {
		return root;
	}
}
package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.ETag;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.MIMEType;
import com.joyzl.webserver.web.FileResourceServlet;
import com.joyzl.webserver.webdav.elements.Collection;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Propfind;
import com.joyzl.webserver.webdav.elements.Propstat;

public class FileWEBDAVServlet extends WEBDAVServlet {

	private final static int XML = 1, JSON = 2;
	private final LinkOption[] options = new LinkOption[] { LinkOption.NOFOLLOW_LINKS };

	/** 基础URI */
	private final String base;
	/** 根目录 */
	private final Path root;

	public FileWEBDAVServlet(String base, String root) {
		this.root = Path.of(root);
		this.base = base;
	}

	private int check(Request request) {
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
		return 0;
	}

	@Override
	protected void propfind(Request request, Response response) throws Exception {
		final int type = check(request);

		final Propfind propfind;
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

		if (propfind != null) {
			if (propfind.isAllprop()) {
				if (propfind.isPropname()) {
					response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
					return;
				}
				if (propfind.getProp().size() > 0) {
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

		final Multistatus multistatus = new Multistatus();
		Path path = Path.of(root.toString(), request.getPath());
		com.joyzl.webserver.webdav.elements.Response r;
		if (propfind == null || propfind.isAllprop()) {
			// 返回死属性和定义的活属性 (include)

			r = response(multistatus);
			defaultAttributes(r, path, propfind.getInclude());
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								defaultAttributes(r, path, propfind.getInclude());
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								defaultAttributes(r, path, propfind.getInclude());
							}
						}
					}
				}
			}
		} else if (propfind.isPropname()) {
			// 获取所有属性名称(不含属性值)

			r = response(multistatus);
			attributeNames(r, path);
			hrefLocation(r, path);
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								attributeNames(r, path);
								hrefLocation(r, path);
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								attributeNames(r, path);
								hrefLocation(r, path);
							}
						}
					}
				}
			}
		} else if (propfind.getProp().size() > 0) {
			// 获取指定属性值

			final Set<String> names = propfind.getProp().keySet();
			r = response(multistatus);
			attributes(r, path, names);
			if (r.ok()) {
				if (r.dir()) {
					if (depth == 1) {
						try (final Stream<Path> stream = Files.list(path)) {
							final Iterator<Path> iterator = stream.iterator();
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								attributes(r, path, names);
								hrefLocation(r, path);
							}
						}
					} else {
						try (final Stream<Path> stream = Files.walk(path, depth)) {
							final Iterator<Path> iterator = stream.iterator();
							iterator.next();// 忽略第一个（遍历根）
							while (iterator.hasNext()) {
								path = iterator.next();
								r = response(multistatus);
								attributes(r, path, names);
								hrefLocation(r, path);
							}
						}
					}
				}
			}
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
		// propertyupdate
		// multistatus
	}

	@Override
	protected void mkcol(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			// 规范未定义任何主体
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		response.setStatus(HTTPStatus.CREATED);
		final Path path = Path.of(root.toString(), request.getPath());
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
		if ("/".equals(request.getPath())) {
			// 禁止删除根
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Depth.INFINITY.equals(request.getHeader(Depth.NAME))) {
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		response.setStatus(HTTPStatus.NO_CONTENT);
		final Multistatus multistatus = new Multistatus();
		final Path path = Path.of(root.toString(), request.getPath());
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					if (e == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						multistatus.getResponses().add(null);
						return FileVisitResult.TERMINATE;
					}
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					multistatus.getResponses().add(null);
					return FileVisitResult.TERMINATE;
				}
			});
		} catch (FileAlreadyExistsException e) {
			response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
		} catch (SecurityException e) {
			response.setStatus(HTTPStatus.FORBIDDEN);
		} catch (IOException e) {
			response.setStatus(HTTPStatus.CONFLICT);
		}

		if (multistatus.getResponses().isEmpty()) {
			// OK
		} else {
			response.setStatus(HTTPStatus.MULTI_STATUS);
			final int type = check(request);
			if (type == XML) {
				XMLCoder.write(multistatus, response);
			} else if (type == JSON) {
				JSONCoder.write(multistatus, response);
			}
		}
	}

	@Override
	protected void put(Request request, Response response) throws Exception {
		final Path path = Path.of(root.toString(), request.getPath());
		if (Files.exists(path, options)) {
			if (Files.isDirectory(path, options)) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
				return;
			} else {
				response.setStatus(HTTPStatus.CREATED);
				try (SeekableByteChannel channel = Files.newByteChannel(path, options)) {
					if (request.hasContent()) {
						final DataBuffer buffer = (DataBuffer) request.getContent();
						buffer.transfer(channel);
					} else {
						channel.truncate(0);
					}
				} catch (FileAlreadyExistsException e) {
					response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
				} catch (SecurityException e) {
					response.setStatus(HTTPStatus.FORBIDDEN);
				} catch (IOException e) {
					response.setStatus(HTTPStatus.CONFLICT);
				}
			}
		} else {
			ByteChannel channel = null;
			response.setStatus(HTTPStatus.CREATED);
			try {
				Files.createFile(root);
				if (request.hasContent()) {
					channel = Files.newByteChannel(path, options);
					if (channel.isOpen()) {
						final DataBuffer buffer = (DataBuffer) request.getContent();
						buffer.transfer(channel);
					}
				}
			} catch (FileAlreadyExistsException e) {
				response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
			} catch (SecurityException e) {
				response.setStatus(HTTPStatus.FORBIDDEN);
			} catch (IOException e) {
				response.setStatus(HTTPStatus.CONFLICT);
			} finally {
				if (channel != null) {
					channel.close();
				}
			}
		}
	}

	@Override
	protected void copy(Request request, Response response) throws Exception {
		String destination = request.getHeader(HTTP1.Destination);
		if (Utility.isEmpty(destination)) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		} else {
			destination = FileResourceServlet.normalize(destination);
		}

		final Path source = Path.of(root.toString(), request.getPath());
		final Path target = Path.of(root.toString(), destination);
		if (Files.isSameFile(source, target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Files.exists(target, options)) {
			final String overwrite = request.getHeader(HTTP1.Overwrite);
			if (Utility.equal("F", overwrite)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			} else {
				Files.copy(source, root, StandardCopyOption.REPLACE_EXISTING);
				response.setStatus(HTTPStatus.NO_CONTENT);
			}
		} else {
			Files.copy(source, root, options);
			response.setStatus(HTTPStatus.CREATED);
		}
	}

	@Override
	protected void move(Request request, Response response) throws Exception {
		if (Depth.INFINITY.equals(request.getHeader(Depth.NAME))) {
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}
		String destination = request.getHeader(HTTP1.Destination);
		if (Utility.isEmpty(destination)) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		} else {
			destination = FileResourceServlet.normalize(destination);
		}

		final Path source = Path.of(root.toString(), request.getPath());
		final Path target = Path.of(root.toString(), destination);
		if (Files.isSameFile(source, target)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return;
		}
		if (Files.exists(target, options)) {
			final String overwrite = request.getHeader(HTTP1.Overwrite);
			if (Utility.equal("F", overwrite)) {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			} else {
				Files.move(source, root, StandardCopyOption.REPLACE_EXISTING);
				response.setStatus(HTTPStatus.NO_CONTENT);
			}
		} else {
			Files.move(source, root, options);
			response.setStatus(HTTPStatus.CREATED);
		}
	}

	@Override
	protected void lock(Request request, Response response) throws Exception {
	}

	@Override
	protected void unlock(Request request, Response response) throws Exception {
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
	 */

	private com.joyzl.webserver.webdav.elements.Response response(Multistatus multistatus) {
		final com.joyzl.webserver.webdav.elements.Response response = new com.joyzl.webserver.webdav.elements.Response();
		multistatus.getResponses().add(response);
		return response;
	}

	/** 获取指定路径的指定属性 */
	private void attributes(com.joyzl.webserver.webdav.elements.Response response, Path path, Set<String> names) throws IOException {
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

		final Propstat propstat = new Propstat();
		if (names == null || names.isEmpty()) {
			response.getPropstats().add(propstat);
			return;
		}

		if (names.contains(WEBDAV.DISPLAY_NAME)) {
			propstat.getProp().put(WEBDAV.DISPLAY_NAME, path.getFileName());
		}
		if (names.contains(WEBDAV.CREATION_DATE)) {
			propstat.getProp().put(WEBDAV.CREATION_DATE, attributes.creationTime());
		}
		if (attributes.isDirectory()) {
			if (names.contains(WEBDAV.RESOURCE_TYPE)) {
				propstat.getProp().put(WEBDAV.RESOURCE_TYPE, Collection.INSTANCE);
			}
		} else {
			if (names.contains(WEBDAV.GET_CONTENT_LANGUAGE)) {
				propstat.getProp().put(WEBDAV.GET_CONTENT_LANGUAGE, null);
			}
			if (names.contains(WEBDAV.GET_LAST_MODIFIED)) {
				propstat.getProp().put(WEBDAV.GET_LAST_MODIFIED, attributes.lastModifiedTime());
			}
			if (names.contains(WEBDAV.GET_CONTENT_LENGTH)) {
				propstat.getProp().put(WEBDAV.GET_CONTENT_LENGTH, attributes.size());
			}
			if (names.contains(WEBDAV.GET_CONTENT_TYPE)) {
				propstat.getProp().put(WEBDAV.GET_CONTENT_TYPE, contentType(path));
			}
			if (names.contains(WEBDAV.GET_ETAG)) {
				propstat.getProp().put(WEBDAV.GET_ETAG, ETag.makeWeak(attributes.size(), attributes.lastModifiedTime().toMillis()));
			}
		}

		if (names.contains(WEBDAV.LOCK_DISCOVERY)) {
			propstat.getProp().put(WEBDAV.LOCK_DISCOVERY, null);
		}
		if (names.contains(WEBDAV.SUPPORTED_LOCK)) {
			propstat.getProp().put(WEBDAV.SUPPORTED_LOCK, null);
		}

		response.getPropstats().add(propstat);

		if (propstat.getProp().size() < names.size()) {

		}
	}

	/** 获取指定路径的默认属性 */
	private void defaultAttributes(com.joyzl.webserver.webdav.elements.Response response, Path path, Set<String> includes) {
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

		Propstat propstat = new Propstat();
		propstat.getProp().put(WEBDAV.DISPLAY_NAME, path.getFileName());
		propstat.getProp().put(WEBDAV.CREATION_DATE, attributes.creationTime());
		if (attributes.isDirectory()) {
			response.dir(true);
			propstat.getProp().put(WEBDAV.RESOURCE_TYPE, Collection.INSTANCE);
		} else {
			response.dir(false);
			propstat.getProp().put(WEBDAV.GET_CONTENT_LANGUAGE, null);
			propstat.getProp().put(WEBDAV.GET_CONTENT_LENGTH, attributes.size());
			propstat.getProp().put(WEBDAV.GET_CONTENT_TYPE, contentType(path));
			propstat.getProp().put(WEBDAV.GET_ETAG, ETag.makeWeak(attributes.size(), attributes.lastModifiedTime().toMillis()));
			propstat.getProp().put(WEBDAV.GET_LAST_MODIFIED, attributes.lastModifiedTime());
		}
		propstat.getProp().put(WEBDAV.LOCK_DISCOVERY, null);
		propstat.getProp().put(WEBDAV.SUPPORTED_LOCK, null);
		response.getPropstats().add(propstat);

		// 目前不支持任何死属性
		if (!includes.isEmpty()) {
			propstat = new Propstat();
			for (String name : includes) {
				propstat.getProp().put(name, null);
			}
			propstat.setStatus(HTTPStatus.NOT_FOUND);
			response.getPropstats().add(propstat);
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
			response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
			return;
		}

		final Propstat propstat = new Propstat();
		propstat.getProp().put(WEBDAV.DISPLAY_NAME, null);
		propstat.getProp().put(WEBDAV.CREATION_DATE, null);
		propstat.getProp().put(WEBDAV.RESOURCE_TYPE, null);
		if (attributes.isDirectory()) {
			response.dir(true);
		} else {
			response.dir(false);
			propstat.getProp().put(WEBDAV.GET_CONTENT_LANGUAGE, null);
			propstat.getProp().put(WEBDAV.GET_CONTENT_LENGTH, null);
			propstat.getProp().put(WEBDAV.GET_CONTENT_TYPE, null);
			propstat.getProp().put(WEBDAV.GET_ETAG, null);
			propstat.getProp().put(WEBDAV.GET_LAST_MODIFIED, null);
			propstat.getProp().put(WEBDAV.RESOURCE_TYPE, null);

			propstat.getProp().put(WEBDAV.LOCK_DISCOVERY, null);
			propstat.getProp().put(WEBDAV.SUPPORTED_LOCK, null);
		}
		propstat.getProp().put(WEBDAV.LAST_ACCESS_TIME, null);

		final PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if (posix != null) {
			propstat.getProp().put(WEBDAV.OWNER, null);
			propstat.getProp().put(WEBDAV.GROUP, null);
			propstat.getProp().put(WEBDAV.PERMISSIONS, null);
		}
		final DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
		if (dos != null) {
			propstat.getProp().put(WEBDAV.ARCHIVE, null);
			propstat.getProp().put(WEBDAV.READONLY, null);
			propstat.getProp().put(WEBDAV.HIDDEN, null);
			propstat.getProp().put(WEBDAV.SYSTEM, null);
		}
		final AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
		if (acl != null) {
			propstat.getProp().put(WEBDAV.OWNER, null);
			propstat.getProp().put(WEBDAV.ACL, null);
		}
		response.getPropstats().add(propstat);
	}

	public static void main(String[] argments) throws Exception {
		// http://www.joyzl.net/webdav/

		final Path root = Path.of("D:\\GitHub\\web-server");
		final Path path = root.resolve("test/github-recovery-codes.txt");

		System.out.println(path);
		String uri = path.toString().substring(root.toString().length());
		System.out.println(uri);

	}

	private void hrefLocation(com.joyzl.webserver.webdav.elements.Response response, Path path) {
		String uri = path.toString().substring(root.toString().length());
		if (response.dir()) {
			if (uri.endsWith("/")) {
				response.setHref(base + uri);
			} else {
				response.setLocation(base + uri + "/");
			}
		} else {
			if (path.endsWith("/")) {
				response.setHref(base + uri.substring(0, uri.length() - 1));
			} else {
				response.setLocation(base + path);
			}
		}
	}

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
}
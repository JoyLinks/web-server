package com.joyzl.webserver.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.webserver.Utility;

/**
 * 文件资源
 * 
 * @author ZhangXi 2024年11月26日
 */
public class FileResourceServlet extends WEBResourceServlet {

	/** 文件大小阈值(16M)，超过此限制的文件无须压缩或缓存 */
	public final static int MAX = 1024 * 1024 * 16;

	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();
	/** 资源基础路径（不含通配符） */
	private final String base;
	/** 主目录 */
	private final File root;
	/** 缓存目录 */
	private final File cache;
	/** 错误页目录 */
	private File error;

	/** 默认文件名 */
	private String[] defaults = new String[] { "index.html", "default.html" };
	/** 压缩的文件扩展名 */
	private String[] compresses = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".xml" };
	/** 缓存的文件扩展名 */
	private String[] caches = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".jpg", ".jpeg", ".png", ".gif", ".ttf", ".woff", ".woff2" };
	/** 允许列示目录文件 */
	private boolean browse = false;
	/** 允许创建文件 */
	private boolean create = false;
	/** 允许删除文件 */
	private boolean delete = false;
	/** 是否使用弱验证 */
	private boolean weak = true;

	public FileResourceServlet(File root) {
		this(null, root, null);
	}

	public FileResourceServlet(String base, String root) {
		this(base, new File(root), null);
	}

	public FileResourceServlet(String base, String root, String cache) {
		this(base, new File(root), new File(cache));
	}

	public FileResourceServlet(String base, File root, File cache) {
		this.base = Utility.correctBase(base);
		this.root = root;
		if (cache != null) {
			// 构建缓存目录，如果不存在
			if (!cache.exists()) {
				if (!cache.mkdirs()) {
					cache = null;
				}
			}
		}
		this.cache = cache;
	}

	@Override
	protected WEBResource find(String path) {
		WEBResource resource = resources.get(path);
		if (resource == null) {
			final File file = Utility.resolveFile(root, base, path);
			if (file.exists()) {
				if (file.isDirectory()) {
					// DIR
					if (path.charAt(path.length() - 1) == '/') {
						// 查找默认页面
						final File page = findDefault(file);
						if (page != null) {
							synchronized (this) {
								resource = resources.get(path);
								if (resource == null) {
									resource = makeResource(page);
									resources.put(resource.getContentLocation(), resource);
									resources.put(path, resource);
								}
							}
						} else {
							// 返回目录资源
							// 可用于重定向或返回目录列表
							resource = new DirectoryResource(path, file, isBrowse());
						}
					} else {
						// 返回目录资源
						// 可用于重定向或返回目录列表
						resource = new DirectoryResource(path + '/', file, isBrowse());
					}
				} else {
					// FILE
					synchronized (this) {
						resource = resources.get(path);
						if (resource == null) {
							resource = makeResource(file);
							resources.put(path, resource);
						}
					}
				}
			} else {
				// 尝试查找
				resource = FileMultiple.find(file);
			}
		}
		return resource;
	}

	@Override
	protected WEBResource find(HTTPStatus status) {
		// 错误页面不进行压缩和缓存
		// 这不是应当经常出现的请求
		if (error != null) {
			// 查找指定目录
			final File file = new File(error, status.code() + ".html");
			if (file.exists()) {
				return new FileResource(Utility.resolvePath(root, base, file), file, false);
			}
		}
		// 查找根目录
		final File file = new File(root, status.code() + ".html");
		if (file.exists()) {
			return new FileResource(Utility.resolvePath(root, base, file), file, false);
		}
		return null;
	}

	@Override
	protected WEBResource create(String path, DataBuffer content) {
		final File file = Utility.resolveFile(root, base, path);
		if (file.equals(root)) {
			return null;
		}
		if (file.isDirectory()) {
			return null;
		}
		if (content == null) {
			try (FileOutputStream output = new FileOutputStream(file)) {
			} catch (IOException e) {
				return null;
			}
		} else {
			try (FileOutputStream output = new FileOutputStream(file)) {
				content.transfer(output.getChannel());
			} catch (IOException e) {
				return null;
			}
		}
		final WEBResource resource = makeResource(file);
		resources.put(path, resource);
		return resource;
	}

	@Override
	protected boolean delete(String path) {
		final WEBResource resource = resources.remove(path);
		final File file;
		if (resource != null) {
			if (resource instanceof FileResource f) {
				file = f.getFile();
			} else if (resource instanceof DirectoryResource d) {
				file = d.getDirectory();
			} else {
				file = null;
			}
		} else {
			file = Utility.resolveFile(root, base, path);
		}
		if (file != null) {
			if (file.equals(root)) {
				return false;
			}
			if (file.delete()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 构建文件为资源对象，资源对象提供WEB所需的标头和内容
	 */
	protected WEBResource makeResource(File file) {
		// 1.不缓存 不压缩
		// 2.不缓存 要压缩
		// 3.要缓存 不压缩
		// 4.要缓存 要压缩
		if (canCache(file)) {
			if (canCompress(file)) {
				return new FileCacheCompressResource(Utility.resolvePath(root, base, file), file, isWeak());
			} else {
				return new FileCacheResource(Utility.resolvePath(root, base, file), file, isWeak());
			}
		} else {
			if (canCompress(file)) {
				return new FileCompressResource(Utility.resolvePath(root, base, file), file, cache, isWeak());
			} else {
				return new FileResource(Utility.resolvePath(root, base, file), file, isWeak());
			}
		}
	}

	/**
	 * 查找目录下的默认页面文件
	 */
	protected File findDefault(File path) {
		File file;
		for (int index = 0; index < defaults.length; index++) {
			file = new File(path, defaults[index]);
			if (file.exists() && file.isFile()) {
				return file;
			}
		}
		return null;
	}

	/**
	 * 检查文件是否应压缩；<br>
	 * jpg和png图像文件本身已经过压缩，再次压缩已难以缩小，因此无须再压缩；<br>
	 * 音频和视频这类太大的文件也没有必要压缩，这会占用过多磁盘空间和运算资源，客户端应分块获取；<br>
	 * zip等已经压缩的文件当然也没有必要再次压缩。
	 */
	protected boolean canCompress(File file) {
		if (file.length() < MAX) {
			for (int index = 0; index < compresses.length; index++) {
				if (Utility.ends(file.getPath(), compresses[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检查文件是否应在内存缓存；
	 */
	protected boolean canCache(File file) {
		if (file.length() < MAX) {
			for (int index = 0; index < caches.length; index++) {
				if (Utility.ends(file.getPath(), caches[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取用于缓存的文件；未指定缓存目录则创建临时文件；
	 * 有指定缓存目录则在指定目录生成缓存文件，缓存文件是对源文件经过压缩后的文件，无须压缩的文件也无须缓存。
	 */
	protected File cacheFile(File file, String extension) throws IOException {
		// Linux文件名的长度限制是255个字符
		// windows文件名必须少于260个字符
		return File.createTempFile(file.getName(), extension, cache);
	}

	/**
	 * 获取文件资源主目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 获取已压缩文件资源临时目录
	 */
	public File getCache() {
		return cache;
	}

	/**
	 * 获取默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public String[] getDefaults() {
		return defaults;
	}

	/**
	 * 设置默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public void setDefaults(String[] values) {
		if (values == null) {
			defaults = new String[0];
		} else {
			defaults = values;
		}
	}

	/**
	 * @see #setDefaults(String[])
	 */
	public void setDefaults(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			defaults = new String[0];
		} else {
			defaults = new String[values.size()];
			int index = 0;
			for (String value : values) {
				defaults[index++] = value;
			}
		}
	}

	/**
	 * 获取应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public String[] getCompresses() {
		return compresses;
	}

	/**
	 * 设置应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public void setCompresses(String[] values) {
		if (values == null) {
			compresses = new String[0];
		} else {
			compresses = values;
		}
	}

	/**
	 * @see #setCompresses(String[])
	 */
	public void setCompresses(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			compresses = new String[0];
		} else {
			compresses = new String[values.size()];
			int index = 0;
			for (String value : values) {
				compresses[index++] = value;
			}
		}
	}

	/**
	 * 获取应缓存的文件扩展名
	 */
	public String[] getCaches() {
		return caches;
	}

	/**
	 * 设置应缓存的文件扩展名
	 */
	public void setCaches(String[] values) {
		if (values == null) {
			caches = new String[0];
		} else {
			caches = values;
		}
	}

	/**
	 * @see #setCaches(String[])
	 */
	public void setCaches(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			caches = new String[0];
		} else {
			caches = new String[values.size()];
			int index = 0;
			for (String value : values) {
				caches[index++] = value;
			}
		}
	}

	/**
	 * 获取错误页面所在目录
	 */
	public File getErrorPages() {
		return error;
	}

	/**
	 * 设置错误页面所在目录，其中文件按 404.html 匹配
	 */
	public void setErrorPages(String value) {
		if (value == null || value.isEmpty() || value.isBlank()) {
			error = null;
		} else {
			error = new File(value);
		}
	}

	/**
	 * 设置错误页面所在目录，其中文件按 404.html 匹配
	 */
	public void setErrorPages(File value) {
		error = value;
	}

	/**
	 * 获取是否可列出目录中的文件
	 */
	public boolean isBrowse() {
		return browse;
	}

	/**
	 * 设置是否可列出目录中的文件
	 */
	public void setBrowse(boolean value) {
		browse = value;
	}

	/**
	 * 获取是否可创建资源
	 */
	public boolean isCreate() {
		return create;
	}

	/**
	 * 设置是否可创建资源
	 */
	public void setCreate(boolean value) {
		create = value;
	}

	/**
	 * 获取是否可删除资源
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * 设置是否可删除资源
	 */
	public void setDelete(boolean value) {
		delete = value;
	}

	/**
	 * 获取是否使用弱验证器
	 */
	public boolean isWeak() {
		return weak;
	}

	/**
	 * 设置是否使用弱验证器
	 */
	public void setWeak(boolean value) {
		weak = value;
	}
}
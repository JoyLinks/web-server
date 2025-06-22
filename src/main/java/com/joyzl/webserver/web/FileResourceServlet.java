/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	public final static String[] DEFAULTS = new String[] { "index.html", "default.html" };
	public final static String[] COMPRESSES = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".xml" };
	public final static String[] CACHES = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".jpg", ".jpeg", ".png", ".gif", ".ttf", ".woff", ".woff2" };

	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();

	/** 主目录 */
	private final File root;
	/** 缓存目录 */
	private final File cache;
	/** 错误页目录 */
	private final File error;

	/** 默认文件名 */
	private final String[] defaults;
	/** 压缩的文件扩展名 */
	private final String[] compresses;
	/** 缓存的文件扩展名 */
	private final String[] caches;
	/** 允许列示目录文件 */
	private final boolean browsable;
	/** 允许创建文件 */
	private final boolean editable;
	/** 是否使用弱验证 */
	private final boolean weak;

	public FileResourceServlet(String path, String root) {
		this(path, root, null, null, null, null, null, false, false, true);
	}

	public FileResourceServlet(String path, String root, String cache, String error) {
		this(path, root, cache, error, null, null, null, false, false, true);
	}

	public FileResourceServlet(//
			String path, String root, String cache, String error, //
			String[] defaults, String[] compresses, String[] caches, //
			boolean browsable, boolean editable, boolean weak) {
		super(path);

		this.root = root == null ? null : new File(root);
		this.error = error == null ? null : new File(error);

		if (cache == null) {
			this.cache = null;
		} else {
			// 构建缓存目录，如果不存在
			final File dir = new File(cache);
			if (dir.exists()) {
				this.cache = dir;
			} else if (dir.mkdirs()) {
				this.cache = dir;
			} else {
				this.cache = null;
			}
		}

		if (defaults == null) {
			defaults = DEFAULTS;
		}
		this.defaults = defaults;

		if (compresses == null) {
			compresses = COMPRESSES;
		}
		this.compresses = compresses;

		if (caches == null) {
			caches = CACHES;
		}
		this.caches = caches;

		this.browsable = browsable;
		this.editable = editable;
		this.weak = weak;
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
							resource = new DirectoryResource(path, file, isBrowsable());
						}
					} else {
						// 返回目录资源
						// 可用于重定向或返回目录列表
						resource = new DirectoryResource(path + '/', file, isBrowsable());
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
	 * 获取资源路径
	 */
	public String getPath() {
		return base;
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
	 * 获取应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public String[] getCompresses() {
		return compresses;
	}

	/**
	 * 获取应缓存的文件扩展名
	 */
	public String[] getCaches() {
		return caches;
	}

	/**
	 * 获取错误页面所在目录，其中文件按 404.html 匹配
	 */
	public File getErrorPages() {
		return error;
	}

	/**
	 * 获取是否可列出目录中的文件
	 */
	public boolean isBrowsable() {
		return browsable;
	}

	/**
	 * 获取是否可创建资源
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * 获取是否使用弱验证器
	 */
	public boolean isWeak() {
		return weak;
	}
}
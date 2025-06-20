package com.joyzl.webserver;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Collection;

/**
 * 实用方法集
 * 
 * @author ZhangXi 2024年11月13日
 */
public class Utility extends com.joyzl.network.Utility {

	/**
	 * 转换字符串为整型，如果转换失败不会抛出异常
	 */
	public final static int value(CharSequence value, int defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value, 0, value.length(), 10);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 转换字符串为整型，如果转换失败不会抛出异常
	 */
	public final static boolean value(CharSequence value, boolean defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		if ("true".contentEquals(value)) {
			return true;
		}
		if ("TRUE".contentEquals(value)) {
			return true;
		}
		return defaultValue;
	}

	public static boolean equal(File file, String path) {
		if (file == null) {
			return path == null;
		}
		if (path == null) {
			return false;
		}
		if (path.equalsIgnoreCase(file.getPath())) {
			return true;
		}
		return false;
	}

	public static boolean equal(Path file, String path) {
		if (file == null) {
			return path == null;
		}
		if (path == null) {
			return false;
		}
		if (path.equalsIgnoreCase(file.toString())) {
			return true;
		}
		return false;
	}

	/**
	 * 修正并移除基础路径通配符，配置的URI路径包含通配符'*'用于匹配请求路径，定位文件资源时应转换为基础路径
	 */
	public static String correctBase(String base) {
		if (base != null) {
			if (base.length() == 0 || "*".equals(base) || "/".equals(base) || "/*".equals(base)) {
				return null;
			}
			if (base.charAt(0) == '*') {
				// */abs -> /
				return null;
			}
			if (base.charAt(base.length() - 1) == '*') {
				// /abc/* -> /abc
				return base.substring(0, base.length() - 1);
			}
			final int star = base.indexOf('*');
			if (star > 0) {
				// /abc/*.png -> /abc/
				return base.substring(star);
			}
		}
		return base;
	}

	/**
	 * 路径回溯后返回标准化路径，移除路径中的"."和".."
	 * <p>
	 * 路径回溯攻击：通过设计攻击路径，通过回溯符".."以此访问超出指定资源根目录之外的文件，例如存储密码的文件。
	 * 为防止此攻击行为应在定位资源文件之前应执行路径回溯。
	 * </p>
	 */
	public static String normalizePath(String path) {
		if (path.length() == 0) {
			return path;
		}
		if (path.length() == 1) {
			if (".".equals(path)) {
				return "/";
			}
			return path;
		}
		if (path.length() == 2) {
			if ("/.".equals(path) || "./".equals(path) || "..".equals(path)) {
				return "/";
			}
			return path;
		}
		if (path.length() == 3) {
			if ("/..".equals(path) || "/./".equals(path) || "../".equals(path)) {
				return "/";
			}
			return path;
		}

		// /root/.
		// /root/..
		// /root/text/./../img.png

		char c;
		int last = -1;
		StringBuilder sb = new StringBuilder(path.length());
		for (int end = path.length() - 1, index = 0; index <= end; index++) {
			c = path.charAt(index);
			if (c == '/') {
				if (index < end) {
					c = path.charAt(++index);
					if (c == '.') {
						if (index < end) {
							c = path.charAt(++index);
							if (c == '/') {
								if (index < end) {
									// */./*
									index--;
									continue;
								} else {
									// */./
									sb.append('/');
									break;
								}
							} else if (c == '.') {
								if (index < end) {
									c = path.charAt(++index);
									if (c == '/') {
										if (index < end) {
											// */../*
											if (last > 0) {
												sb.setLength(last - 1);
												last = -1;
											} else {
												// 二次回退，须查找
												last = sb.length();
												while (--last >= 0) {
													if (sb.charAt(last) == '/') {
														sb.setLength(last);
														break;
													}
												}
												last = -1;
											}
											index--;
											continue;
										} else {
											// */../
											if (last > 0) {
												sb.setLength(last);
												break;
											} else {
												// "/"
											}
										}
									} else {
										last = index - 2;
										sb.append('/');
										sb.append('.');
										sb.append('.');
									}
								} else {
									// */..
									sb.setLength(last);
									break;
								}
							} else {
								last = index - 1;
								sb.append('/');
								sb.append('.');
							}
						} else {
							// */.
							sb.append('/');
							break;
						}
					} else {
						last = index;
						sb.append('/');
					}
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * 请求路径转换为实际文件（或目录）；<br>
	 * 调用此方法之前意味着path已经与含有通配符的base匹配，此方法不会重复检查path是否匹配base。
	 * 
	 * @param root 资源根目录
	 * @param path 请求的完整路径
	 * @param base 请求的资源路径前缀
	 */
	public static File resolveFile(File root, String base, String path) {
		// "/" *
		// URL "http://192.168.0.1"
		// - URI "/"
		// - PTH "/"
		// URL "http://192.168.0.1/content/main.html"
		// - URI /content/main.html
		// - PTH /content/main.html
		// URL "http://192.168.0.1/eno"
		// - URI "/eno/index.html"
		// - PTH "/eno/index.html"

		if (base == null || base.length() == 0) {
			if (path.length() > 1) {
				return new File(root, path);
			} else {
				return root;
			}
		}

		// "/xx" *
		// "/xx/" *
		// URL "http://192.168.0.1/xx/"
		// - URI "/xx/"
		// - PTH "/"
		// URL "http://192.168.0.1/xx/content/main.html"
		// - URI "/xx/content/main.html"
		// - PTH "/content/main.html"

		// "/.well-known/access.log"

		if (path.length() == base.length()) {
			return root;
		}
		if (path.length() - base.length() == 1) {
			if (path.charAt(path.length() - 1) == '/') {
				return root;
			}
		}
		return new File(root, path.substring(base.length()));

		// * ".png"
		// * "/image.png"
		// 视为无base全path定位

		// "/image/" * ".png"
		// 视为"/image/" *
	}

	/**
	 * 请求路径转换为实际文件（或目录）；<br>
	 * 此方法将检查请求资源路径并分离路径部分。
	 * 
	 * @see {@link #resolveFile(File, String, String)}
	 */
	public static Path resolveFile(Path root, String base, String path) {
		// 检查PATH是否完整URI，并定位PATH部分
		int i = path.indexOf(':');
		if (i > 0) {
			i = path.indexOf('/', i + 3);
			if (i < 0) {
				i = 0;
			}
		}

		if (base == null || base.length() == 0) {
			if (path.length() > 1) {
				return Path.of(root.toString(), path.substring(i));
			} else {
				return root;
			}
		}
		if (path.length() == base.length()) {
			return root;
		}
		if (path.length() - base.length() == 1) {
			if (path.charAt(path.length() - 1) == '/') {
				return root;
			}
		}
		return Path.of(root.toString(), path.substring(i + base.length()));
	}

	/**
	 * 资源文件转换为资源路径(URL:PATH)<br>
	 * 调用此方法意味着已经在资源根目录定位到文件，此方法不会检查file是否位于root目录中
	 * 
	 * @param file 资源文件
	 * @return 资源路径 URL 的 PATH 部
	 */
	public static String resolvePath(File root, String base, File file) {
		String path = file.getPath().substring(root.getPath().length()).replace('\\', '/');
		if (base == null || base.length() == 0) {
			return path;
		}
		if (base.charAt(base.length() - 1) == '/') {
			return base + path;
		}
		return base + '/' + path;
	}

	/**
	 * @see {@link #resolvePath(File, String, File)}
	 */
	public static String resolvePath(Path root, String base, Path path) {
		// System.out.println();

		String p = root.relativize(path).toString().replace('\\', '/');
		if (base == null || base.length() == 0) {
			return p;
		}
		if (base.charAt(base.length() - 1) == '/') {
			return base + p;
		}
		return base + '/' + p;
	}

	//////////

	/**
	 * 在数组中添加元素，返回包含新元素的新数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayAdd(Class<T> type, T[] source, T value) {
		final T[] items;
		if (source.length == 0) {
			items = (T[]) Array.newInstance(type, 1);
			items[0] = value;
		} else {
			items = (T[]) Array.newInstance(type, source.length + 1);
			System.arraycopy(source, 0, items, 0, source.length);
			items[items.length - 1] = value;
		}
		return items;
	}

	/**
	 * 在数组中添加元素，返回包含新元素的新数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayAdd(T[] source, Collection<T> values) {
		if (values.isEmpty()) {
			return source;
		}
		final Object[] items = new Object[source.length + values.size()];
		System.arraycopy(source, 0, items, 0, source.length);
		int index = source.length;
		for (Object item : values) {
			items[index++] = item;
		}
		return (T[]) items;
	}

	/**
	 * 在数组中查找元素，返回元素的索引
	 */
	public static <T> int arrayFind(T[] source, T value) {
		if (value == null) {
			for (int i = 0; i < source.length; i++) {
				if (source[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < source.length; i++) {
				if (value.equals(source[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 在数组中移除元素，返回移除元素的新数组
	 */
	public static <T> T[] arrayRemove(T[] source, T value) {
		int index = 0;
		if (value == null) {
			for (; index < source.length; index++) {
				if (source[index] == null) {
					break;
				}
			}
		} else {
			for (; index < source.length; index++) {
				if (value.equals(source[index])) {
					break;
				}
			}
		}
		if (index < source.length) {
			return arrayRemove(source, index);
		}
		return source;
	}

	/**
	 * 在数组中移除元素，返回移除元素的新数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayRemove(T[] source, int index) {
		final Object[] items = new Object[source.length - 1];
		if (index == 0) {
			System.arraycopy(source, 1, items, 0, items.length);
		} else if (index == source.length - 1) {
			System.arraycopy(source, 0, items, 0, items.length);
		} else {
			System.arraycopy(source, 0, items, 0, index);
			System.arraycopy(source, index + 1, items, index, source.length - index);
		}
		return (T[]) items;
	}

	/**
	 * 在数组中移除元素，返回移除元素的新数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayRemove(T[] source, Collection<T> values) {
		Object[] items = new Object[source.length];
		int size = 0;
		for (int index = 0; index < source.length; index++) {
			if (values.contains(source[index])) {
				continue;
			}
			size++;
		}
		if (size > 0) {
			Object[] news = new Object[size];
			System.arraycopy(items, 0, news, 0, size);
			items = news;
		} else {
			items = new Object[0];
		}
		return (T[]) items;
	}
}
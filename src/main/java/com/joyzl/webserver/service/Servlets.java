/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.joyzl.odbs.ODBSReflect;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.servlet.ServletReload;

/**
 * 服务程序集，用于扫描发现或创建服务程序
 * 
 * @author ZhangXi 2025年6月18日
 */
public class Servlets {

	/** 可用的服务程序 */
	private static final Map<String, Class<? extends Servlet>> SERVLETS = new HashMap<>();
	/** 可用的重载程序 */
	private static final Map<String, ServletReload> RELOADS = new HashMap<>();

	static {
		// 扫描内部服务程序
		Servlets.scanServlets("com.joyzl.webserver");
		// Servlets.scanServlets("com.joyzl.webserver.web");
		// Servlets.scanServlets("com.joyzl.webserver.webdav");
		// Servlets.scanServlets("com.joyzl.webserver.servlet");
		// Servlets.scanServlets("com.joyzl.webserver.manage");
	}

	/**
	 * 扫描指定包中的服务程序(Servlet)和重载程序(ServletReload)
	 */
	@SuppressWarnings("unchecked")
	public static void scanServlets(String... packages) {
		for (String pkg : packages) {
			final List<Class<?>> classes = ODBSReflect.scanClass(pkg);
			for (Class<?> clazz : classes) {
				if (ODBSReflect.isImplemented(clazz, Servlet.class)) {
					register((Class<? extends Servlet>) clazz);
				}
				if (ODBSReflect.isImplemented(clazz, ServletReload.class)) {
					registerReloader((Class<? extends ServletReload>) clazz);
				}
			}
		}
	}

	/**
	 * 注册服务程序，注册的服务程序可创建实例并添加到服务集
	 */
	public static boolean register(Class<? extends Servlet> servletClass) {
		if (ODBSReflect.canUsable(servletClass)) {
			// 构造函数无参
			if (ODBSReflect.canInstance(servletClass)) {
				SERVLETS.put(servletClass.getName(), servletClass);
				return true;
			}
			// 构造函数:绑定路径参数(path)
			if (ODBSReflect.canInstance(servletClass, String.class)) {
				SERVLETS.put(servletClass.getName(), servletClass);
				return true;
			}
			// 构造函数:绑定路径参数(path),额外参数(Map)
			if (ODBSReflect.canInstance(servletClass, String.class, Map.class)) {
				SERVLETS.put(servletClass.getName(), servletClass);
				return true;
			}
		}
		return false;
	}

	/**
	 * 注册服务重载程序，注册的服务重载程序可更精确的创建实例并添加到服务集
	 */
	public static boolean registerReloader(Class<? extends ServletReload> reloadClass) {
		if (ODBSReflect.canUsable(reloadClass)) {
			if (ODBSReflect.canInstance(reloadClass)) {
				final ServletClass annotation = ODBSReflect.findAnnotation(reloadClass, ServletClass.class);
				if (annotation != null) {
					try {
						final ServletReload instance = reloadClass.getConstructor().newInstance();
						if (instance != null) {
							RELOADS.put(instance.name(), instance);
							return true;
						}
					} catch (Exception e) {
						// Logger.error(e);
					}
				}
			}
		}
		return false;
	}

	/**
	 * 创建指定名称服务程序实例
	 */
	public static Servlet create(String type, String path, Map<String, String> parameters) {
		final ServletReload reload = RELOADS.get(type);
		if (reload != null) {
			if (path == null) {
				path = defaultPath(reload);
			}
			return reload.create(path, parameters);
		}

		final Class<? extends Servlet> s = SERVLETS.get(type);
		if (s != null) {
			if (path == null) {
				path = defaultPath(s);
			}
			try {
				return s.getConstructor(String.class, Map.class).newInstance(path, parameters);
			} catch (Exception e2) {
				try {
					return s.getConstructor(String.class).newInstance(path);
				} catch (Exception e1) {
					try {
						return s.getConstructor().newInstance();
					} catch (Exception e0) {
						return null;
					}
				}
			}
		}
		return null;
	}

	public static Class<? extends Servlet> findClass(String type) {
		return SERVLETS.get(type);
	}

	public static ServletReload findReload(String name) {
		return RELOADS.get(name);
	}

	/**
	 * 获取ServletReload注解设置的默认路径
	 */
	public static String defaultPath(ServletReload reload) {
		final ServletClass annotation = ODBSReflect.findAnnotation(reload.getClass(), ServletClass.class);
		if (annotation != null) {
			return defaultPath(annotation.servlet());
		}
		return null;
	}

	/**
	 * 获取ServletPath注解设置的默认路径
	 */
	public static String defaultPath(Class<?> clazz) {
		final ServletPath annotation = ODBSReflect.findAnnotation(clazz, ServletPath.class);
		return annotation != null ? annotation.path() : null;
	}

	/**
	 * 获取ServletPath注解设置的默认路径
	 */
	public static String defaultPath(Servlet servlet) {
		return defaultPath(servlet.getClass());
	}

	/**
	 * 输出调试
	 */
	public static Object checkString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("\nSERVLETS:\n");
		for (Entry<String, Class<? extends Servlet>> entry : SERVLETS.entrySet()) {
			builder.append(entry.getKey());
			builder.append(':');
			builder.append(entry.getValue());
			builder.append('\n');
		}

		builder.append("RELOADS:\n");
		for (Entry<String, ServletReload> entry : RELOADS.entrySet()) {
			builder.append(entry.getKey());
			builder.append(':');
			builder.append(entry.getValue().getClass());
			builder.append('\n');
		}
		return builder.toString();
	}
}
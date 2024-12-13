package com.joyzl.webserver;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.ServletPath;
import com.joyzl.network.web.Wildcards;
import com.joyzl.odbs.ODBSReflect;

/**
 * 实用方法集
 * 
 * @author ZhangXi 2024年11月13日
 */
public class Utility extends com.joyzl.network.Utility {

	/**
	 * 扫描指定包中的Servlet并绑定ServletURI注解指定的URI
	 */
	public static void scanServlets(Wildcards<Servlet> instances, Collection<String> packages) throws Exception {
		for (String pkg : packages) {
			final List<Class<?>> classes = ODBSReflect.scanClass(pkg);
			for (Class<?> clazz : classes) {
				if (ODBSReflect.canUsable(clazz)) {
					if (ODBSReflect.canInstance(clazz)) {
						if (ODBSReflect.isImplemented(clazz, Servlet.class)) {
							ServletPath annotation = ODBSReflect.findAnnotation(clazz, ServletPath.class);
							if (annotation != null) {
								try {
									Servlet servlet = (Servlet) clazz.getConstructor().newInstance();
									instances.bind(annotation.path(), servlet);
								} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
									throw e;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 转换字符串为整型，如果转换失败不会抛出异常
	 *
	 * @see #value(CharSequence, int, int)
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
}
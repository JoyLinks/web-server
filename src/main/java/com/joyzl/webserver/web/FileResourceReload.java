package com.joyzl.webserver.web;

import java.util.Arrays;
import java.util.Map;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = FileResourceServlet.class)
public class FileResourceReload extends ServletReload {

	@Override
	public String name() {
		return "RESOURCE";
	}

	// content:资源目录
	// cache:缓存目录
	// error:错误页面所在目录
	//
	// defaults:逗号分隔的多个默认文件名
	// compresses:逗号分隔的多个应压缩的文件扩展名
	// caches:逗号分隔的多个应缓存的文件扩展名
	//
	// browsable:可浏览目录
	// editable:可创建资源
	// weak:使用弱验证器

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		final String content = parameters.get("content");
		if (Utility.isEmpty(content)) {
			return null;
		}

		final String cache = parameters.get("cache");
		final String error = parameters.get("error");

		final String[] defaults = split(parameters.get("defaults"));
		final String[] compresses = split(parameters.get("compresses"));
		final String[] caches = split(parameters.get("caches"));

		final boolean browsable = Utility.value(parameters.get("browsable"), false);
		final boolean editable = Utility.value(parameters.get("editable"), false);
		final boolean weak = Utility.value(parameters.get("weak"), false);

		return new FileResourceServlet(path, //
			content, cache, error, //
			defaults, compresses, caches, //
			browsable, editable, weak);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof FileResourceServlet s) {
			if (Utility.equal(s.getRoot(), parameters.get("content"))) {
			} else {
				return true;
			}
			if (Utility.equal(s.getCache(), parameters.get("cache"))) {
			} else {
				return true;
			}
			if (Utility.equal(s.getErrorPages(), parameters.get("error"))) {
			} else {
				return true;
			}

			final String[] defaults = split(parameters.get("defaults"));
			if (s.getDefaults() == null) {
				if (defaults == null) {
				} else {
					return true;
				}
			} else {
				if (defaults == null) {
					return true;
				}
				if (Arrays.equals(s.getDefaults(), defaults)) {
				} else {
					return true;
				}
			}
			final String[] compresses = split(parameters.get("compresses"));
			if (s.getCompresses() == null) {
				if (compresses == null) {
				} else {
					return true;
				}
			} else {
				if (compresses == null) {
					return true;
				}
				if (Arrays.equals(s.getCompresses(), compresses)) {
				} else {
					return true;
				}
			}
			final String[] caches = split(parameters.get("caches"));
			if (s.getCaches() == null) {
				if (caches == null) {
				} else {
					return true;
				}
			} else {
				if (caches == null) {
					return true;
				}
				if (Arrays.equals(s.getCaches(), caches)) {
				} else {
					return true;
				}
			}

			if (s.isBrowsable() != Utility.value(parameters.get("browsable"), false)) {
				return true;
			}
			if (s.isEditable() != Utility.value(parameters.get("editable"), false)) {
				return true;
			}
			if (s.isWeak() != Utility.value(parameters.get("weak"), false)) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	String[] split(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() == 0) {
			return new String[0];
		}
		final String[] values = value.split(",");
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i].trim();
		}
		return values;
	}
}
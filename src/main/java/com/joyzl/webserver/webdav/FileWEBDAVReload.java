/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import java.util.Map;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = FileWEBDAVServlet.class)
public class FileWEBDAVReload extends ServletReload {

	@Override
	public String name() {
		return "WEBDAV";
	}

	// content:资源目录
	// allProperty:可读所有属性

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		final String content = parameters.get("content");
		if (Utility.isEmpty(content)) {
			return null;
		}
		final boolean all = Utility.value(parameters.get("allProperty"), false);
		return new FileWEBDAVServlet(path, content, all);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof FileWEBDAVServlet s) {
			if (Utility.equal(s.getRoot(), parameters.get("content"))) {
			} else {
				return true;
			}
			if (s.isAllProperty() != Utility.value(parameters.get("allProperty"), false)) {
				return true;
			}
		}
		return false;
	}
}
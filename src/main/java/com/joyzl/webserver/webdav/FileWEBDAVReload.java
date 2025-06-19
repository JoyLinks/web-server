package com.joyzl.webserver.webdav;

import java.util.HashMap;
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

	@Override
	public Map<String, String> parameters() {
		final Map<String, String> items = new HashMap<>();
		items.put("content", "资源目录");
		items.put("allProperty", "可读所有属性");
		return items;
	}
}
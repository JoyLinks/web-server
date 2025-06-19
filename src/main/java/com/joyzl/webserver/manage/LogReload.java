package com.joyzl.webserver.manage;

import java.util.Collections;
import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = LogServlet.class)
public class LogReload extends ServletReload {

	@Override
	public String name() {
		return "LOG";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new LogServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof LogServlet) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Map<String, String> parameters() {
		return Collections.emptyMap();
	}
}
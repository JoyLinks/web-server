package com.joyzl.webserver.manage;

import java.util.Collections;
import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = IPServlet.class)
public class IPReload extends ServletReload {

	@Override
	public String name() {
		return "IP";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new IPServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof IPServlet) {
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
package com.joyzl.webserver.manage;

import java.util.Collections;
import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = VisitsServlet.class)
public class VisitsReload extends ServletReload {

	@Override
	public String name() {
		return "VISITS";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new VisitsServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof VisitsServlet) {
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
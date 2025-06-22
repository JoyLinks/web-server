package com.joyzl.webserver.manage;

import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = RosterServlet.class)
public class RosterReload extends ServletReload {

	@Override
	public String name() {
		return "ROSTER";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new RosterServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof RosterServlet) {
			return false;
		} else {
			return true;
		}
	}
}
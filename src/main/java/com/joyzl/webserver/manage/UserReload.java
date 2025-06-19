package com.joyzl.webserver.manage;

import java.util.Collections;
import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = UserServlet.class)
public class UserReload extends ServletReload {

	@Override
	public String name() {
		return "USER";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new UserServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof UserServlet) {
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
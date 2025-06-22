package com.joyzl.webserver.manage;

import java.util.Map;

import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = SettingServlet.class)
public class SettingReload extends ServletReload {

	@Override
	public String name() {
		return "SETTING";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		return new SettingServlet(path);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof SettingServlet) {
			return false;
		} else {
			return true;
		}
	}
}
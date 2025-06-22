/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

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
}
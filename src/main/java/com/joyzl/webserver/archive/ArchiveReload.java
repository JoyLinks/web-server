/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.archive;

import java.io.IOException;
import java.util.Map;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.ServletClass;
import com.joyzl.webserver.servlet.ServletReload;

@ServletClass(servlet = ArchiveServlet.class)
public class ArchiveReload extends ServletReload {

	@Override
	public String name() {
		return "ARCHIVE";
	}

	// content:归档目录
	// expire:过期天数

	@Override
	public Servlet create(String path, Map<String, String> parameters) throws IOException {
		final String content = parameters.get("content");
		if (Utility.isEmpty(content)) {
			return null;
		}
		final int expire = Utility.value(parameters.get("expire"), 0);
		return new ArchiveServlet(path, content, expire);
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof ArchiveServlet s) {
			if (Utility.equal(s.archive().path(), parameters.get("content"))) {
			} else {
				return true;
			}
			if (s.archive().expire() != Utility.value(parameters.get("expire"), 0)) {
				return true;
			}
		}
		return false;
	}
}
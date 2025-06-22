/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.servlet;

import java.util.Map;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.webserver.Utility;

@ServletClass(servlet = Location.class)
public class LocationReload extends ServletReload {

	// location:请求重定向的URL或路径
	// status:请求重定向状态码，默认为301

	@Override
	public String name() {
		return "LOCATION";
	}

	@Override
	public Servlet create(String path, Map<String, String> parameters) {
		final String location = parameters.get("location");
		if (Utility.isEmpty(location)) {
			return null;
		}
		if (Utility.equal(path, location)) {
			return null;
		}
		final int state = Utility.value(parameters.get("status"), 0);
		return new Location(path, location, HTTPStatus.fromCode(state));
	}

	@Override
	public boolean differently(Servlet servlet, Map<String, String> parameters) {
		if (servlet instanceof Location l) {

			if (Utility.equal(parameters.get("location"), l.getLocation())) {
			} else {
				return true;
			}

			final int status = Utility.value(parameters.get("status"), 0);
			if (status == 0) {
				return l.getStatus() != HTTPStatus.MOVED_PERMANENTLY;
			} else {
				return l.getStatus().code() != status;
			}
		} else {
			return true;
		}
	}
}
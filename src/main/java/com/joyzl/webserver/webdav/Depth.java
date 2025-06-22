/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;
import com.joyzl.network.http.Request;

/**
 * Depth:infinity
 * 
 * @author ZhangXi 2025年2月7日
 */
public class Depth extends Header {

	public final static String NAME = HTTP1.Depth;
	public final static String INFINITY = "infinity";

	@Override
	public String getHeaderName() {
		return "Depth";
	}

	@Override
	public String getHeaderValue() {
		return null;
	}

	@Override
	public void setHeaderValue(String value) {
	}

	public static int get(Request request) {
		String value = request.getHeader(NAME);
		if (value == null || value.length() == 0) {
			return Integer.MAX_VALUE;
		}
		if (INFINITY.equalsIgnoreCase(value)) {
			return Integer.MAX_VALUE;
		}
		return Integer.parseInt(value);
	}
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;
import com.joyzl.network.http.Request;

public class Overwrite extends Header {

	public final static String NAME = HTTP1.Overwrite;
	public final static String T = "T";
	public final static String F = "F";

	@Override
	public String getHeaderName() {
		return null;
	}

	@Override
	public String getHeaderValue() {
		return null;
	}

	@Override
	public void setHeaderValue(String value) {
	}

	public static boolean get(Request request) {
		String value = request.getHeader(NAME);
		if (Utility.isEmpty(value)) {
			return true;
		}
		if (F.equals(value)) {
			return false;
		}
		return true;
	}
}

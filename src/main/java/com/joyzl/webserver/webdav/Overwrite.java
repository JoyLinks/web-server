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

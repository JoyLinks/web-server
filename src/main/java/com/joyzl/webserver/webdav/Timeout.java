/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;

public class Timeout extends Header {

	public final static String NAME = HTTP1.Timeout;
	public final static String INFINITE = "Infinite";
	public final static String SECOND = "Second-";

	// Timeout: Infinite, Second-4100000000
	// Timeout: Second-3600
	// Timeout: Infinite

	private String value;

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	public boolean isInfinite() {
		if (value == null) {
			return false;
		}
		return value.contains(INFINITE);
	}

	public long getSecond() {
		if (value == null) {
			return 0;
		}
		int begin = value.indexOf(SECOND);
		if (begin < 0) {
			return 0;
		}
		begin += SECOND.length();
		return Long.parseUnsignedLong(value, begin, value.length(), 10);
	}

	public final static Timeout parse(String value) {
		if (Utility.noEmpty(value)) {
			Timeout header = new Timeout();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}

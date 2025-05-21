package com.joyzl.webserver.webdav;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;

public class Timeout extends Header {

	public final static String NAME = HTTP1.Timeout;
	public final static String INFINITE = "Infinite";

	// Timeout: Infinite, Second-4100000000
	// Timeout: Second-3600
	// Timeout: Infinite

	@Override
	public String getHeaderName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeaderValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHeaderValue(String value) {
		// TODO Auto-generated method stub

	}

}

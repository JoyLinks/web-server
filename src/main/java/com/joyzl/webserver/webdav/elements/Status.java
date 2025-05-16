package com.joyzl.webserver.webdav.elements;

import com.joyzl.network.http.HTTPStatus;

/**
 * 状态
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Status {
	/*-
	 * <!ELEMENT status (#PCDATA) >
	 * <status>HTTP/1.1 200 OK</status>
	 */
	public final static String OK = "200 OK";

	String getStatus();

	void setStatus(String value);

	default void setStatus(HTTPStatus value) {
		setStatus(value.code() + " " + value.text());
	}

	/** STATUS: "200 OK" */
	default boolean ok() {
		return getStatus() != null && OK.equals(getStatus());
	}
}
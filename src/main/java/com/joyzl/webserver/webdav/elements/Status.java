package com.joyzl.webserver.webdav.elements;

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

	String getStatus();

	void setStatus(String value);
}
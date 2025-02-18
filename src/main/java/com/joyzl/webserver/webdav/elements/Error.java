package com.joyzl.webserver.webdav.elements;

/**
 * 错误
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Error {
	/*-
	 * <!ELEMENT error ANY >
	 */

	String getError();

	void setError(String value);
}
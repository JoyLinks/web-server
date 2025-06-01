package com.joyzl.webserver.webdav.elements;

/**
 * 锁过期前经过的秒数
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Timeout {

	/*-
	 * <!ELEMENT timeout (#PCDATA) >
	 */

	long getTimeout();

	void setTimeout(long value);

	boolean valid();
}
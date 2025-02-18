package com.joyzl.webserver.webdav.elements;

/**
 * 位置
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Location {
	/*-
	 * <!ELEMENT location (href)>
	 * <location>
	 * 		<href>...</href>
	 * </location>
	 */

	String getLocation();

	void setLocation(String value);
}
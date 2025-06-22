/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
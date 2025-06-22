/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

/**
 * 绝对或者相对路径
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Href {
	/*-
	 * <!ELEMENT href (#PCDATA)>
	 * <href>http://www.example.com/file</href>
	 */

	String getHref();

	void setHref(String value);
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

/**
 * 响应描述
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface ResponseDescription {
	/*-
	 * <!ELEMENT responsedescription (#PCDATA) >
	 * <responsedescription>There has been an access violation error.</responsedescription> 
	 */

	public String getDescription();

	public void setDescription(String value);
}
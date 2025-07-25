/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.Set;

/**
 * 包含在PROPFIND响应中的属性的名称
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Include {
	/*-
	 * <!ELEMENT include ANY >
	 * <D:include> 
	    <D:supported-live-property-set/> 
	    <D:supported-report-set/> 
	   </D:include>
	 */

	Set<String> include();

	Set<String> getInclude();

	void setInclude(Set<String> values);

	boolean hasInclude();
}
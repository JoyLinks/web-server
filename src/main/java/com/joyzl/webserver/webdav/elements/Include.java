package com.joyzl.webserver.webdav.elements;

import java.util.List;

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

	List<String> getInclude();

	void setInclude(List<String> values);
}
package com.joyzl.webserver.webdav.elements;

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

	java.util.Set<String> getInclude();

	void setInclude(java.util.Set<String> values);
}
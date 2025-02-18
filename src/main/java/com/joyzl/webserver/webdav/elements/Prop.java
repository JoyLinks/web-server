package com.joyzl.webserver.webdav.elements;

import java.util.Map;

/**
 * 资源相关的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Prop {
	/*-
	 * <!ELEMENT prop ANY >
	 */

	public Map<String, Object> getProp();

	public void setProp(Map<String, Object> values);
}
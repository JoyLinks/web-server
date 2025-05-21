package com.joyzl.webserver.webdav.elements;

import java.util.List;

/**
 * 资源相关的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Prop {
	/*-
	 * <!ELEMENT prop ANY >
	 */

	List<Property> prop();

	List<Property> getProp();

	void setProp(List<Property> values);

	boolean hasProp();
}
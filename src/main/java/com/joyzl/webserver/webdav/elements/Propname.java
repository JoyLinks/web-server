package com.joyzl.webserver.webdav.elements;

import java.util.Set;

/**
 * 指定仅返回资源上属性名称的列表
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Propname {
	/*-
	 * <!ELEMENT propname EMPTY >
	 */

	Set<String> prop();

	Set<String> getProp();

	void setProp(Set<String> values);

	boolean hasProp();
}
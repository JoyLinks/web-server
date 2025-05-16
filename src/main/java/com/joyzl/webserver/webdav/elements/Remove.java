package com.joyzl.webserver.webdav.elements;

import java.util.HashMap;
import java.util.Map;

/**
 * 要从资源删除的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Remove extends Element implements Prop {
	/*-
	 * <!ELEMENT remove (prop) >
	 */

	private final Map<String, Object> prop = new HashMap<>();

	@Override
	public Map<String, Object> getProp() {
		return prop;
	}

	@Override
	public void setProp(Map<String, Object> values) {
		if (prop != values) {
			prop.clear();
			prop.putAll(values);
		}
	}
}
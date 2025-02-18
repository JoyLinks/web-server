package com.joyzl.webserver.webdav.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PROPFIND方法返回的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Propfind extends Element implements Prop, Propname, Allprop, Include {
	/*-
	 * <!ELEMENT propfind ( propname | (allprop, include?) | prop ) >
	 */

	private Map<String, Object> prop = new HashMap<>();
	private List<String> include;
	private boolean propname;
	private boolean allprop;

	public boolean isPropname() {
		return propname;
	}

	public void setPropname(boolean value) {
		propname = value;
	}

	public boolean isAllprop() {
		return allprop;
	}

	public void setAllprop(boolean value) {
		allprop = value;
	}

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

	@Override
	public List<String> getInclude() {
		return include;
	}

	@Override
	public void setInclude(List<String> values) {
		include = values;
	}
}
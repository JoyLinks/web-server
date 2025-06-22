/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.HashSet;
import java.util.Set;

/**
 * PROPFIND方法返回的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Propfind extends Element implements Propname, Allprop, Include {
	/*-
	 * <!ELEMENT propfind ( propname | (allprop, include?) | prop ) >
	 */

	private Set<String> prop;
	private Set<String> include;
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
	public Set<String> getProp() {
		return prop;
	}

	@Override
	public void setProp(Set<String> values) {
		if (prop != values) {
			if (prop == null) {
				prop = values;
			} else {
				prop.clear();
				prop.addAll(values);
			}
		}
	}

	@Override
	public Set<String> prop() {
		if (prop == null) {
			prop = new HashSet<>();
		}
		return prop;
	}

	@Override
	public boolean hasProp() {
		return prop != null && prop.size() > 0;
	}

	@Override
	public java.util.Set<String> getInclude() {
		return include;
	}

	@Override
	public void setInclude(java.util.Set<String> values) {
		if (include != values) {
			if (include == null) {
				include = new HashSet<>(values);
			} else {
				include.clear();
				include.addAll(values);
			}
		}
	}

	@Override
	public boolean hasInclude() {
		return include != null && include.size() > 0;
	}

	@Override
	public Set<String> include() {
		if (include == null) {
			include = new HashSet<>();
		}
		return include;
	}
}
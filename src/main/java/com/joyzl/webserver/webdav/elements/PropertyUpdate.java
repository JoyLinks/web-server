package com.joyzl.webserver.webdav.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 更改资源属性的请求
 * 
 * @author ZhangXi 2025年2月9日
 */
public class PropertyUpdate extends Element implements Prop {
	/*-
	 * <!ELEMENT propertyupdate (remove | set)+ >
	 */

	private List<Property> proo;

	@Override
	public List<Property> prop() {
		if (proo == null) {
			proo = new ArrayList<>();
		}
		return proo;
	}

	@Override
	public boolean hasProp() {
		return proo != null && proo.size() > 0;
	}

	@Override
	public List<Property> getProp() {
		return proo;
	}

	@Override
	public void setProp(List<Property> values) {
		if (proo != values) {
			if (proo == null) {
				proo = values;
			} else {
				proo.clear();
				proo.addAll(values);
			}
		}
	}
}
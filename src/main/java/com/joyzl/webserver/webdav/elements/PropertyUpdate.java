package com.joyzl.webserver.webdav.elements;

/**
 * 更改资源属性的请求
 * 
 * @author ZhangXi 2025年2月9日
 */
public class PropertyUpdate extends Element {
	/*-
	 * <!ELEMENT propertyupdate (remove | set)+ >
	 */

	private Set set;
	private Remove remove;

	public Remove getRemove() {
		return remove;
	}

	public void setRemove(Remove value) {
		remove = value;
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set value) {
		set = value;
	}
}
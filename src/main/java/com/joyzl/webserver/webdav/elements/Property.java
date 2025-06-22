/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

/**
 * 资源属性（活属性/死属性）
 * 
 * @author ZhangXi 2025年5月20日
 */
public class Property extends Element {

	public final static int NORMAL = 0, SET = 1, REMOVE = 2;

	private int type = NORMAL;
	private String name;
	private Object value;

	public Property() {
	}

	public Property(int type) {
		this.type = type;
	}

	public Property(String name) {
		this.name = name;
	}

	public Property(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean setting() {
		return type == SET;
	}

	public boolean removing() {
		return type == REMOVE;
	}
}
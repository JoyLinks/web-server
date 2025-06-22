/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.ArrayList;
import java.util.List;

/*
 * 属性和状态
 */
public class Propstat extends Element implements Prop, Status, Error, ResponseDescription {
	/*-
	 * <!ELEMENT propstat (prop, status, error?, responsedescription?) >
	 * 
	 * <D:propstat> 
	        <D:prop> 
	            <R:bigbox> 
	                <R:BoxType>Box type A</R:BoxType> 
	            </R:bigbox> 
	            <R:author> 
	                <R:Name>J.J. Johnson</R:Name> 
	            </R:author> 
	        </D:prop> 
	        <D:status>HTTP/1.1 200 OK</D:status> 
	    </D:propstat> 
	 */

	private List<Property> prop;
	private String description;
	private String version;
	private String status = OK;
	private String error;

	public Propstat() {
	}

	public Propstat(String version) {
		this.version = version;
	}

	@Override
	public List<Property> getProp() {
		return prop;
	}

	@Override
	public void setProp(List<Property> values) {
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
	public List<Property> prop() {
		if (prop == null) {
			prop = new ArrayList<>();
		}
		return prop;
	}

	@Override
	public boolean hasProp() {
		return prop != null && prop.size() > 0;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String value) {
		description = value;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String value) {
		status = value;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public void setError(String value) {
		error = value;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public void version(String value) {
		version = value;
	}
}
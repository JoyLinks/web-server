package com.joyzl.webserver.webdav.elements;

import java.util.HashMap;
import java.util.Map;

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

	private Map<String, Object> prop = new HashMap<>();
	private String description;
	private String status = OK;
	private String error;

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
}
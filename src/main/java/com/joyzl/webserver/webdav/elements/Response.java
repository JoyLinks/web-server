package com.joyzl.webserver.webdav.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应结果
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Response extends Element implements Href, Location, Status, ResponseDescription, Error {
	/*-
	 * <!ELEMENT response (href, ((href*, status)|(propstat+)), error?, responsedescription? , location?) >
	 */

	private boolean dir;
	private String href;
	private List<Propstat> propstat = new ArrayList<>();
	private String location;
	private String description;
	private String version;
	private String status = OK;
	private String error;

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public void setHref(String value) {
		href = value;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String value) {
		location = value;
	}

	public List<Propstat> getPropstats() {
		return propstat;
	}

	public void setPropstats(List<Propstat> values) {
		if (propstat != values) {
			propstat.clear();
			propstat.addAll(values);
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

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String value) {
		status = value;
	}

	/** 指示是否目录 */
	public boolean dir() {
		return dir;
	}

	/** 设置是否目录 */
	public void dir(boolean dir) {
		this.dir = dir;
	}
}
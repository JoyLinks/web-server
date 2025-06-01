package com.joyzl.webserver.entities;

/**
 * 请求重定向
 * 
 * @author ZhangXi 2025年5月27日
 */
public class Location {

	private String path;
	private String location;
	private int status;

	public String getPath() {
		return path;
	}

	public void setPath(String value) {
		path = value;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String value) {
		location = value;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}
}
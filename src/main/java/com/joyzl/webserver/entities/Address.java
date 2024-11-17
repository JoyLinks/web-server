package com.joyzl.webserver.entities;

/**
 * 地址
 * 
 * @author ZhangXi 2024年11月17日
 */
public class Address {

	private boolean allow;
	private String host;

	/**
	 * 获取主机地址
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 设置主机地址
	 */
	public void setHost(String value) {
		host = value;
	}

	/**
	 * 获取是否白名单（仅允许）
	 */
	public boolean isAllow() {
		return allow;
	}

	/**
	 * 设置是否白名单（仅允许）
	 */
	public void setAllow(boolean value) {
		allow = value;
	}

	/**
	 * 获取是否黑名单（拒绝）
	 */
	public boolean isDeny() {
		return !allow;
	}

	/**
	 * 设置是否黑名单（拒绝）
	 */
	public void setDeny(boolean value) {
		allow = !value;
	}
}
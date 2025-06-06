package com.joyzl.webserver.entities;

/**
 * 地址阻止或允许
 * 
 * @author ZhangXi 2024年11月17日
 */
public class Address {

	/** 关联主机 */
	private String[] host;
	/** 允许或阻止 */
	private boolean allow;
	/** 网络地址 */
	private String address;
	/** 阻止时响应 */
	private Integer status;

	/**
	 * 获取主机地址
	 */
	public String[] getHost() {
		return host;
	}

	/**
	 * 设置主机地址
	 */
	public void setHost(String... values) {
		host = values;
	}

	/**
	 * 获取地址
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * 设置地址
	 */
	public void setAddress(String value) {
		address = value;
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

	/**
	 * 获取阻止状态码
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置阻止状态码
	 */
	public void setStatus(Integer value) {
		status = value;
	}
}
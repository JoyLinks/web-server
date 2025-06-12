package com.joyzl.webserver.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	/** 阻止时响应消息 */
	private String text;
	/** 阻止时响应代码 */
	private Integer status;

	private InetAddress a;

	public InetAddress inetAddress() {
		if (a == null) {
			try {
				a = InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				a = null;
			}
		}
		return a;
	}

	public boolean hasHost() {
		return host == null || host.length == 0;
	}

	/**
	 * 获取主机地址
	 */
	public String[] getHost() {
		return host;
	}

	/**
	 * 设置主机地址，可设置服务名称，主机名称和域名
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
		try {
			a = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			a = null;
		}
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

	public String getText() {
		return text;
	}

	public void setText(String value) {
		text = value;
	}

	@Override
	public String toString() {
		return address;
	}

	@Override
	public int hashCode() {
		return address == null ? super.hashCode() : address.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Address a) {
			if (address == null) {
				return a.address == null;
			}
			if (a.address != null) {
				return address.equals(a.address);
			}
		}
		return false;
	}
}
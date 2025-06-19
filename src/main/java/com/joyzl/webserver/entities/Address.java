package com.joyzl.webserver.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * 地址阻止或允许
 * 
 * @author ZhangXi 2024年11月17日
 */
public class Address {

	/** 允许或阻止 */
	private boolean deny;
	/** 网络地址 */
	private String address;
	/** 阻止时响应消息 */
	private String text;
	/** 阻止时响应代码 */
	private Integer status;
	/** 关联主机 */
	private String[] hosts;

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
		return hosts != null && hosts.length > 0;
	}

	/**
	 * 获取主机地址
	 */
	public String[] getHosts() {
		return hosts;
	}

	/**
	 * 设置主机地址，可设置服务名称，主机名称和域名
	 */
	public void setHosts(String... values) {
		hosts = values;
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
	 * 获取是否黑名单（拒绝）
	 */
	public boolean isDeny() {
		return deny;
	}

	/**
	 * 设置是否黑名单（拒绝）
	 */
	public void setDeny(boolean value) {
		deny = value;
	}

	/**
	 * 设置是否白名单（仅允许）
	 */
	public void setAllow(boolean value) {
		deny = !value;
	}

	/**
	 * 指示是否白名单（仅允许）
	 */
	public boolean theAllow() {
		return !deny;
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
		return (deny ? "O:" : "X:") + address + "=" + inetAddress();
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
			if (deny != a.deny) {
				return false;
			}
			if (address != a.address) {
				if (address != null && a.address != null) {
					if (!address.equals(a.address)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (status != a.status) {
				if (status != null && a.status != null) {
					if (!status.equals(a.status)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (text != a.text) {
				if (text != null && a.text != null) {
					if (!text.equals(a.text)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (hosts != a.hosts) {
				if (hosts != null && a.hosts != null) {
					if (!Arrays.equals(hosts, a.hosts)) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
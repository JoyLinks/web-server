package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Server and Host super class
 * 
 * @author ZhangXi 2024年11月27日
 */
public abstract class Domain {

	private final List<Address> roster = new ArrayList<>();
	private final List<String> servlets = new ArrayList<>();
	private final List<Resource> resources = new ArrayList<>();
	private final List<Authenticate> authenticates = new ArrayList<>();

	/**
	 * 获取服务组包名
	 */
	public List<String> getServlets() {
		return servlets;
	}

	/**
	 * 设置服务组包名
	 */
	public void setServlets(List<String> values) {
		if (values != servlets) {
			servlets.clear();
			servlets.addAll(values);
		}
	}

	/**
	 * 获取资源组
	 */
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * 设置资源组
	 */
	public void setResources(List<Resource> values) {
		if (values != resources) {
			resources.clear();
			resources.addAll(values);
		}
	}

	/**
	 * 获取验证区
	 */
	public List<Authenticate> getAuthenticates() {
		return authenticates;
	}

	/**
	 * 设置验证区
	 */
	public void setAuthenticates(List<Authenticate> values) {
		if (values != authenticates) {
			authenticates.clear();
			authenticates.addAll(values);
		}
	}

	/**
	 * 获取地址组（黑白名单）
	 */
	public List<Address> getRoster() {
		return roster;
	}

	/**
	 * 设置地址组（黑白名单）
	 */
	public void setRoster(List<Address> values) {
		if (values != roster) {
			roster.clear();
			roster.addAll(values);
		}
	}
}
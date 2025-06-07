package com.joyzl.webserver.entities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.joyzl.network.Utility;
import com.joyzl.webserver.service.HostService;
import com.joyzl.webserver.servlet.Wildcards;

/**
 * Server and Host super class
 * 
 * @author ZhangXi 2024年11月27日
 */
public abstract class Domain {

	private String name;
	private String access;

	private final List<Authentication> authorizations = new CopyOnWriteArrayList<>();
	private final List<Servlet> servlets = new CopyOnWriteArrayList<>();

	private HostService service;

	public HostService service() {
		return service;
	}

	public void reset() {
		if (service == null) {
			service = new HostService();
		}

		service.authenticates().clear();
		for (Authentication authentication : authorizations) {
			if (authentication.different()) {
				authentication.reset();
			}
			service.authenticates().add(authentication.service());
		}

		service.servlets().clear();
		for (Servlet servlet : servlets) {
			if (servlet.different()) {
				servlet.reset();
			}
			if (Utility.isEmpty(servlet.getPath())) {
				service.servlets().bind(Wildcards.STAR, servlet.service());
			} else {
				service.servlets().bind(servlet.getPath(), servlet.service());
			}
		}
	}

	/**
	 * 获取服务名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置服务名称
	 */
	public void setName(String value) {
		name = value;
	}

	/**
	 * 获取访问日志存储位置
	 */
	public String getAccess() {
		return access;
	}

	/**
	 * 设置访问日志存储位置，格式为"dir/access.log"
	 */
	public void setAccess(String value) {
		access = value;
	}

	/**
	 * 获取服务集
	 */
	public List<Servlet> getServlets() {
		return servlets;
	}

	/**
	 * 设置服务集
	 */
	public void setServlets(List<Servlet> values) {
		if (values != servlets) {
			servlets.clear();
			servlets.addAll(values);
		}
	}

	/**
	 * 获取验证区
	 */
	public List<Authentication> getAuthentications() {
		return authorizations;
	}

	/**
	 * 设置验证区
	 */
	public void setAuthentications(List<Authentication> values) {
		if (values != authorizations) {
			authorizations.clear();
			authorizations.addAll(values);
		}
	}
}
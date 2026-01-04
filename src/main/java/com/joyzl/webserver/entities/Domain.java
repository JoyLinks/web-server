/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.entities;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.service.HostService;
import com.joyzl.webserver.service.Servlets;
import com.joyzl.webserver.servlet.Wildcards;

/**
 * Server and Host super class
 * 
 * @author ZhangXi 2024年11月27日
 */
public abstract class Domain {

	private String name;
	private String access;

	private final List<Authentication> authorizations = new ArrayList<>();
	private final List<Servlet> servlets = new ArrayList<>();

	/** 服务实例 */
	private HostService service;

	/** 服务实例 */
	public HostService service() {
		return service;
	}

	/** 重置域服务实例 */
	public void reset() throws Exception {
		if (service == null) {
			service = new HostService();
		}

		if (differently()) {
			service.name(name);
			service.access(access);
		}

		service.authenticates().clear();
		for (Authentication authentication : authorizations) {
			if (authentication.differently()) {
				authentication.reset();
			}
			if (authentication.service() != null) {
				service.authenticates().add(authentication.service());
			}
		}

		service.servlets().clear();
		for (Servlet servlet : servlets) {
			if (servlet.differently()) {
				servlet.reset();
			}
			if (servlet.service() != null) {
				if (servlet.getPath() == null) {
					final String path = Servlets.defaultPath(servlet.service());
					if (path != null) {
						service.servlets().bind(path, servlet.service());
					} else {
						service.servlets().bind(Wildcards.STAR, servlet.service());
					}
				} else {
					if (Utility.isEmpty(servlet.getPath())) {
						service.servlets().bind(Wildcards.STAR, servlet.service());
					} else {
						service.servlets().bind(servlet.getPath(), servlet.service());
					}
				}
			}
		}
	}

	/** 检查管理对象参数与服务实例是否不同 */
	public boolean differently() {
		if (service == null) {
			return true;
		}

		if (Utility.equal(name, service.name())) {
		} else {
			return true;
		}

		if (service.logger() == null) {
			if (access != null) {
				return true;
			}
		} else {
			if (access == null) {
				return true;
			}
			if (access.equals(service.logger().getFile())) {
			} else {
				return true;
			}
		}

		return false;
	}

	public void close() throws IOException {
		if (service != null) {
			service.close();
			service = null;
		}
		for (Servlet servlet : servlets) {
			if (servlet.service() != null) {
				if (servlet.service() instanceof Closeable s) {
					s.close();
				}
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

	/**
	 * 比较不含内部集合对象(Authentication,Servlet)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Domain d) {
			return equals(this, d);
		}
		return false;
	}

	/**
	 * 比较域参数是否相同
	 */
	public static boolean equals(Domain a, Domain b) {
		if (a.name != b.name) {
			if (a.name != null && b.name != null) {
				if (!a.name.equals(b.name)) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (a.access != b.access) {
			if (a.access != null && b.access != null) {
				if (!a.access.equals(b.access)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
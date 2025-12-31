/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.entities;

import java.util.HashMap;
import java.util.Map;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.service.Servlets;
import com.joyzl.webserver.servlet.ServletReload;

/**
 * 服务程序
 * 
 * @author ZhangXi 2025年6月6日
 */
public class Servlet {

	/** 服务类型标识 */
	private String type;
	/** 匹配资源路径 */
	private String path;

	/** 附加标头 */
	private final Map<String, String> headers = new HashMap<>();
	/** 配置参数 */
	private final Map<String, String> parameters = new HashMap<>();

	/** 服务程序实例 */
	private com.joyzl.webserver.servlet.Servlet service;

	/** 服务程序实例 */
	public com.joyzl.webserver.servlet.Servlet service() {
		return service;
	}

	/** 重置服务程序实例 */
	public void reset() throws Exception {
		if (differently()) {
			if (type != null) {
				service = Servlets.create(type, path, parameters);
				service.headers().putAll(headers);
			} else {
				service = null;
			}
		}
	}

	/** 检查管理对象参数与服务实例是否不同 */
	public boolean differently() {
		if (service == null) {
			return type != null;
		}
		if (type == null) {
			return true;
		}

		if (Utility.equal(path, service.getPath())) {
		} else {
			return true;
		}

		if (headers.equals(service.headers())) {
		} else {
			return true;
		}

		final ServletReload reload = Servlets.findReload(type);
		if (reload == null) {
			if (service.getClass().getName().equals(type)) {
			} else {
				return true;
			}
		} else {
			if (reload.name().equals(type)) {
				if (reload.differently(service, parameters)) {
					return true;
				}
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * 获取服务类型
	 */
	public String getType() {
		return type;
	}

	/**
	 * 设置服务类型
	 */
	public void setType(String value) {
		type = value;
	}

	/**
	 * 获取资源路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置资源路径
	 */
	public void setPath(String value) {
		path = value;
	}

	/**
	 * 获取附加标头，附加标头将添加到每个响应
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * 设置附加标头，附加标头将添加到每个响应
	 */
	public void setHeaders(Map<String, String> values) {
		if (values != headers) {
			headers.clear();
			headers.putAll(values);
		}
	}

	/**
	 * 获取服务程序设置参数
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * 设置服务程序设置参数
	 */
	public void setParameters(Map<String, String> values) {
		if (values != parameters) {
			parameters.clear();
			parameters.putAll(values);
		}
	}

	@Override
	public String toString() {
		return getType() + " " + getPath();
	}

	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) {
			if (o instanceof Servlet s) {
				if (type != s.type) {
					if (type != null && s.type != null) {
						if (!type.equals(s.type)) {
							return false;
						}
					} else {
						return false;
					}
				}
				if (path != s.path) {
					if (path != null && s.path != null) {
						if (!path.equals(s.path)) {
							return false;
						}
					} else {
						return false;
					}
				}

				if (!parameters.equals(s.parameters)) {
					return false;
				}
				if (!headers.equals(s.headers)) {
					return false;
				}

				return true;
			}
		}
		return false;
	}
}
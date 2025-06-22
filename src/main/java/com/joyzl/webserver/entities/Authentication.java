/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.entities;

import java.util.Arrays;

import com.joyzl.network.Utility;
import com.joyzl.webserver.authenticate.Authenticate;
import com.joyzl.webserver.authenticate.AuthenticateBasic;
import com.joyzl.webserver.authenticate.AuthenticateDeny;
import com.joyzl.webserver.authenticate.AuthenticateDigest;
import com.joyzl.webserver.authenticate.AuthenticateNone;

/**
 * 资源请求身份验证，未明确指定请求方法时则所有方法须验证，反之仅验证指定的方法。
 * 
 * @author ZhangXi 2024年11月27日
 */
public class Authentication {

	private String path;
	private String type;
	private String realm;
	private String algorithm;
	private String[] methods;

	/** 身份验证服务实例 */
	private Authenticate service;

	/** 身份验证服务实例 */
	public Authenticate service() {
		return service;
	}

	/** 重置验证服务实例 */
	public void reset() {
		if (differently()) {
			if (type == null) {
				service = null;
			} else if (AuthenticateDeny.TYPE.equalsIgnoreCase(type)) {
				service = new AuthenticateDeny(path, realm, algorithm, methods);
			} else if (AuthenticateNone.TYPE.equalsIgnoreCase(type)) {
				service = new AuthenticateNone(path, realm, algorithm, methods);
			} else if (AuthenticateBasic.TYPE.equalsIgnoreCase(type)) {
				service = new AuthenticateBasic(path, realm, algorithm, methods);
			} else if (AuthenticateDigest.TYPE.equalsIgnoreCase(type)) {
				service = new AuthenticateDigest(path, realm, algorithm, methods);
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

		if (AuthenticateDeny.TYPE.equalsIgnoreCase(type)) {
			if (service instanceof AuthenticateDeny) {
			} else {
				return true;
			}
		} else if (AuthenticateNone.TYPE.equalsIgnoreCase(type)) {
			if (service instanceof AuthenticateNone) {
			} else {
				return true;
			}
		} else if (AuthenticateBasic.TYPE.equalsIgnoreCase(type)) {
			if (service instanceof AuthenticateBasic) {
			} else {
				return true;
			}
		} else if (AuthenticateDigest.TYPE.equalsIgnoreCase(type)) {
			if (service instanceof AuthenticateDigest) {
			} else {
				return true;
			}
		} else {
			return true;
		}

		if (Utility.isEmpty(path)) {
			if ("/".equals(service.getPath())) {
			} else {
				return true;
			}
		} else {
			if (Utility.same(path, service.getPath())) {
			} else {
				return true;
			}
		}

		if (Utility.same(realm, service.getRealm())) {
		} else {
			return true;
		}

		if (Utility.same(algorithm, service.getAlgorithm())) {
		} else {
			return true;
		}

		if (methods == null) {
			if (service.getMethods() != null) {
				return true;
			}
		} else {
			if (service.getMethods() == null) {
				return true;
			}
			if (methods.length != service.getMethods().length) {
				return true;
			}
			for (int i = 0; i < methods.length; i++) {
				if (Utility.same(methods[i], service.getMethods()[i])) {
					continue;
				} else {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 获取须验证的路径，指定路径及其子路径须身份验证
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置须验证的路径，指定路径及其子路径须身份验证
	 */
	public void setPath(String value) {
		path = value;
	}

	/**
	 * 获取身份验证提示信息
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * 设置身份验证提示信息
	 */
	public void setRealm(String value) {
		realm = value;
	}

	/**
	 * 获取身份验证类型
	 */
	public String getType() {
		return type;
	}

	/**
	 * 设置身份验证类型
	 */
	public void setType(String value) {
		type = value;
	}

	/**
	 * 获取用户信息加密方式
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * 设置用户信息加密方式
	 */
	public void setAlgorithm(String value) {
		algorithm = value;
	}

	/**
	 * 获取须验证的请求方法，如果未指定则所有方法须验证
	 */
	public String[] getMethods() {
		return methods;
	}

	/**
	 * 设置须验证的请求方法，如果未指定则所有方法须验证
	 */
	public void setMethods(String... values) {
		methods = values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Authentication a) {
			if (path != a.path) {
				if (path != null && a.path != null) {
					if (!path.equals(a.path)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (type != a.type) {
				if (type != null && a.type != null) {
					if (!type.equalsIgnoreCase(a.type)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (realm != a.realm) {
				if (realm != null && a.realm != null) {
					if (!realm.equals(a.realm)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (algorithm != a.algorithm) {
				if (algorithm != null && a.algorithm != null) {
					if (!algorithm.equalsIgnoreCase(a.algorithm)) {
						return false;
					}
				} else {
					return false;
				}
			}
			if (methods != a.methods) {
				if (methods != null && a.methods != null) {
					if (!Arrays.equals(methods, a.methods)) {
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
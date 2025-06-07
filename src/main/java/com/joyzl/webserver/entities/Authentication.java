package com.joyzl.webserver.entities;

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

	private Authenticate service;

	public Authenticate service() {
		return service;
	}

	public boolean different() {
		if (service != null) {
			if (type == null) {
				return true;
			} else if (AuthenticateDeny.TYPE.equalsIgnoreCase(type)) {
				if (!(service instanceof AuthenticateDeny)) {
					return true;
				}
			} else if (AuthenticateNone.TYPE.equalsIgnoreCase(type)) {
				if (!(service instanceof AuthenticateNone)) {
					return true;
				}
			} else if (AuthenticateBasic.TYPE.equalsIgnoreCase(type)) {
				if (!(service instanceof AuthenticateBasic)) {
					return true;
				}
			} else if (AuthenticateDigest.TYPE.equalsIgnoreCase(type)) {
				if (!(service instanceof AuthenticateDigest)) {
					return true;
				}
			} else {
				return true;
			}

			if (Utility.isEmpty(path) && "/".equals(service.getPath()) || Utility.same(path, service.getPath())) {
				if (Utility.same(realm, service.getRealm())) {
					if (Utility.same(algorithm, service.getAlgorithm())) {
						if (methods == null) {
							if (service.getMethods() == null) {
								return false;
							}
						} else {
							if (service.getMethods() != null) {
								if (methods.length == service.getMethods().length) {
									for (int i = 0; i < methods.length; i++) {
										if (Utility.same(methods[i], service.getMethods()[i])) {
											continue;
										} else {
											return true;
										}
									}
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void reset() {
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
	 * 获取允许的请求方法，如果未指定则默认允许所有
	 */
	public String[] getMethods() {
		return methods;
	}

	/**
	 * 设置允许的请求方法，如果未指定则默认允许所有
	 */
	public void setMethods(String... values) {
		methods = values;
	}
}
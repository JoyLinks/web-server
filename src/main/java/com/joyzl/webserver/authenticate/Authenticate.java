package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 资源请求者身份验证，未明确指定请求方法时则所有方法须验证，反正仅验证指定的方法。
 * 
 * @author ZhangXi 2024年11月26日
 */
public abstract class Authenticate {

	/** 匹配起始路径 */
	private final String path;
	/** 须验证的方法 */
	private String[] methods;
	/** 加密算法 */
	private String algorithm;
	/** 领域提示 */
	private String realm;

	public Authenticate(String path) {
		if (path == null || path.length() == 0) {
			this.path = "/";
		} else {
			this.path = path;
		}
	}

	/**
	 * 请求方法是否受保护须验证
	 */
	public boolean require(String method) {
		if (methods == null || methods.length == 0) {
			// 未明确指定则所有方法须验证
			return true;
		}
		for (int index = 0; index < methods.length; index++) {
			if (method.equalsIgnoreCase(methods[index])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 验证是否允许请求资源
	 */
	public abstract boolean verify(Request request, Response response);

	public abstract String getType();

	/**
	 * 获取受保护的资源路径，相对于URL根路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 获取受保护资源的验证提示信息
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * 设置受保护资源的验证提示信息
	 */
	public void setRealm(String vlaue) {
		realm = vlaue;
	}

	/**
	 * 获取加密方式，如果密码采用加密存储，必须使用验证方式匹配的加密方式
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * 设置加密方式，例如：MD5 SHA-1 SHA-256
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
		if (values == null) {
			methods = null;
		} else {
			methods = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				methods[i] = HTTP1.METHODS.get(values[i]);
			}
		}
	}
}
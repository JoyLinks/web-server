/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.authenticate;

import java.util.Arrays;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 资源请求者身份验证，未明确指定请求方法时则所有方法须验证，反正仅验证指定的方法。
 * <p>
 * 身份验证在Servlet之前执行，简单请求(GET HEAD)时浏览器可能不事先发送预检(OPTIONS)；
 * 复杂请求(DELETE)时浏览器会事先发送预检(OPTIONS)，如果预检(OPTIONS)请求被身份验证拒绝，浏览器无法继续身份验证而失败；
 * 身份验证失败可能返回有助于继续验证的信息，跨域请求时应提供相应支持的标头。
 * </p>
 * 
 * @author ZhangXi 2024年11月26日
 */
public abstract class Authenticate {

	/** 受保护的资源路径 */
	private String path;
	/** 受保护的请求方法 */
	private String[] methods;
	/** 加密算法 */
	private String algorithm;
	/** 领域提示 */
	private String realm;

	public Authenticate(String path, String realm, String algorithm, String[] methods) {
		if (Utility.isEmpty(path)) {
			path = "/";
		}
		if (Utility.isEmpty(realm)) {
			realm = "JOYZL protection";
		}
		if (methods != null) {
			// 字符串加速处理
			methods = Arrays.copyOf(methods, methods.length);
			for (int i = 0; i < methods.length; i++) {
				methods[i] = HTTP1.METHODS.get(methods[i]);
			}
		}
		this.methods = methods;
		this.algorithm = algorithm;
		this.realm = realm;
		this.path = path;
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

	/**
	 * 验证方式名称
	 */
	public abstract String getType();

	/**
	 * 受保护的资源路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 受保护的请求方法集
	 */
	public String[] getMethods() {
		return methods;
	}

	/**
	 * 受保护资源的领域信息
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * 加密方式，如果密码采用加密存储，必须使用验证方式匹配的加密方式
	 */
	public String getAlgorithm() {
		return algorithm;
	}
}
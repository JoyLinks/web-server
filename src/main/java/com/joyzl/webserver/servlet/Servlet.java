/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.servlet;

import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;

/**
 * 服务程序(Servlet)
 * 
 * @author ZhangXi
 * @date 2021年10月9日
 */
public abstract class Servlet {

	protected final String path;
	protected final String base;

	public Servlet(String path) {
		base = Utility.correctBase(path);
		this.path = path;
	}

	public abstract void service(HTTPSlave chain, Request request, Response response) throws Exception;

	/**
	 * 服务程序名称（日志记录）
	 */
	public String name() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 服务程序基础路径（已修正不含通配符）
	 */
	public String getBase() {
		return base;
	}

	/**
	 * 服务程序绑定路径（原始路径可能含有通配符）
	 */
	public String getPath() {
		return path;
	}
}
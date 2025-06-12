/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.servlet;

import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 服务程序(Servlet)
 * 
 * @author ZhangXi
 * @date 2021年10月9日
 */
public abstract class Servlet {

	/**
	 * 服务程序名称
	 * 
	 * @return 默认返回类名称
	 */
	public String name() {
		return this.getClass().getSimpleName();
	}

	public abstract void service(HTTPSlave chain, Request request, Response response) throws Exception;
}
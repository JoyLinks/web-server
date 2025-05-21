package com.joyzl.webserver.servlets;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.web.CROSServlet;
import com.joyzl.webserver.web.ServletPath;

/**
 * 服务管理接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manager/*")
public class UserServlet extends CROSServlet {

	// 管理 Basic 和 Digict 用户
	protected void get(Request request, Response response) throws Exception {

	}

	protected void put(Request request, Response response) throws Exception {

	}

	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}
}
package com.joyzl.webserver.servlets;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.CROSServlet;

/**
 * 服务管理接口
 * 
 * @author ZhangXi 2024年11月15日
 */
public class ManageServlet extends CROSServlet {

	protected void get(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void put(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void connect(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}
}

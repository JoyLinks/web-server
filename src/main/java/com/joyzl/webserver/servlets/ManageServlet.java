package com.joyzl.webserver.servlets;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.web.CROSServlet;
import com.joyzl.network.web.WEBRequest;
import com.joyzl.network.web.WEBResponse;

/**
 * 服务管理接口
 * 
 * @author ZhangXi 2024年11月15日
 */
public class ManageServlet extends CROSServlet {

	protected void get(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void head(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void post(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void put(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void delete(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void connect(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}
}

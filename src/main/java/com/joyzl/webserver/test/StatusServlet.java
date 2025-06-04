package com.joyzl.webserver.test;

import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBServlet;

@ServletPath(path = "/a5-test/status.cgi")
public class StatusServlet extends WEBServlet {

	protected void get(Request request, Response response) throws Exception {
		response.addHeader(ContentLength.NAME, "0");
		response.setText("This is not a real HTTP status code");
		response.setStatus(678);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void post(Request request, Response response) throws Exception {

	}

	protected void put(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void options(Request request, Response response) throws Exception {

	}
}
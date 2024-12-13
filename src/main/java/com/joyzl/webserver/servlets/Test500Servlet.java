package com.joyzl.webserver.servlets;

import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.network.web.MIMEType;
import com.joyzl.network.web.ServletPath;
import com.joyzl.network.web.WEBServlet;

@ServletPath(path = "/a5-test/500.cgi")
public class Test500Servlet extends WEBServlet {

	protected void get(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
		response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
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
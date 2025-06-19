package com.joyzl.webserver.test;

import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBServlet;

@ServletPath(path = "/a5-test/500.cgi")
public class Test500Servlet extends WEBServlet {

	public Test500Servlet(String path) {
		super(path);
	}

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
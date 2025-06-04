package com.joyzl.webserver.test;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBServlet;

@ServletPath(path = "/a5-test/ls.cgi")
public class ListServlet extends WEBServlet {

	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

		writer.write("drwxr-xr-x limited4/foo ");
		writer.write("drwxr-xr-x WeMustProtectThisHouse! ");
		writer.flush();
		writer.close();
		response.setContent(output.buffer());
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		response.addHeader(ContentType.NAME, MIMEType.TEXT_PLAIN);
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
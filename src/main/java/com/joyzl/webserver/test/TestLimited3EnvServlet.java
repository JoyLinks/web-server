package com.joyzl.webserver.test;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBServlet;

@ServletPath(path = "/a5-test/limited3/env.cgi")
public class TestLimited3EnvServlet extends WEBServlet {

	protected void get(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void post(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

		writer.write("REQUEST_METHOD = ");
		writer.write(request.getMethod());
		writer.write(HTTP1Coder.LF);

		writer.write("REMOTE_USER = ");
		writer.write("bda");
		writer.write(HTTP1Coder.LF);

		if (request.hasParameters()) {
			for (Entry<String, String[]> entry : request.getParametersMap().entrySet()) {
				writer.write(entry.getKey());
				writer.write("=");
				writer.write(String.join(",", entry.getValue()));
				writer.write(HTTP1Coder.LF);
			}
		}
		writer.write(HTTP1Coder.LF);
		writer.flush();

		// TODO 请求参数保持顺序
		// FormDataCoder.writeXWWWForm(request, output.buffer());
		writer.write("var1=foo&var2=bar");

		writer.close();

		response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		response.setContent(output.buffer());
	}

	protected void put(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void options(Request request, Response response) throws Exception {
		response.addHeader(HTTP1.Allow, "OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE");
	}
}
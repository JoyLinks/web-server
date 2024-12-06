package com.joyzl.webserver.servlets;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.network.web.MIMEType;
import com.joyzl.network.web.ServletURI;
import com.joyzl.network.web.WEBServlet;

@ServletURI(uri = "/a5-test/env.cgi")
public class TestEnvServlet extends WEBServlet {

	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

		if (request.hasParameters()) {
			for (Entry<String, String[]> entry : request.getParametersMap().entrySet()) {
				writer.write(entry.getKey());
				writer.write("=");
				writer.write(String.join(",", entry.getValue()));
				writer.write(HTTPCoder.LF);
			}
		}

		writer.write("QUERY_STRING = ");
		writer.write(request.getQuery().substring(1));
		writer.write(HTTPCoder.LF);

		writer.write("HTTP_USER_AGENT = ");
		writer.write(request.getHeader(HTTP.User_Agent));
		writer.write(HTTPCoder.LF);

		writer.close();

		response.setContent(output.buffer());
		response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void post(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

		writer.write("REQUEST_METHOD = ");
		writer.write(request.getMethod());
		writer.write(HTTPCoder.LF);

		writer.write("REMOTE_USER = ");
		writer.write("bda");
		writer.write(HTTPCoder.LF);

		if (request.hasParameters()) {
			for (Entry<String, String[]> entry : request.getParametersMap().entrySet()) {
				writer.write(entry.getKey());
				writer.write("=");
				writer.write(String.join(",", entry.getValue()));
				writer.write(HTTPCoder.LF);
			}
		}
		writer.write(HTTPCoder.LF);
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
		response.addHeader(HTTP.Allow, "OPTIONS, GET, HEAD, POST, TRACE");
	}
}
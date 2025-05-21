package com.joyzl.webserver.servlets;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.web.CROSServlet;
import com.joyzl.webserver.web.MIMEType;
import com.joyzl.webserver.web.ServletPath;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.manage.Manager;
import com.joyzl.webserver.manage.Serializer;

/**
 * 服务管理接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manager/*")
public class ManageServlet extends CROSServlet {

	// 获取计数
	// 获取配置，调整配置，重置服务
	// 黑白名单
	// 访问日志查询
	// 管理 Basic 和 Digict 用户
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntity(Manager.servers(), writer);
		writer.flush();

		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		response.setContent(output.buffer());
	}

	protected void put(Request request, Response response) throws Exception {
		if (response.getContent() != null) {
			final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
			final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
			@SuppressWarnings("unchecked")
			final List<Server> servers = (List<Server>) Serializer.JSON().readEntity(Server.class, reader);
			if (servers != null && servers.size() > 0) {

			}
		}
	}

	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}
}
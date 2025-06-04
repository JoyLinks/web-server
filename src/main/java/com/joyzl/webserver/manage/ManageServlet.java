package com.joyzl.webserver.manage;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.service.Service;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 服务配置接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manager/service")
public class ManageServlet extends CROSServlet {

	// 获取服务配置
	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(Service.all(), writer);
		writer.flush();

		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.setContent(output.buffer());
	}

	// 保存服务配置
	@Override
	protected void put(Request request, Response response) throws Exception {
		if (response.getContent() == null) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final List<Server> servers;
		try {
			final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
			final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
			servers = Serializer.JSON().readEntities(Server.class, reader);
		} catch (Exception e) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		if (servers != null) {

		} else {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
		}
	}

	// 重置服务
	@Override
	protected void post(Request request, Response response) throws Exception {
		// 重置指定Server
		// 重置所有Server
	}
}
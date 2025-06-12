package com.joyzl.webserver.manage;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
import com.joyzl.webserver.service.Services;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 服务配置接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manage/setting")
public class SettingServlet extends CROSServlet {

	public final static String NAME = "SETTING";

	@Override
	public String name() {
		return NAME;
	}

	/**
	 * 获取服务配置
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(Services.all(), writer);
		writer.flush();

		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.setContent(output.buffer());
	}

	/**
	 * 新建服务，已存在则失败
	 */
	@Override
	protected void put(Request request, Response response) throws Exception {
		if (!response.hasContent()) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final Server server;
		try {
			final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
			final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
			server = Serializer.JSON().readEntity(Server.class, reader);
		} catch (Exception e) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		if (server == null) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		final Server old = Services.find(server.getName());
		if (old != null) {
			response.setStatus(HTTPStatus.CONFLICT);
			return;
		}

		Services.add(server);
		server.start();
	}

	/**
	 * 提交配置并重置服务
	 */
	@Override
	protected void post(Request request, Response response) throws Exception {
		// 重置指定Server
		// 重置所有Server
	}

	/**
	 * 删除服务，不存在则失败
	 */
	@Override
	protected void delete(Request request, Response response) throws Exception {
		// 删除指定Server
		// 删除所有Server
	}
}
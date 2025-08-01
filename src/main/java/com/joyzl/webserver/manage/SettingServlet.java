/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

import java.util.List;

import com.joyzl.logger.Logger;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferReader;
import com.joyzl.network.buffer.DataBufferWriter;
import com.joyzl.network.http.CacheControl;
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
@ServletPath(path = "/manage/api/setting")
public class SettingServlet extends CROSServlet {

	public SettingServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "SETTING";
	}

	/**
	 * 获取服务配置
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		if (request.hasContent()) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);

		try {
			final DataBufferWriter writer = new DataBufferWriter();
			Serializer.JSON().writeEntities(Services.all(), writer);
			response.setContent(writer.buffer());
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
	}

	/**
	 * 上载服务配置，服务将被重置
	 */
	@Override
	protected void put(Request request, Response response) throws Exception {
		if (!request.hasContent()) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final List<Server> servers;
		try {
			final DataBufferReader reader = new DataBufferReader((DataBuffer) request.getContent());
			servers = Serializer.JSON().readEntities(Server.class, reader);
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}

		if (servers == null) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		Services.apply(servers);
		Services.save();
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET,PUT,POST,DELETE";
	}

	@Override
	protected String allowHeaders() {
		// 允许Content-Type:application/xml,application/json,因此须列出允许的Content-Type头
		return "*,Content-Type,Authorization,Depth,Destination,If,Lock-Token,Overwrite,Timeout";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
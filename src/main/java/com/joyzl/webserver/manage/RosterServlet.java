/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.joyzl.logger.Logger;
import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.service.Roster;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 黑白名单接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manage/api/roster")
public class RosterServlet extends CROSServlet {

	public RosterServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "ROSTER";
	}

	/**
	 * 获取名单
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(Roster.all(), writer);
		writer.flush();
		response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.setContent(output.buffer());
	}

	/**
	 * 新建名单
	 */
	@Override
	protected void put(Request request, Response response) throws Exception {
		final Address address;
		if (request.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) request.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (address == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(address.getAddress())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		Roster.add(address);
		Roster.save();
		response.setStatus(HTTPStatus.CREATED);
	}

	/**
	 * 修改名单
	 */
	@Override
	protected void post(Request request, Response response) throws Exception {
		final Address address;
		if (request.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) request.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (address == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(address.getAddress())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		final Address old = Roster.get(address.getAddress());
		if (old == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		} else {
			response.setStatus(HTTPStatus.NO_CONTENT);
			Roster.add(address);
			Roster.save();
		}
	}

	/**
	 * 删除名单
	 */
	@Override
	protected void delete(Request request, Response response) throws Exception {
		Address address;
		if (request.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) request.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (address == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(address.getAddress())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		address = Roster.remove(address.getAddress());
		if (address == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
		} else {
			response.setStatus(HTTPStatus.NO_CONTENT);
			Roster.save();
		}
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET,PUT,POST,DELETE";
	}

	@Override
	protected String allowHeaders() {
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
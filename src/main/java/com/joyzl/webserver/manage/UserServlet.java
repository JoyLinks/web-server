/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

import java.util.ArrayList;
import java.util.List;

import com.joyzl.logger.Logger;
import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferReader;
import com.joyzl.network.buffer.DataBufferWriter;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.service.Users;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 用户管理接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manage/api/user")
public class UserServlet extends CROSServlet {

	public UserServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "USER";
	}

	/**
	 * 获取用户
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		// 重建实例避免输出用户密码
		final List<User> users = new ArrayList<>();
		User u;
		for (User user : Users.all()) {
			u = new User();
			u.setEnable(user.isEnable());
			u.setName(user.getName());
			u.setURIs(user.getURIs());
			users.add(u);
		}

		response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);

		final DataBufferWriter writer = new DataBufferWriter();
		Serializer.JSON().writeEntities(users, writer);
		response.setContent(writer.buffer());
	}

	/**
	 * 新建用户
	 */
	@Override
	protected void put(Request request, Response response) throws Exception {
		final User user;
		if (request.hasContent()) {
			try {
				final DataBufferReader reader = new DataBufferReader((DataBuffer) request.getContent());
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (user == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(user.getName())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		final User old = Users.remove(user.getName());
		if (old != null) {
			response.setStatus(HTTPStatus.CONFLICT);
		} else {
			if (Users.add(user)) {
				response.setStatus(HTTPStatus.CREATED);
				Users.save();
			} else {
				response.setStatus(HTTPStatus.CONFLICT);
			}
		}
	}

	/**
	 * 修改用户
	 */
	@Override
	protected void post(Request request, Response response) throws Exception {
		final User user;
		if (request.hasContent()) {
			try {
				final DataBufferReader reader = new DataBufferReader((DataBuffer) request.getContent());
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (user == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(user.getName())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		final User old = Users.remove(user.getName());
		if (old == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}
		if (Users.add(user)) {
			response.setStatus(HTTPStatus.CREATED);
			Users.save();
		} else {
			response.setStatus(HTTPStatus.CONFLICT);
		}
	}

	/**
	 * 删除用户
	 */
	@Override
	protected void delete(Request request, Response response) throws Exception {
		User user;
		if (request.hasContent()) {
			try {
				final DataBufferReader reader = new DataBufferReader((DataBuffer) request.getContent());
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		if (user == null) {
			response.setStatus(HTTPStatus.UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		if (Utility.isEmpty(user.getName())) {
			response.setStatus(HTTPStatus.UNPROCESSABLE_ENTITY);
			return;
		}

		user = Users.remove(user.getName());
		if (user == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
		} else {
			response.setStatus(HTTPStatus.NO_CONTENT);
			Users.save();
		}
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET,PUT,POST,DELETE";
	}

	@Override
	protected String allowHeaders() {
		// 允许Content-Type:application/xml,application/json,因此须列出允许的Content-Type头
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
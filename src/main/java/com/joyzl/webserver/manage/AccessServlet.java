package com.joyzl.webserver.manage;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
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
 * 日志查询接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manager/user")
public class AccessServlet extends CROSServlet {

	// 获取用户
	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(Users.all(), writer);
		writer.flush();

		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.setContent(output.buffer());
	}

	// 新建用户
	@Override
	protected void put(Request request, Response response) throws Exception {
		final User user;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
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

		if (Users.add(user)) {
			response.setStatus(HTTPStatus.CREATED);
			Users.save();
		} else {
			response.setStatus(HTTPStatus.CONFLICT);
		}
	}

	// 修改用户
	@Override
	protected void post(Request request, Response response) throws Exception {
		final User user;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
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

	// 删除用户
	@Override
	protected void delete(Request request, Response response) throws Exception {
		User user;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				user = Serializer.JSON().readEntity(User.class, reader);
			} catch (Exception e) {
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
		return "OPTIONS,GET,PUT,POST,DELETE,TRACE";
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
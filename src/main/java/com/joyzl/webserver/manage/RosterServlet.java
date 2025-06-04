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
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.service.Users;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 黑白名单接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manager/user")
public class RosterServlet extends CROSServlet {

	// 获取地址
	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBufferOutput output = new DataBufferOutput();
		final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(Users.all(), writer);
		writer.flush();

		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		response.setContent(output.buffer());
	}

	// 新建地址
	@Override
	protected void put(Request request, Response response) throws Exception {
		final Address address;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
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

		// if (Users.add(address)) {
		// response.setStatus(HTTPStatus.CREATED);
		// Users.save();
		// } else {
		// response.setStatus(HTTPStatus.CONFLICT);
		// }
	}

	// 修改地址
	@Override
	protected void post(Request request, Response response) throws Exception {
		final Address address;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
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

		// final User old = Users.remove(address.getHost());
		// if (old == null) {
		// response.setStatus(HTTPStatus.NOT_FOUND);
		// return;
		// }
		// if (Users.add(address)) {
		// response.setStatus(HTTPStatus.CREATED);
		// Users.save();
		// } else {
		// response.setStatus(HTTPStatus.CONFLICT);
		// }
	}

	// 删除地址
	@Override
	protected void delete(Request request, Response response) throws Exception {
		Address address;
		if (response.hasContent()) {
			try {
				final DataBufferInput input = new DataBufferInput((DataBuffer) response.getContent());
				final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				address = Serializer.JSON().readEntity(Address.class, reader);
			} catch (Exception e) {
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

		// address = Users.remove(address.getHost());
		// if (address == null) {
		// response.setStatus(HTTPStatus.NOT_FOUND);
		// } else {
		// response.setStatus(HTTPStatus.NO_CONTENT);
		// Users.save();
		// }
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
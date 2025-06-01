package com.joyzl.webserver.authenticate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 身份验证集合
 * 
 * @author ZhangXi 2024年11月29日
 */
public class Authenticates {

	private final List<Authenticate> AUTHENTICATES = new ArrayList<>();

	public boolean check(Request request, Response response) {
		if (AUTHENTICATES.isEmpty()) {
			return true;
		}

		// 身份验证在Servlet之前执行
		// 简单请求(GET HEAD)时浏览器可能不事先发送预检
		// 复杂请求(DELETE)时浏览器会事先发送预检(OPTIONS)
		// 如果预检(OPTIONS)请求被身份验证拒绝，浏览器无法继续身份验证而失败

		// 身份验证失败可能返回有助于继续验证的信息
		// 跨域请求时应提供相应支持的标头

		Authenticate authenticate;
		for (int index = 0; index < AUTHENTICATES.size(); index++) {
			authenticate = AUTHENTICATES.get(index);
			if (request.pathStart(authenticate.getPath())) {
				if (authenticate.allow(request, response)) {
					if (authenticate.getPreflight()) {
						if (HTTP1.OPTIONS.equals(request.getMethod())) {
							// 预检允许
							return true;
						}
					}
					if (authenticate.verify(request, response)) {
						return true;
					}
					if (authenticate.getPreflight()) {
						final String origin = request.getHeader(HTTP1.Origin);
						if (Utility.noEmpty(origin)) {
							// 跨域允许
							response.addHeader(HTTP1.Access_Control_Allow_Origin, origin);
							response.addHeader(HTTP1.Access_Control_Allow_Credentials, "true");
						}
					}
				}
				// 身份验证前提条件失败
				return false;
			}
		}
		// 请求路径无须身份验证
		return true;
	}

	public List<Authenticate> getAuthenticates() {
		return Collections.unmodifiableList(AUTHENTICATES);
	}

	public void setAuthenticates(List<Authenticate> values) {
		if (AUTHENTICATES != values) {
			AUTHENTICATES.clear();
			AUTHENTICATES.addAll(values);
		}
	}

	public void addAuthenticate(Authenticate value) {
		AUTHENTICATES.add(value);
	}

	public void removeAuthenticate(Authenticate value) {
		AUTHENTICATES.remove(value);
	}

	public void clear() {
		AUTHENTICATES.clear();
	}
}
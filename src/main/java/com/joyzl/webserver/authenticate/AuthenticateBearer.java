package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * Bearer 身份验证 RFC 6750
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateBearer extends Authenticate {

	public final static String TYPE = "Bearer";

	public AuthenticateBearer(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, algorithm, methods);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		return false;
	}

	boolean check(String token) {
		// 向第三方请求验证令牌
		return false;
	}
}
package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * 拒绝请求，既身份验证无条件拒绝
 * 
 * @author ZhangXi 2025年6月4日
 */
public class AuthenticateDeny extends Authenticate {

	public final static String TYPE = "Deny";

	private final String www;

	public AuthenticateDeny(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, algorithm, methods);
		www = TYPE + " realm=\"" + realm + "\"";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		response.setStatus(HTTPStatus.FORBIDDEN);
		response.addHeader(WWWAuthenticate.NAME, www);
		return false;
	}
}
package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 拒绝请求，既身份验证无条件拒绝
 * 
 * @author ZhangXi 2025年6月4日
 */
public class AuthenticateDeny extends Authenticate {

	public final static String TYPE = "Deny";

	public AuthenticateDeny(String path) {
		super(path);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		return false;
	}
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * Mutual 身份验证 RFC 8120
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateMutual extends Authenticate {

	public final static String TYPE = "Mutual";

	private final String www;

	public AuthenticateMutual(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, algorithm, methods);
		www = TYPE + " realm=\"" + realm + "\"";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// TODO
		response.setStatus(HTTPStatus.UNAUTHORIZED);
		response.addHeader(WWWAuthenticate.NAME, www);
		return false;
	}
}
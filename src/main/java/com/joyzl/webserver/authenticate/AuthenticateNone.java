/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 无须身份验证，既无条件通过
 * 
 * @author ZhangXi 2025年6月4日
 */
public class AuthenticateNone extends Authenticate {

	public final static String TYPE = "None";

	public AuthenticateNone(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, algorithm, methods);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		return true;
	}
}
package com.joyzl.webserver.authenticate;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * Bearer 身份验证
 * 
 * <pre>
 * RFC6749 The OAuth 2.0 Authorization Framework
 * RFC6750 The OAuth 2.0 Authorization Framework: Bearer Token Usage
 * </pre>
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

	/*-
	 * 请求受保护资源的方式
	 * 
	 * 1. GET HEAD
	 * 
	 * GET /resource HTTP/1.1
	 * Host: server.example.com
	 * Authorization: Bearer mF_9.B5f-4.1JqM
	 * 
	 * 2. POST PARAMETER
	 * 
	 * POST /resource HTTP/1.1
	 * Host: server.example.com
	 * Content-Type: application/x-www-form-urlencoded
	 * 
	 * access_token=mF_9.B5f-4.1JqM
	 * 
	 * 3. GET URL
	 * 
	 * GET /resource?access_token=mF_9.B5f-4.1JqM HTTP/1.1
	 * Host: server.example.com
	 * 
	 * 拒绝时响应
	 * 
	 * HTTP/1.1 401 Unauthorized
	 * WWW-Authenticate: Bearer realm="example"
	 * 
	 * HTTP/1.1 401 Unauthorized
	 * WWW-Authenticate: Bearer realm="example",
	 *                   error="invalid_token",
	 *                   error_description="The access token expired"
	 * error:
	 * invalid_request	  400 Bad Request
	 * invalid_token      401 Unauthorized
	 * insufficient_scope 403 Forbidden
	 * 
	 * 获取令牌响应
	 * 
	 * HTTP/1.1 200 OK
	 * Content-Type: application/json;charset=UTF-8
	 * Cache-Control: no-store
	 * Pragma: no-cache
	 * 
	 * {
	 * 		"access_token":"mF_9.B5f-4.1JqM",
	 * 		"token_type":"Bearer",
	 * 		"expires_in":3600,
	 * 		"refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA"
	 * }
	 * 
	 * 请求获取令牌
	 * 
	 * POST /token HTTP/1.1
	 * Host: server.example.com
	 * Content-Type: application/x-www-form-urlencoded
	 * 
	 * grant_type=refresh_token&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA
	 * &client_id=s6BhdRkqt3&client_secret=7Fjfp0ZBr1KtDRbnfVdmIw
	 */

	@Override
	public boolean verify(Request request, Response response) {
		return false;
	}

	boolean check(String token) {
		// 向第三方请求验证令牌
		return false;
	}
}
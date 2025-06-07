package com.joyzl.webserver.authenticate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.WWWAuthenticate;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.service.Users;

/**
 * Basic 身份验证 RFC 7617
 * <p>
 * 用户名明文传输，客户端密码可采用指定的摘要加密方式传输或明文传输。如果指定了密码加密方式，则浏览器无法提供请求时支持，应用于客户端程序请求场景。
 * </p>
 * 
 * <pre>
 * Authorization: Basic Base64(name:password)
 * Authorization: Basic Base64(name:MD5(password))
 * </pre>
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateBasic extends Authenticate {

	public final static String TYPE = "Basic";

	private final String www;

	public AuthenticateBasic(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, algorithm, methods);
		www = TYPE + " realm=\"" + realm + "\"";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
		// Authorization: Basic name:password

		String a = request.getHeader(Authorization.NAME);
		if (a != null && a.length() > 6) {
			if (a.startsWith(TYPE)) {
				a = a.substring(TYPE.length() + 1);
				a = new String(Base64.getDecoder().decode(a), StandardCharsets.UTF_8);
				int colon = a.indexOf(HTTP1Coder.COLON);
				if (colon > 0) {
					final User user = Users.get(a.substring(0, colon));
					if (Users.check(request, user)) {
						if (user.getPassword() != null) {
							a = a.substring(colon + 1);
							if (getAlgorithm() == null) {
								// 未指定密码加密方式，采用明文密码
								if (a.contains(user.getPassword())) {
									return true;
								}
							} else {
								// 客户端按指定加密方式传递密码
								if (Utility.equal(a, encrypt(user.getPassword()))) {
									return true;
								}
							}
						}
					}
				}
			}
		}

		// 验证未通过
		wwwAuthenticate(response);
		return false;
	}

	/**
	 * 响应 WWW-Authenticate
	 */
	private void wwwAuthenticate(Response response) {
		// 验证未通过
		response.setStatus(HTTPStatus.UNAUTHORIZED);
		response.addHeader(WWWAuthenticate.NAME, www);
		// TEST cs531a5
		// response.addHeader(ContentType.NAME, "text/html");
		// response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
	}

	private String encrypt(String password) {
		try {
			final MessageDigest md = MessageDigest.getInstance(getAlgorithm());
			md.update(password.getBytes(StandardCharsets.UTF_8));
			return Utility.hex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
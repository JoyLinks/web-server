package com.joyzl.webserver.authenticate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.manage.Users;

/**
 * Digest 身份验证 RFC 7616
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateDigest extends Authenticate {

	public final static String TYPE = "Digest";
	final static byte[] COLON = new byte[] { ':' };
	/** 完整保护 qop="auth-int" */
	final static String AUTH_INT = "auth-int";
	/** 身份验证 qop="auth" */
	final static String AUTH = "auth";

	public AuthenticateDigest(String path) {
		super(path);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// Authorization: Digest realm="",response="",username="",cnonce="",nc=
		// Authorization: Digest
		// realm="Colonial Place",
		// response="4b92e1b584783d2a234ffc1786a32e86"
		// username="mln",
		// uri="http://192.168.2.19/a4-test/limited2/foo/bar.txt",
		// cnonce="014a54548c61ba03827ef6a4dc2f7b4c",
		// qop=auth,
		// nc=00000001,
		// userhash,
		// nonce="",
		// 不带引号:algorithm qop nc stale userhash

		final Parameter parameter = parseParameter(request.getHeader(Authorization.NAME));
		if (parameter != null) {
			// 校验提示信息
			if (Utility.equal(parameter.realm, getRealm())) {
				// 校验随机数
				if (check(parameter.nonce, parameter.opaque)) {
					// 校验用户
					if (Utility.noEmpty(parameter.username)) {
						final User user = Users.get(parameter.username);
						if (user != null && user.isEnable()) {
							if (Users.check(request, user)) {
								if (parameter.qop == null || AUTH.equalsIgnoreCase(parameter.qop)) {
									final String rspauth = auth(request, parameter, user.getPassword());
									if (rspauth != null) {
										authenticationInfo(response, parameter, rspauth);
										return true;
									}
								} else {
									// 暂未支持 auth-int
									// response=MD5(H(username:realm:passwd):nonce:nc:cnonce:qop:H(Method:request-uri:H(entity-body)))
								}
							}
						}
					}
				}
			}
		}

		wwwAuthenticate(response);
		return false;
	}

	/** 解析请求头参数 */
	private Parameter parseParameter(String a) {
		if (a != null && a.length() > 64) {
			if (a.startsWith(TYPE)) {
				final Parameter parameter = new Parameter();
				// 解析参数
				int end, equal, start = TYPE.length() + 1;
				do {
					equal = a.indexOf(HTTP1Coder.EQUAL, start);
					if (equal > 0) {
						end = a.indexOf(HTTP1Coder.COMMA, equal + 1);
						if (end < 0) {
							end = a.length();
						}
						if (equal("realm", a, start, equal)) {
							parameter.realm = a.substring(equal + 2, end - 1);
						} else if (equal("response", a, start, equal)) {
							parameter.respons = a.substring(equal + 2, end - 1);
						} else if (equal("username", a, start, equal)) {
							parameter.username = a.substring(equal + 2, end - 1);
						} else if (equal("cnonce", a, start, equal)) {
							parameter.cnonce = a.substring(equal + 2, end - 1);
						} else if (equal("opaque", a, start, equal)) {
							parameter.opaque = a.substring(equal + 2, end - 1);
						} else if (equal("nonce", a, start, equal)) {
							parameter.nonce = a.substring(equal + 2, end - 1);
						} else if (equal("qop", a, start, equal)) {
							parameter.qop = a.substring(equal + 1, end);
						} else if (equal("nc", a, start, equal)) {
							parameter.nc = a.substring(equal + 1, end);
						}
						start = end + 1;
						while (start < a.length()) {
							if (Character.isWhitespace(a.charAt(start))) {
								start++;
								continue;
							}
							break;
						}
					} else {
						break;
					}
				} while (start < a.length());

				// System.out.println("realm=" + realm);
				// System.out.println("response=" + respons);
				// System.out.println("username=" + username);
				// System.out.println("cnonce=" + cnonce);
				// System.out.println("opaque=" + opaque);
				// System.out.println("nonce=" + nonce);
				// System.out.println("qop=" + qop);
				// System.out.println("nc=" + nc);

				return parameter;
			}
		}
		return null;
	}

	/**
	 * 检查随机数，随机数由纳秒和时间戳组的36进制字符成 "nano|time" ，其中nano的长度由opaque参数除2获得
	 * 
	 * @param nonce
	 * @param opaque
	 * @return
	 */
	private boolean check(String nonce, String opaque) {
		if (nonce != null && opaque != null) {
			int start = opaque.length() / 2;
			long time = Long.parseUnsignedLong(nonce, start, nonce.length(), Character.MAX_RADIX);
			long current = System.currentTimeMillis();
			if (time < current && current - time < 60000) {
				return true;
			}
		} else {
			// 暂未校验
			return true;
		}
		return false;
	}

	/** 身份验证 qop="auth" 并返回 rspauth */
	private String auth(Request request, Parameter parameter, String password) {
		if (Utility.isEmpty(parameter.respons)) {
			return null;
		}
		if (Utility.isEmpty(parameter.cnonce)) {
			return null;
		}
		if (Utility.isEmpty(parameter.nonce)) {
			return null;
		}
		if (Utility.isEmpty(parameter.nc)) {
			return null;
		}

		// response=MD5(MD5(A1):nonce:nc:cnonce:qop:MD5(A2))
		// A1=username:realm:passwd
		// A2=Method:request-uri

		try {
			final MessageDigest md;
			if (getAlgorithm() == null) {
				// 未指定加密算法，采用默认MD5
				md = MessageDigest.getInstance("MD5");
			} else {
				// 指定加密算法，密码加密存储
				md = MessageDigest.getInstance(getAlgorithm());
			}

			// A1
			// 由于机制限制密码必须连同 name:realm:password 加密
			md.update(parameter.username.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(getRealm().getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(password.getBytes(StandardCharsets.UTF_8));
			password = Utility.hex(md.digest());

			// A2
			md.reset();
			md.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(request.getURL().getBytes(StandardCharsets.UTF_8));
			String A2 = Utility.hex(md.digest());

			// RESPONSE
			md.reset();
			md.update(password.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(parameter.nonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(parameter.nc.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(parameter.cnonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(AUTH.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(A2.getBytes(StandardCharsets.UTF_8));

			if (Utility.same(Utility.hex(md.digest()), parameter.respons)) {
				// A2
				md.reset();
				md.update(COLON);
				md.update(request.getURL().getBytes(StandardCharsets.UTF_8));
				A2 = Utility.hex(md.digest());
				// RSPAUTH
				md.reset();
				md.update(password.getBytes(StandardCharsets.UTF_8));
				md.update(COLON);
				md.update(parameter.nonce.getBytes(StandardCharsets.UTF_8));
				md.update(COLON);
				md.update(parameter.nc.getBytes(StandardCharsets.UTF_8));
				md.update(COLON);
				md.update(parameter.cnonce.getBytes(StandardCharsets.UTF_8));
				md.update(COLON);
				md.update(AUTH.getBytes(StandardCharsets.UTF_8));
				md.update(COLON);
				md.update(A2.getBytes(StandardCharsets.UTF_8));
				return Utility.hex(md.digest());
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * 响应 Authentication-Info
	 */
	private void authenticationInfo(Response response, Parameter parameter, String rspauth) {
		final String nano = Long.toUnsignedString(System.nanoTime(), Character.MAX_RADIX);
		final String time = Long.toUnsignedString(System.currentTimeMillis(), Character.MAX_RADIX);
		final StringBuilder builder = new StringBuilder();

		builder.append(TYPE);
		// realm
		builder.append(" realm=\"");
		builder.append(getRealm());
		builder.append("\"");
		// nextnonce
		builder.append(",nextnonce=\"");
		builder.append(nano);
		builder.append(time);
		builder.append("\"");
		// qop
		builder.append(",qop=");
		builder.append(AUTH);
		// rspauth
		builder.append(",rspauth=\"");
		builder.append(rspauth);
		builder.append("\"");
		// cnonce
		builder.append(",cnonce=\"");
		builder.append(parameter.cnonce);
		builder.append("\"");
		// nc
		builder.append(",nc=");
		builder.append(parameter.nc);
		response.addHeader(HTTP1.Authentication_Info, builder.toString());
	}

	/**
	 * 响应 WWW-Authenticate
	 */
	private void wwwAuthenticate(Response response) {
		response.setStatus(HTTPStatus.UNAUTHORIZED);

		final String nano = Long.toUnsignedString(System.nanoTime(), Character.MAX_RADIX);
		final String time = Long.toUnsignedString(System.currentTimeMillis(), Character.MAX_RADIX);
		final StringBuilder builder = new StringBuilder();

		builder.append(TYPE);
		// realm
		builder.append(" realm=\"");
		builder.append(getRealm());
		builder.append("\"");
		// domain
		builder.append(",domain=\"");
		builder.append(getPath());
		builder.append("\"");
		// nonce
		builder.append(",nonce=\"");
		builder.append(nano);
		builder.append(time);
		builder.append("\"");
		// opaque
		builder.append(",opaque=\"");
		int size = nano.length() * 2;
		final Random random = new Random();
		while (size-- > 0) {
			builder.append(Character.forDigit(random.nextInt(Character.MAX_RADIX), Character.MAX_RADIX));
		}
		builder.append("\"");
		// algorithm
		if (getAlgorithm() != null) {
			builder.append(",algorithm=");
			builder.append(getAlgorithm());
		}
		// qop
		builder.append(",qop=");
		builder.append(AUTH);
		// stale 不设置，服务端始终设置true
		// charset 不设置，默认UTF-8
		// userhash 不设置，默认 false
		response.addHeader(HTTP1.WWW_Authenticate, builder.toString());
		// TEST cs531a5
		response.addHeader(ContentType.NAME, "text/html");
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
	}

	private boolean equal(String a, CharSequence b, int start, int done) {
		if (done - start == a.length() && done <= b.length()) {
			for (int index = 0; index < a.length(); index++, start++) {
				if (a.charAt(index) != b.charAt(start)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private class Parameter {
		String respons = null;
		String username = null;
		String cnonce = null;
		String nc = null;

		String realm = null;
		String qop = null;
		String nonce = null;
		String opaque = null;
	}
}
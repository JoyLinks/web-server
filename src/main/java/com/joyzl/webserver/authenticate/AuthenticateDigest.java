package com.joyzl.webserver.authenticate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.service.Users;

/**
 * Digest 身份验证 RFC7616
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateDigest extends Authenticate {

	public final static String TYPE = "Digest";
	public final static String SHA_512_256_NAME = "SHA-512-256";
	public final static String SHA_512_256 = "SHA-512/256";
	public final static String SHA_256 = "SHA-256";
	public final static String MD5 = "MD5";

	final static byte COLON = HTTP1Coder.COLON;
	/** 随机数有效时间 1h */
	final static int TIMEOUT = 1000 * 60 * 60;
	/** 完整保护 qop="auth-int" */
	final static String AUTH_INT = "auth-int";
	/** 身份验证 qop="auth" */
	final static String AUTH = "auth";

	/** 领域字符串的字节形态 */
	private final byte[] REALM;

	public AuthenticateDigest(String path, String realm, String algorithm, String[] methods) {
		super(path, realm, expedite(algorithm), methods);
		REALM = getRealm().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		final Authorization authorization = Authorization.parse(request.getHeader(Authorization.NAME));
		if (authorization != null) {
			// 校验提示信息
			if (Utility.equal(authorization.getValue("realm"), getRealm())) {
				// 校验用户
				final User user = findUser(authorization);
				if (Users.check(request, user)) {
					// 校验随机数
					String opaque = authorization.getValue("opaque");
					String nonce = authorization.getValue("nonce");
					if (check(nonce, opaque)) {
						String cnonce = authorization.getValue("cnonce");
						String qop = authorization.getValue("qop");
						String nc = authorization.getValue("nc");
						String rspauth = authenticate(request, authorization, user, cnonce, nonce, qop, nc);
						if (rspauth != null) {
							authenticationInfo(response, cnonce, qop, nc, rspauth, false);
							return true;
						}
					}
					// 用户成功随机数失败
					wwwAuthenticate(response, true);
					return false;
				}
			}
		}
		wwwAuthenticate(response, false);
		return false;
	}

	/**
	 * 检查并获取用户
	 */
	private User findUser(Authorization authorization) {
		String userhash = authorization.getValue("userhash");
		String username = authorization.getValue("username");
		if (Utility.equal(userhash, "true")) {
			// username=H(unq(username)":"unq(realm))
		} else {
			if (username == null) {
				username = authorization.getValue("username*");
				if (username == null) {
					return null;
				}
				// "UTF-8''J%C3%A4s%C3%B8n%20Doe"
				int index = username.indexOf("''");
				if (index > 0) {
					try {
						username = URLDecoder.decode(username.substring(index + 2), username.substring(0, index));
					} catch (UnsupportedEncodingException e) {
						return null;
					}
				}
			}
			return Users.get(username);
		}
		return null;
	}

	/**
	 * 检查随机数，随机数由纳秒和时间戳组的36进制字符成 "nano|time" ，其中nano的长度由opaque参数除2获得
	 * 
	 * <pre>
	 * 注意：本实现未遵循建议
	 * Base64 (timestamp H(timestamp ":" ETag ":" secret-data))
	 * </pre>
	 * 
	 * @param nonce
	 * @param opaque
	 * @return
	 */
	private boolean check(String nonce, String opaque) {
		if (nonce != null && opaque != null) {
			int start = opaque.length() / 2;
			long time = Long.parseUnsignedLong(nonce, start, nonce.length(), Character.MAX_RADIX);
			if (System.currentTimeMillis() - time < TIMEOUT) {
				return true;
			}
		}
		return false;
	}

	/** 身份验证 qop="auth" 并返回 rspauth */
	private String authenticate(Request request, Authorization a, User user, String cnonce, String nonce, String qop, String nc) {
		final String algorithm = a.getValue("algorithm");
		final String response = a.getValue("response");
		final String uri = a.getValue("uri");

		if (Utility.isEmpty(response)) {
			return null;
		}
		if (Utility.isEmpty(cnonce)) {
			return null;
		}
		if (Utility.isEmpty(nonce)) {
			return null;
		}
		if (Utility.isEmpty(uri)) {
			return null;
		}
		if (Utility.isEmpty(nc)) {
			return null;
		}

		final MessageDigest md;
		try {
			if (Utility.isEmpty(algorithm)) {
				md = MessageDigest.getInstance(MD5);
			} else if (Utility.same(SHA_256, algorithm)) {
				md = MessageDigest.getInstance(SHA_256);
			} else if (Utility.same(SHA_512_256_NAME, algorithm)) {
				md = MessageDigest.getInstance(SHA_512_256);
			} else if (Utility.same(MD5, algorithm)) {
				md = MessageDigest.getInstance(MD5);
			} else {
				return null;
			}
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		// response=MD5(MD5(A1):nonce:nc:cnonce:qop:MD5(A2))
		// rspauth=MD5(MD5(A1):nonce:nc:cnonce:qop:MD5(A3))

		// qop="auth"
		// A1=username:realm:passwd
		// A2=method:request-uri
		// A3=:request-uri

		// qop="auth-int"
		// A1=username:realm:passwd
		// A2=method:request-uri:MD5(entity-body)
		// A3=:request-uri:MD5(entity-body)

		// H(A1)
		md.update(user.getName().getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(REALM);
		md.update(COLON);
		if (Utility.noEmpty(user.getPassword())) {
			md.update(user.getPassword().getBytes(StandardCharsets.UTF_8));
		}
		final String A1 = Utility.hex(md.digest());

		final byte[] A1Bytes = A1.getBytes(StandardCharsets.UTF_8);
		final byte[] uriBytes = uri.getBytes(StandardCharsets.UTF_8);
		final byte[] cnonceBytes = cnonce.getBytes(StandardCharsets.UTF_8);
		final byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);
		final byte[] qopBytes = qop.getBytes(StandardCharsets.UTF_8);
		final byte[] ncBytes = nc.getBytes(StandardCharsets.UTF_8);

		String A3;
		if (Utility.isEmpty(qop) || "auth".equalsIgnoreCase(qop)) {
			// H(A2)
			md.reset();
			md.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(uriBytes);
			String A2 = Utility.hex(md.digest());
			// response
			md.reset();
			md.update(A1Bytes);
			md.update(COLON);
			md.update(nonceBytes);
			md.update(COLON);
			md.update(ncBytes);
			md.update(COLON);
			md.update(cnonceBytes);
			md.update(COLON);
			md.update(qopBytes);
			md.update(COLON);
			md.update(A2.getBytes(StandardCharsets.UTF_8));
			if (Utility.same(Utility.hex(md.digest()), response)) {
				md.reset();
				md.update(COLON);
				md.update(uriBytes);
				A3 = Utility.hex(md.digest());
			} else {
				return null;
			}
		} else if ("auth-int".equalsIgnoreCase(qop)) {
			// H(BODY)
			md.reset();
			final DataBuffer content = (DataBuffer) request.getContent();
			DataBufferUnit unit = content.head();
			do {
				unit.buffer().mark();
				md.update(unit.buffer());
				unit.buffer().reset();
				unit = unit.next();
			} while (unit != null);
			String A2 = Utility.hex(md.digest());
			final byte[] hbodyBytes = A2.getBytes(StandardCharsets.UTF_8);
			// H(A2)
			md.reset();
			md.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(uriBytes);
			md.update(COLON);
			md.update(hbodyBytes);
			A2 = Utility.hex(md.digest());
			// response
			md.reset();
			md.update(A1Bytes);
			md.update(COLON);
			md.update(nonceBytes);
			md.update(COLON);
			md.update(ncBytes);
			md.update(COLON);
			md.update(cnonceBytes);
			md.update(COLON);
			md.update(qopBytes);
			md.update(COLON);
			md.update(A2.getBytes(StandardCharsets.UTF_8));
			if (Utility.same(Utility.hex(md.digest()), response)) {
				md.reset();
				md.update(COLON);
				md.update(uriBytes);
				md.update(COLON);
				md.update(hbodyBytes);
				A3 = Utility.hex(md.digest());
			} else {
				return null;
			}
		} else {
			return null;
		}

		// rspauth
		md.reset();
		md.update(A1Bytes);
		md.update(COLON);
		md.update(nonceBytes);
		md.update(COLON);
		md.update(ncBytes);
		md.update(COLON);
		md.update(cnonceBytes);
		md.update(COLON);
		md.update(qopBytes);
		md.update(COLON);
		md.update(A3.getBytes(StandardCharsets.UTF_8));
		return Utility.hex(md.digest());
	}

	/**
	 * 响应 Authentication-Info
	 */
	private void authenticationInfo(Response response, String cnonce, String qop, String nc, String rspauth, boolean nextnonce) {
		final StringBuilder builder = new StringBuilder();
		// Digest
		builder.append(TYPE);
		// realm
		builder.append(" realm=\"");
		builder.append(getRealm());
		builder.append("\"");

		if (nextnonce) {
			// nextnonce
			// 此参数可能导致无法流水线请求
			final String nano = Long.toUnsignedString(System.nanoTime(), Character.MAX_RADIX);
			final String time = Long.toUnsignedString(System.currentTimeMillis(), Character.MAX_RADIX);
			builder.append(",nextnonce=\"");
			builder.append(nano);
			builder.append(time);
			builder.append("\"");
		}

		// cnonce
		builder.append(",cnonce=\"");
		builder.append(cnonce);
		builder.append("\"");
		// nc
		builder.append(",nc=");
		builder.append(nc);
		// qop
		builder.append(",qop=");
		builder.append(qop);

		if (rspauth != null) {
			// rspauth
			builder.append(",rspauth=\"");
			builder.append(rspauth);
			builder.append("\"");
		}

		response.addHeader(HTTP1.Authentication_Info, builder.toString());
	}

	/**
	 * 响应 WWW-Authenticate,stale指示用户名密码是否有效
	 */
	private void wwwAuthenticate(Response response, boolean stale) {
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
		long n;
		while (size-- > 0) {
			n = System.nanoTime() / 100;
			if ((n & 1) == 0) {
				// 0~9
				builder.append((char) (48 + n % 10));
			} else {
				// A~Z
				builder.append((char) (65 + n % 26));
			}
		}
		builder.append("\"");
		// algorithm="MD5" / "SHA-256" / "SHA-512/256"
		if (getAlgorithm() != null) {
			builder.append(",algorithm=");
			builder.append(getAlgorithm());
		}
		// qop
		builder.append(",qop=\"auth,auth-int\"");

		// stale
		if (stale) {
			// 用户名密码有效，无须重新提示用户输入
			builder.append(",stale=true");
		} else {
			// 用户名密码无效，需要重新提示用户输入
			builder.append(",stale=false");
		}

		// userhash=false 用户名是否哈希
		builder.append(",userhash=false");

		// charset="UTF-8" 默认

		response.addHeader(HTTP1.WWW_Authenticate, builder.toString());

		// TEST cs531a5
		// response.addHeader(ContentType.NAME, "text/html");
		// response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
	}

	/** 字符串实例加速 */
	private static String expedite(String value) {
		if (Utility.noEmpty(value)) {
			if (Utility.same(SHA_512_256_NAME, value)) {
				value = SHA_512_256_NAME;
			} else if (Utility.same(SHA_512_256, value)) {
				value = SHA_512_256_NAME;
			} else if (Utility.same(SHA_256, value)) {
				value = SHA_256;
			} else if (Utility.same(MD5, value)) {
				value = MD5;
			} else {
				value = SHA_256;
			}
		}
		return value;
	}
}
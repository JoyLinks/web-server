/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * https://www.rfc-editor.org/rfc/rfc7616#section-3.9
 * 
 * @author ZhangXi 2025年5月29日
 */
public class TestAuthorizationDigest {

	// WWW-Authenticate
	final static String WWA1 = """
			Digest realm="http-auth@example.org",
			qop="auth, auth-int",
			algorithm=SHA-256,
			nonce="7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v",
			opaque="FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS"
			""";
	final static String WWA2 = """
			Digest realm="http-auth@example.org",
			qop="auth, auth-int",
			algorithm=MD5,
			nonce="7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v",
			opaque="FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS"
			""";
	final static String WWW3 = """
			Digest realm="api@example.org",
			qop="auth",
			algorithm=SHA-512-256,
			nonce="5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK",
			opaque="HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS",
			charset=UTF-8,
			userhash=true""";

	// Authorization
	final static String AUTH1 = """
			Digest username="Mufasa",
			realm="http-auth@example.org",
			uri="/dir/index.html",
			algorithm=MD5,
			nonce="7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v",
			nc=00000001,
			cnonce="f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ",
			qop=auth,
			response="8ca523f5e9506fed4657c9700eebdbec",
			opaque="FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS"
			""";
	final static String AUTH2 = """
			Digest username="Mufasa",
			realm="http-auth@example.org",
			uri="/dir/index.html",
			algorithm=SHA-256,
			nonce="7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v",
			nc=00000001,
			cnonce="f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ",
			qop=auth,
			response="753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1",
			opaque="FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS"
			""";
	final static String AUTH3 = """
			Digest username="488869477bf257147b804c45308cd62ac4e25eb717b12b298c79e62dcea254ec",
			realm="api@example.org",
			uri="/doe.json",
			algorithm=SHA-512-256,
			nonce="5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK",
			nc=00000001,
			cnonce="NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v",
			qop=auth,
			response="3798d4131c277846293534c3edc11bd8a5e4cdcbff78b05db9d95eeb1cec68a5",
			opaque="HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS",
			userhash=true
			""";
	final static String AUTH4 = """
			Digest username*=UTF-8''J%C3%A4s%C3%B8n%20Doe,
			realm="api@example.org",
			uri="/doe.json",
			algorithm=SHA-512-256,
			nonce="5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK",
			nc=00000001,
			cnonce="NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v",
			qop=auth,
			response="3798d4131c277846293534c3edc11bd8a5e4cdcbff78b05db9d95eeb1cec68a5",
			opaque="HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS",
			userhash=false
			""";

	@Test
	void testWWWAuthenticate() {
		final WWWAuthenticate a = new WWWAuthenticate();
		a.setHeaderValue(WWA1);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("realm"), "http-auth@example.org");
		assertEquals(a.getValue("qop"), "auth, auth-int");
		assertEquals(a.getValue("algorithm"), "SHA-256");
		assertEquals(a.getValue("nonce"), "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v");
		assertEquals(a.getValue("opaque"), "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS");

		a.setHeaderValue(WWA2);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("realm"), "http-auth@example.org");
		assertEquals(a.getValue("qop"), "auth, auth-int");
		assertEquals(a.getValue("algorithm"), "MD5");
		assertEquals(a.getValue("nonce"), "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v");
		assertEquals(a.getValue("opaque"), "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS");

		a.setHeaderValue(WWW3);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("realm"), "api@example.org");
		assertEquals(a.getValue("qop"), "auth");
		assertEquals(a.getValue("algorithm"), "SHA-512-256");
		assertEquals(a.getValue("nonce"), "5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK");
		assertEquals(a.getValue("opaque"), "HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS");
		assertEquals(a.getValue("charset"), "UTF-8");
		assertEquals(a.getValue("userhash"), "true");
	}

	@Test
	void testAuthorization() {
		final Authorization a = new Authorization();
		a.setHeaderValue(AUTH1);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("username"), "Mufasa");
		assertEquals(a.getValue("realm"), "http-auth@example.org");
		assertEquals(a.getValue("uri"), "/dir/index.html");
		assertEquals(a.getValue("algorithm"), "MD5");
		assertEquals(a.getValue("nonce"), "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v");
		assertEquals(a.getValue("algorithm"), "MD5");
		assertEquals(a.getValue("nc"), "00000001");
		assertEquals(a.getValue("cnonce"), "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ");
		assertEquals(a.getValue("qop"), "auth");
		assertEquals(a.getValue("response"), "8ca523f5e9506fed4657c9700eebdbec");
		assertEquals(a.getValue("opaque"), "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS");

		a.setHeaderValue(AUTH2);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("username"), "Mufasa");
		assertEquals(a.getValue("realm"), "http-auth@example.org");
		assertEquals(a.getValue("uri"), "/dir/index.html");
		assertEquals(a.getValue("algorithm"), "SHA-256");
		assertEquals(a.getValue("nonce"), "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v");
		assertEquals(a.getValue("nc"), "00000001");
		assertEquals(a.getValue("cnonce"), "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ");
		assertEquals(a.getValue("qop"), "auth");
		assertEquals(a.getValue("response"), "753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1");
		assertEquals(a.getValue("opaque"), "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS");

		a.setHeaderValue(AUTH3);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("username"), "488869477bf257147b804c45308cd62ac4e25eb717b12b298c79e62dcea254ec");
		assertEquals(a.getValue("realm"), "api@example.org");
		assertEquals(a.getValue("uri"), "/doe.json");
		assertEquals(a.getValue("algorithm"), "SHA-512-256");
		assertEquals(a.getValue("nonce"), "5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK");
		assertEquals(a.getValue("nc"), "00000001");
		assertEquals(a.getValue("cnonce"), "NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v");
		assertEquals(a.getValue("qop"), "auth");
		assertEquals(a.getValue("response"), "3798d4131c277846293534c3edc11bd8a5e4cdcbff78b05db9d95eeb1cec68a5");
		assertEquals(a.getValue("opaque"), "HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS");
		assertEquals(a.getValue("userhash"), "true");

		a.setHeaderValue(AUTH4);
		assertTrue(a.isType("Digest"));
		assertEquals(a.getValue("username*"), "UTF-8''J%C3%A4s%C3%B8n%20Doe");
		assertEquals(a.getValue("realm"), "api@example.org");
		assertEquals(a.getValue("uri"), "/doe.json");
		assertEquals(a.getValue("algorithm"), "SHA-512-256");
		assertEquals(a.getValue("nonce"), "5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK");
		assertEquals(a.getValue("nc"), "00000001");
		assertEquals(a.getValue("cnonce"), "NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v");
		assertEquals(a.getValue("qop"), "auth");
		assertEquals(a.getValue("response"), "3798d4131c277846293534c3edc11bd8a5e4cdcbff78b05db9d95eeb1cec68a5");
		assertEquals(a.getValue("opaque"), "HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS");
		assertEquals(a.getValue("userhash"), "false");
	}

	@Test
	void checks() throws UnsupportedEncodingException {
		final Authorization a = new Authorization();
		a.setHeaderValue(AUTH1);
		assertTrue(auth(a, "Mufasa", "Circle of Life"));

		a.setHeaderValue(AUTH2);
		assertTrue(auth(a, "Mufasa", "Circle of Life"));

		// username:"Jason Doe" password"Secret, or not?"
		// System.out.println(Utility.hex("Jäsøn
		// Doe".getBytes(StandardCharsets.UTF_8)));
		// 4ac3a473c3b86e20446f65
		// 4AC3A473C3B86E20446F65

		a.setHeaderValue(AUTH3);
		assertTrue(auth(a, "Jäsøn Doe", "Secret, or not?"));

		a.setHeaderValue(AUTH4);
		assertTrue(auth(a, "Jäsøn Doe", "Secret, or not?"));

		// "UTF-8''J%C3%A4s%C3%B8n%20Doe"
		String username = a.getValue("username*");
		int index = username.indexOf("''");
		username = URLDecoder.decode(username.substring(index + 2), username.substring(0, index));
		assertEquals(username, "Jäsøn Doe");

	}

	final static byte COLON = ':';

	private boolean auth(Authorization a, String username, String password) {
		final String realm = a.getValue("realm");
		final String algorithm = a.getValue("algorithm");
		final String response = a.getValue("response");
		final String cnonce = a.getValue("cnonce");
		final String nonce = a.getValue("nonce");
		final String uri = a.getValue("uri");
		final String qop = a.getValue("qop");
		final String nc = a.getValue("nc");

		if (Utility.isEmpty(response)) {
			return false;
		}
		if (Utility.isEmpty(cnonce)) {
			return false;
		}
		if (Utility.isEmpty(nonce)) {
			return false;
		}
		if (Utility.isEmpty(uri)) {
			return false;
		}
		if (Utility.isEmpty(nc)) {
			return false;
		}

		final MessageDigest md;
		try {
			if (Utility.isEmpty(algorithm)) {
				md = MessageDigest.getInstance("MD5");
			} else {
				if (Utility.same("SHA-512-256", algorithm)) {
					md = MessageDigest.getInstance("SHA-512/256");
				} else {
					md = MessageDigest.getInstance(algorithm);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			return false;
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
		md.update(username.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(realm.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(password.getBytes(StandardCharsets.UTF_8));
		final String A1 = Utility.hex(md.digest());

		String A2;
		if (Utility.isEmpty(qop) || "auth".equalsIgnoreCase(qop)) {
			// H(A2)
			md.reset();
			md.update("GET".getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(uri.getBytes(StandardCharsets.UTF_8));
			A2 = Utility.hex(md.digest());
			// response
			md.reset();
			md.update(A1.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(nonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(nc.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(cnonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(qop.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(A2.getBytes(StandardCharsets.UTF_8));
			if (Utility.same(Utility.hex(md.digest()), response)) {
				md.reset();
				md.update(COLON);
				md.update(uri.getBytes(StandardCharsets.UTF_8));
				A2 = Utility.hex(md.digest());
			} else {
				return false;
			}
		} else if ("auth-int".equalsIgnoreCase(qop)) {
			// H(A2)
			md.reset();
			md.update("GET".getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(uri.getBytes(StandardCharsets.UTF_8));
			A2 = Utility.hex(md.digest());
			// response
			md.reset();
			md.update(A1.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(nonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(nc.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(cnonce.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(qop.getBytes(StandardCharsets.UTF_8));
			md.update(COLON);
			md.update(A2.getBytes(StandardCharsets.UTF_8));
			if (Utility.same(Utility.hex(md.digest()), response)) {
				md.reset();
				md.update(COLON);
				md.update(uri.getBytes(StandardCharsets.UTF_8));
				A2 = Utility.hex(md.digest());
			}
		} else {
			return false;
		}
		// rspauth
		md.reset();
		md.update(A1.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(nonce.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(nc.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(cnonce.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(qop.getBytes(StandardCharsets.UTF_8));
		md.update(COLON);
		md.update(A2.getBytes(StandardCharsets.UTF_8));
		// Utility.hex(md.digest());
		return true;
	}

	@Test
	void testOpaque() {
		long nano;
		int size = 16;
		while (size-- > 0) {
			nano = System.nanoTime() / 100;
			if ((nano & 1) == 0) {
				// 0~9
				System.out.print((char) (48 + nano % 10));
			} else {
				// A~Z
				System.out.print((char) (65 + nano % 26));
			}
		}
	}
}
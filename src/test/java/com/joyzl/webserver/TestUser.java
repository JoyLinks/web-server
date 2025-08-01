/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.service.Users;

class TestUser {

	/**
	 * 验证用户与请求 HOST 和 URI PATH 匹配关系，用户可设定允许请求的 HOST 和 URI PATH
	 */
	@Test
	void test() throws Exception {
		final User user = new User();
		user.setEnable(true);
		user.setName("name");
		user.setPassword("123456789");

		final Request request1 = new Request();
		request1.addHeader(HTTP1.Host, "www.joyzl.com");
		request1.setURL("/test");

		user.setURIs(null);
		assertTrue(Users.check(request1, user));

		user.setURIs(new String[] { "" });
		assertTrue(Users.check(request1, user));

		user.setURIs(new String[] { "/" });
		assertTrue(Users.check(request1, user));

		user.setURIs(new String[] { "/a" });
		assertFalse(Users.check(request1, user));

		user.setURIs(new String[] { "//www.joyzl.com/a" });
		assertFalse(Users.check(request1, user));

		user.setURIs(new String[] { "//www.joyzl.com/test" });
		assertTrue(Users.check(request1, user));
	}
}
package com.joyzl.webserver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.webserver.entities.User;
import com.joyzl.webserver.manage.Users;

class TestUser {

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
		assertFalse(Users.check(request1, user));

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
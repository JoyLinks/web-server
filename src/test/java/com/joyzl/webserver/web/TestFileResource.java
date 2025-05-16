package com.joyzl.webserver.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestFileResource {

	@Test
	void testPath() {
		assertEquals(FileResourceServlet.normalize("/"), "/");
		assertEquals(FileResourceServlet.normalize("."), "/");

		assertEquals(FileResourceServlet.normalize("/."), "/");
		assertEquals(FileResourceServlet.normalize("./"), "/");
		assertEquals(FileResourceServlet.normalize(".."), "/");

		assertEquals(FileResourceServlet.normalize("/.."), "/");
		assertEquals(FileResourceServlet.normalize("../"), "/");
		assertEquals(FileResourceServlet.normalize("/./"), "/");

		assertEquals(FileResourceServlet.normalize("/root/."), "/root/");
		assertEquals(FileResourceServlet.normalize("/root/.."), "/");

		assertEquals(FileResourceServlet.normalize("/../x"), "/x");
		assertEquals(FileResourceServlet.normalize("/././foo"), "/foo");
		assertEquals(FileResourceServlet.normalize("/././"), "/");

		assertEquals(FileResourceServlet.normalize("/root/text/img.png"), "/root/text/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/./img.png"), "/root/text/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/././img.png"), "/root/text/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/../img.png"), "/root/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/../../img.png"), "/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/./../img.png"), "/root/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/././../../img.png"), "/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/a/././../../img.png"), "/root/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/a/././../../../img.png"), "/img.png");
		assertEquals(FileResourceServlet.normalize("/root/text/../../../img.png"), "/img.png");
		assertEquals(FileResourceServlet.normalize("/root/../../../img.png"), "/img.png");
		assertEquals(FileResourceServlet.normalize("/../../../img.png"), "/img.png");
	}

}

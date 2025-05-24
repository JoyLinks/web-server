package com.joyzl.webserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestPath {

	@Test
	void testPath() {
		assertEquals(Utility.normalizePath("/"), "/");
		assertEquals(Utility.normalizePath("."), "/");

		assertEquals(Utility.normalizePath("/."), "/");
		assertEquals(Utility.normalizePath("./"), "/");
		assertEquals(Utility.normalizePath(".."), "/");

		assertEquals(Utility.normalizePath("/.."), "/");
		assertEquals(Utility.normalizePath("../"), "/");
		assertEquals(Utility.normalizePath("/./"), "/");

		assertEquals(Utility.normalizePath("/root/."), "/root/");
		assertEquals(Utility.normalizePath("/root/.."), "/");

		assertEquals(Utility.normalizePath("/../x"), "/x");
		assertEquals(Utility.normalizePath("/././foo"), "/foo");
		assertEquals(Utility.normalizePath("/././"), "/");

		assertEquals(Utility.normalizePath("/root/text/img.png"), "/root/text/img.png");
		assertEquals(Utility.normalizePath("/root/text/./img.png"), "/root/text/img.png");
		assertEquals(Utility.normalizePath("/root/text/././img.png"), "/root/text/img.png");
		assertEquals(Utility.normalizePath("/root/text/../img.png"), "/root/img.png");
		assertEquals(Utility.normalizePath("/root/text/../../img.png"), "/img.png");
		assertEquals(Utility.normalizePath("/root/text/./../img.png"), "/root/img.png");
		assertEquals(Utility.normalizePath("/root/text/././../../img.png"), "/img.png");
		assertEquals(Utility.normalizePath("/root/text/a/././../../img.png"), "/root/img.png");
		assertEquals(Utility.normalizePath("/root/text/a/././../../../img.png"), "/img.png");
		assertEquals(Utility.normalizePath("/root/text/../../../img.png"), "/img.png");
		assertEquals(Utility.normalizePath("/root/../../../img.png"), "/img.png");
		assertEquals(Utility.normalizePath("/../../../img.png"), "/img.png");
	}
}
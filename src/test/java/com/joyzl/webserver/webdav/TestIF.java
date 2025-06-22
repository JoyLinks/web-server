/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * RFC2518 If 标头值解析
 * 
 * @author ZhangXi 2025年5月30日
 */
class TestIF {

	@Test
	void testIF1() {
		final If IF = new If();
		IF.setHeaderValue(" (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> [\"ETag1\"]) ([\"ETag2\"])");

		assertTrue(IF.nextGroup());
		assertNull(IF.getTag());

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2");

		assertTrue(IF.nextValue());
		assertFalse(IF.isToken());
		assertTrue(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "\"ETag1\"");

		assertFalse(IF.nextValue());
		assertTrue(IF.nextGroup());

		assertTrue(IF.nextValue());
		assertTrue(IF.isETag());
		assertEquals(IF.getValue(), "\"ETag2\"");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF2() {
		final If IF = new If();
		IF.setHeaderValue(" (Not <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> <urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092>)");

		assertTrue(IF.nextGroup());
		assertNull(IF.getTag());

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertTrue(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2");

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF3() {
		final If IF = new If();
		IF.setHeaderValue(" (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>) (Not <DAV:no-lock>)");

		assertTrue(IF.nextGroup());
		assertNull(IF.getTag());

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2");

		assertFalse(IF.nextValue());
		assertTrue(IF.nextGroup());

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertTrue(IF.isNot());
		assertEquals(IF.getValue(), "DAV:no-lock");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF4() {
		final If IF = new If();
		IF.setHeaderValue(" </resource1> (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> [W/\"A ETag\"]) ([\"strong ETag\"])");

		assertTrue(IF.nextGroup());
		assertEquals(IF.getTag(), "/resource1");

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2");

		assertTrue(IF.nextValue());
		assertFalse(IF.isToken());
		assertTrue(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "W/\"A ETag\"");

		assertFalse(IF.nextValue());
		assertTrue(IF.nextGroup());

		assertTrue(IF.nextValue());
		assertTrue(IF.isETag());
		assertEquals(IF.getValue(), "\"strong ETag\"");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF5() {
		final If IF = new If();
		IF.setHeaderValue(" <http://www.example.com/specs/> (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>)");

		assertTrue(IF.nextGroup());
		assertEquals(IF.getTag(), "http://www.example.com/specs/");

		assertTrue(IF.nextValue());
		assertTrue(IF.isToken());
		assertFalse(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF6() {
		final If IF = new If();
		IF.setHeaderValue(" </specs/rfc2518.doc> ([\"4217\"])");

		assertTrue(IF.nextGroup());
		assertEquals(IF.getTag(), "/specs/rfc2518.doc");

		assertTrue(IF.nextValue());
		assertFalse(IF.isToken());
		assertTrue(IF.isETag());
		assertFalse(IF.isNot());
		assertEquals(IF.getValue(), "\"4217\"");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}

	@Test
	void testIF7() {
		final If IF = new If();
		IF.setHeaderValue(" </specs/rfc2518.doc> (Not [\"4217\"])");

		assertTrue(IF.nextGroup());
		assertEquals(IF.getTag(), "/specs/rfc2518.doc");

		assertTrue(IF.nextValue());
		assertFalse(IF.isToken());
		assertTrue(IF.isETag());
		assertTrue(IF.isNot());
		assertEquals(IF.getValue(), "\"4217\"");

		assertFalse(IF.nextValue());
		assertFalse(IF.nextGroup());
	}
}
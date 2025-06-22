/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.joyzl.webserver.servlet.Wildcards;

/**
 * Wildcards 用于URI通配符匹配的正确性和性能测试
 * 
 * @author ZhangXi 2023年9月14日
 */
class TestWildcards {

	final static Wildcards<Object> WILDCARDS = new Wildcards<>();
	final static int SIZE = 1000;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// 测试样本:全字符匹配
		String text;
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			WILDCARDS.bind(text, text);
		}

		// 测试样本:前缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index) + Wildcards.ANY;
			WILDCARDS.bind(text, text);
		}

		// 测试样本:后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Wildcards.ANY + Integer.toString(index);
			WILDCARDS.bind(text, text);
		}

		// 测试样本:前缀和后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			text = text + Wildcards.ANY + text;
			WILDCARDS.bind(text, text);
		}

		// 测试样本
		WILDCARDS.bind("joyzl*test", "joyzl*test");
		WILDCARDS.bind("*test", "*test");
		WILDCARDS.bind("/*", "/*");
		WILDCARDS.bind("*", "*");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	// 全字符匹配>前缀匹配>后缀匹配>前缀和后缀匹配

	// @Test
	void time() {
		int type = 1;
		Object o = null;

		long time = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			if (type == 1) {
				;
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("整数比较:" + time + "ms");

		time = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			if (o == null) {
				;
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("对象比较:" + time + "ms");

		// 测试结果均为2/3毫秒，可忽略不计
	}

	@Test
	void testWildcards() {
		// 全字符匹配
		String text, value;
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text;
			assertEquals(WILDCARDS.find(text), value);
		}

		// 前缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text + Wildcards.ANY;
			assertEquals(WILDCARDS.find(text + "B"), value);
		}

		// 后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = Wildcards.ANY + text;
			assertEquals(WILDCARDS.find("A" + text), value);
		}

		// 前缀和后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text + Wildcards.ANY + text;
			assertEquals(WILDCARDS.find(text + "A" + text), value);
		}
		// 空字符
		assertEquals(WILDCARDS.find(""), "*");
	}

	@RepeatedTest(10000)
	void performance1() {
		assertEquals(WILDCARDS.find("A"), "*");
	}

	@Test
	void performance2() {
		long time = System.currentTimeMillis();
		int size = 10000;
		while (size-- > 0) {
			assertEquals(WILDCARDS.find("A"), "*");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("耗时:" + time + "ms");
	}
}

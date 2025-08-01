/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

class TestInetAddress {

	@Test
	void test() throws Exception {
		InetAddress a1 = InetAddress.getByName("192.168.0.1");
		System.out.println(a1.getHostAddress());

		InetAddress a2 = InetAddress.getByName("192.168.0.255");
		System.out.println(a2.getHostAddress());

		InetAddress a3 = InetAddress.getByName("www.joyzl.com");
		System.out.println(a3.getHostAddress());

		System.out.println(a1.equals(a2));
		System.out.println(a1.getAddress()[0]);
		System.out.println(a1.getAddress()[1]);
		System.out.println(a1.getAddress()[2]);
		System.out.println(a1.getAddress()[3]);

	}

}
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
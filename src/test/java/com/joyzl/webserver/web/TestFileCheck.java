package com.joyzl.webserver.web;

import java.io.File;

import org.junit.jupiter.api.Test;

class TestFileCheck {

	@Test
	void testFile() {
		final File file = new File("LICENSE");
		System.out.println(file.lastModified());
		System.out.println(file.length());

		int count = 1000000;
		long time = System.currentTimeMillis();
		while (count-- > 0) {
			file.lastModified();
			file.length();
		}
		time = System.currentTimeMillis() - time;
		System.out.println(time);
		// 42秒，有点慢呀
	}

}
package com.joyzl.webserver;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.http.HTTPCoder;

class FileTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		File file = new File("");
		System.out.println(file.getAbsolutePath());
		// D:\GitHub\web-server
		// assertTrue(file.exists());

		file = new File(".");
		System.out.println(file.getAbsolutePath());
		// D:\GitHub\web-server\.
		assertTrue(file.exists());

		file = new File("content");
		System.out.println(file.getAbsolutePath());
		// D:\GitHub\web-server\content

		file = new File("src");
		System.out.println(file.getAbsolutePath());
		// D:\GitHub\web-server\src
		assertTrue(file.exists());
		assertTrue(file.isDirectory());

		file = new File("src\\");
		assertTrue(file.exists());
		assertTrue(file.isDirectory());

		file = new File("src/");
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
		System.out.println(file.getName());
	}

	@Test
	void lastModified() throws Exception {
		File file = new File("test.txt");
		System.out.println(file.getAbsolutePath());
		// D:\GitHub\web-server\test.txt
		System.out.println(file.getName());

		file.createNewFile();
		long modified = file.lastModified();

		file.setLastModified(modified + 1);
		assertNotEquals(modified, file.lastModified());

		// 如果两次修改时间小于1毫秒
		// 无法通过文件修改时间反应出来
		Thread.sleep(1);

		modified = file.lastModified();
		try (FileOutputStream output = new FileOutputStream(file)) {
			output.write(1);
			output.flush();
		}
		System.out.println(modified + ":" + file.lastModified());
		assertNotEquals(modified, file.lastModified());

		long time = System.currentTimeMillis();
		for (int index = 0; index < 10000; index++) {
			modified = file.lastModified();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("File.lastModified() 耗时 " + time);
		// 结论：比较耗时

		file.delete();
		System.out.println(modified + ":" + file.lastModified());
		assertNotEquals(modified, file.lastModified());
	}

	@Test
	void testDiskOrRAM() throws IOException {
		// 测试每次从磁盘读取文件以及缓存文件性能差异

		final File file = new File("test.dat");
		try (FileOutputStream output = new FileOutputStream(file)) {
			for (int index = 0; index < HTTPCoder.BLOCK_BYTES; index++) {
				output.write(index);
			}
			output.flush();
		}

		// 测试次数
		final int count = 1000;

		// 从文件读取
		long time = System.currentTimeMillis();
		for (int index = 0; index < count; index++) {
			try (FileInputStream input = new FileInputStream(file)) {
				while (input.read() >= 0) {
					;
				}
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("从文件读取" + count + "次，耗时：" + time);

		// 构建缓存
		time = System.currentTimeMillis();
		final ByteBuffer buffer = ByteBuffer.allocateDirect((int) file.length());
		try (FileInputStream input = new FileInputStream(file);
			FileChannel channel = input.getChannel();) {
			channel.read(buffer);
			buffer.flip();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("构建缓存耗时：" + time);

		// 从缓存读取
		time = System.currentTimeMillis();
		for (int index = 0; index < count; index++) {
			for (int i = 0; i < buffer.limit(); i++) {
				buffer.get(i);
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("从缓存读取" + count + "次，耗时：" + time);

		// 结论：内存比固态硬盘快1000倍不止
		file.delete();
	}

	@Test
	void testURI() {
		final File root = new File("");
		final File file = new File(root, "style/index.html");
		final String uri = file.getPath().substring(root.getPath().length()).replace('\\', '/');
		System.out.println(uri);
	}

	@Test
	void testToString() {
		long time = System.currentTimeMillis();
		for (int index = 0; index < 1000000; index++) {
			Long.toString(time);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("数值转换字符串，耗时：" + time);

		String text = Long.toString(System.currentTimeMillis());
		time = System.currentTimeMillis();
		for (int index = 0; index < 1000000; index++) {
			text.toString();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("直接获取字符串，耗时：" + time);
		// 结论：直接获取显然更快
	}
}
/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Archive 多文件归档测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月27日
 */
class TestArchive {

	static final Path PATH = Path.of("archive.test");
	static final Path FILE1 = Path.of("archive1.test");
	static final Path FILE2 = Path.of("archive2.test");
	static final Path FILE3 = Path.of("archive3.test");

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Files.deleteIfExists(PATH);

		Files.writeString(FILE1, "TEST1", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.writeString(FILE2, "TEST2", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.write(FILE3, new byte[1024 * 1024 + 1], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		Files.deleteIfExists(PATH);
		Files.deleteIfExists(FILE1);
		Files.deleteIfExists(FILE2);
		Files.deleteIfExists(FILE3);
	}

	@Test
	void test() throws IOException {
		final Packet archive = new Packet(PATH);
		final long time = System.currentTimeMillis();
		archive.add(FILE1.toFile());
		archive.add(FILE2.toFile(), "NUMBER1");
		archive.add(FILE3.toFile(), "NUMBER2");

		// final byte[] bytes = Files.readAllBytes(PATH);
		// System.out.print(bytes.length);
		// System.out.println(Arrays.toString(bytes));

		final List<Document> files = archive.list();
		assertEquals(files.size(), 3);
		assertEquals(files.get(0).getIndex(), 0);
		assertEquals(files.get(0).getName(), FILE1.getFileName().toString());
		assertEquals(files.get(0).getSize(), Files.size(FILE1));
		assertEquals(files.get(0).getNumber(), null);
		assertEquals(files.get(1).getIndex(), 1);
		assertEquals(files.get(1).getName(), FILE2.getFileName().toString());
		assertEquals(files.get(1).getSize(), Files.size(FILE2));
		assertEquals(files.get(1).getNumber(), "NUMBER1");
		assertEquals(files.get(2).getIndex(), 2);
		assertEquals(files.get(2).getName(), FILE3.getFileName().toString());
		assertEquals(files.get(2).getSize(), Files.size(FILE3));
		assertEquals(files.get(2).getNumber(), "NUMBER2");

		// TIME
		assertTrue(files.get(0).getTime() >= time);
		assertTrue(files.get(1).getTime() >= time);
		assertTrue(files.get(2).getTime() >= time);

		// GET BY INDEX
		{
			final Document file = archive.get(0);
			final byte[] data1 = Files.readAllBytes(FILE1);
			assertEquals(file.stream().available(), data1.length);
			final byte[] data2 = file.stream().readAllBytes();
			assertArrayEquals(data1, data2);
		}
		{
			final Document file = archive.get(1);
			final byte[] data1 = Files.readAllBytes(FILE2);
			assertEquals(file.stream().available(), data1.length);
			final byte[] data2 = file.stream().readAllBytes();
			assertArrayEquals(data1, data2);
		}
		{
			final Document file = archive.get(2);
			final byte[] data1 = Files.readAllBytes(FILE3);
			assertEquals(file.stream().available(), data1.length);
			final byte[] data2 = file.stream().readAllBytes();
			assertArrayEquals(data1, data2);
		}
		assertNull(archive.get(3));

		// GET BY NAME

		{
			final Document file = archive.get(FILE1.getFileName().toString());
			final byte[] data = file.stream().readAllBytes();
			assertArrayEquals(data, Files.readAllBytes(FILE1));
		}
		{
			final Document file = archive.get(FILE2.getFileName().toString());
			final byte[] data = file.stream().readAllBytes();
			assertArrayEquals(data, Files.readAllBytes(FILE2));
		}
		{
			final Document file = archive.get(FILE3.getFileName().toString());
			final byte[] data = file.stream().readAllBytes();
			assertArrayEquals(data, Files.readAllBytes(FILE3));
		}
		assertNull(archive.get("OTHER"));

		// INPUT READ

		{
			final Document file = archive.get(0);
			int size = 0;
			while (file.stream().read() >= 0) {
				size++;
			}
			assertEquals(Files.size(FILE1), size);
		}
		{
			final Document file = archive.get(1);
			int size = 0;
			while (file.stream().read() >= 0) {
				size++;
			}
			assertEquals(Files.size(FILE2), size);
		}
		{
			final Document file = archive.get(2);
			int size = 0;
			while (file.stream().read() >= 0) {
				size++;
			}
			assertEquals(Files.size(FILE3), size);
		}

		// LAST

		{
			final Document file = archive.last();
			assertEquals(Files.size(FILE3), file.stream().available());
			final byte[] data = file.stream().readAllBytes();
			assertArrayEquals(data, Files.readAllBytes(FILE3));
		}
	}
}

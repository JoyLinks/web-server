/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

/**
 * 验证超大量文件索引
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月27日
 */
class TestPathIndex {

	@Test
	void testFeature() {
		final List<String> items1 = new ArrayList<>();
		final List<String> items2 = new ArrayList<>();
		for (int index = 0; index < 10000; index++) {
			items1.add("JOYZL-WBK1-202510-" + String.format("%06d", index));
			items2.add("JOYZL-SDH23-202511-" + String.format("%06d", index));
		}

		// 计算特征

		long value = 0;
		for (String item : items1) {
			value |= Coder.check(item);
		}
		System.out.println("ITEMS1: " + value);

		value = 0;
		for (String item : items2) {
			value |= Coder.check(item);
		}
		System.out.println("ITEMS2: " + value);
	}

	// @Test
	void test() throws IOException {
		final Path path = Path.of("c:\\");
		final ConcurrentHashMap<String, Path> PATHS = new ConcurrentHashMap<>(10000);
		long time = System.currentTimeMillis();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			int index = 1;

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				PATHS.put(Integer.toString(index++, Character.MAX_RADIX), file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		time = System.currentTimeMillis() - time;

		System.out.println("PATH:" + path);
		System.out.println("FILES:" + PATHS.size());
		System.out.println(time + "ms");

		// PATH:c:\
		// FILES:567552
		// 33435ms
	}
}
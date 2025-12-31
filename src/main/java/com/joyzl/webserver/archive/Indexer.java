/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 归档库索引
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月29日
 */
public final class Indexer {

	// 归档结构：archives/20251023/code
	// 为每个日期目录建立索引并维护内部文件特征
	// 通过代码定位文件时遍历目录索引判断特征
	// 符合特征的目录尝试判断文件是否存在

	private final Path path;
	private final Path index;
	private final ConcurrentHashMap<Path, Directory> DIRECTORIES = new ConcurrentHashMap<>();

	public Indexer(Path path) throws IOException {
		index = path.resolve("index");
		this.path = path;

		// 如果删除索引文件将自动重建
		if (Files.exists(index)) {
			load();
		} else {
			build();
		}
	}

	public Path find(String code) {
		return find(code, Coder.check(code));
	}

	public Path find(String code, long chars) {
		Path file = null;
		for (Directory directory : DIRECTORIES.values()) {
			if (directory.feature(code, chars)) {
				file = directory.exists(code);
				if (file != null) {
					break;
				}
			}
		}
		return file;
	}

	public Collection<Directory> list() {
		return Collections.unmodifiableCollection(DIRECTORIES.values());
	}

	/** 更新目录和特征 */
	void update(Path path, String code, long chars, long length) throws IOException {
		Directory directory = DIRECTORIES.get(path);
		if (directory == null) {
			directory = new Directory(path);
			DIRECTORIES.put(path, directory);
		}
		directory.update(code, chars, length);
	}

	/** 移除目录 */
	void remove(Path path) throws IOException {
		if (DIRECTORIES.remove(path) != null) {
			save();
		}
	}

	/** 移除目录 */
	void remove(Collection<Path> paths) throws IOException {
		int size = 0;
		for (Path path : paths) {
			if (DIRECTORIES.remove(path) != null) {
				size++;
			}
		}
		if (size > 0) {
			save();
		}
	}

	public void close() throws IOException {
		save();
	}

	/*-
	 * 索引文件格式
	 * +------+------+-------+-------+-----+-----+
	 * | name | size | bytes | chars | min | max |
	 * +------+------+-------+-------+-----+-----+
	 * name		固定8字节的ASCII目录名，字符表示的日期 20111203
	 * size		4字节整数，表示目录中文件数量
	 * bytes	8字节整数，表示目录中文件字节数
	 * chars	8字节整数，表示目录中文件名字符特征
	 * min		2字节整数，表示目录中文件名最短字符数
	 * max		2字节整数，表示目录中文件名最长字符数
	 */

	/** 加载索引文件 */
	private void load() throws IOException {
		try (final FileInputStream input = new FileInputStream(index)) {
			Path dir;
			long bytes, chars;
			int size, minChars, maxChars;
			final byte[] name = new byte[8];
			while (input.available() > 0) {
				if (input.read(name) == 8) {
					size = input.read4Byte();
					bytes = input.read8Byte();
					chars = input.read8Byte();
					minChars = input.read2Byte();
					maxChars = input.read2Byte();
					if (size < 0 || minChars < 0 || maxChars < 0 || minChars > maxChars) {
						throw new IOException("索引文件可能损坏或格式过旧");
					} else {
						dir = path.resolve(new String(name, StandardCharsets.US_ASCII));
						DIRECTORIES.put(dir, new Directory(dir, size, bytes, chars, minChars, maxChars));
					}
				} else {
					throw new IOException("索引文件可能损坏或格式过旧");
				}
			}
		}

		// for (Directory directory : DIRECTORIES.values()) {
		// System.out.println(directory);
		// }
	}

	/** 保存索引文件 */
	private void save() throws IOException {
		try (final FileOutputStream output = new FileOutputStream(index, false)) {
			for (Directory directory : DIRECTORIES.values()) {
				output.write(directory.name().getBytes(StandardCharsets.US_ASCII));
				output.write4byte(directory.codes());
				output.write8byte(directory.size());
				output.write8byte(directory.chars());
				output.write2Byte(directory.minChars());
				output.write2Byte(directory.maxChars());
			}
		}
	}

	/** 重建整个索引 */
	public void build() throws IOException {
		final Map<Path, Directory> directories = new HashMap<>();
		Files.walkFileTree(path, Collections.emptySet(), 2, new SimpleFileVisitor<Path>() {
			Directory directory;

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (path.equals(dir)) {
					// 排除主目录
					return FileVisitResult.CONTINUE;
				}
				if (directory != null) {
					// 排除日期目录中混入的目录
					if (dir.startsWith(directory.path())) {
						return FileVisitResult.CONTINUE;
					}
				}

				// System.out.println(dir);

				final String date = dir.getFileName().toString();
				if (date.length() == 8) {
					try {
						// 目录名符合 "20111203" 格式
						if (LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE) != null) {
							directory = new Directory(dir);
							directories.put(dir, directory);
						}
						return FileVisitResult.CONTINUE;
					} catch (Exception e) {
						// 忽略无效目录
					}
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (directory != null) {
					// 排除日期目录之外的文件
					if (file.startsWith(directory.path())) {
						// 排除不符合字节块的文件
						if (attrs.size() % Packet.BLOCK == 0) {
							final String code = file.getFileName().toString();
							try {
								final long chars = Coder.check(code);
								directory.update(code, chars, attrs.size());
							} catch (Exception e) {
								// 忽略无效文件
							}
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});

		DIRECTORIES.clear();
		DIRECTORIES.putAll(directories);
		save();
	}
}
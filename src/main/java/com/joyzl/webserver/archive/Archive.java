/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 归档文件库
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月23日
 */
public final class Archive {

	// 组织结构：archives/20251023/code
	// Linux 文件名长度 255 Byte，路径长度 4096 Byte
	// Linux 文件名不允许 '/' 字符
	// Window 文件名长度 255 Char
	// Windows 文件名不允许 '<' '>' ':' '"' '/' '\' '|' '?' '*' 字符
	// 100年的话，目录为365*100=36500

	private final int id;
	private final Path path;
	private final int expire;
	private final Indexer indexer;

	public Archive(String path, int expire) throws IOException {
		this(Path.of(path), expire);
	}

	public Archive(Path path, int expire) throws IOException {
		this.expire = expire;
		this.path = path;

		if (conflict(path)) {
			throw new IOException("归档库路径冲突" + path);
		}

		if (Files.notExists(path)) {
			Files.createDirectory(path);
		}

		indexer = new Indexer(path);
		id = ARCHIVES.size();
		ARCHIVES.add(this);
	}

	/** 获取指定代码归档集 */
	public Packet find(String code) throws IOException {
		final Path path = indexer.find(code);
		if (path != null) {
			return new Packet(path);
		}
		return null;
	}

	/** 获取指定日期归档的代码列表 */
	public List<Packet> find(LocalDate date) throws IOException {
		final String day = DateTimeFormatter.BASIC_ISO_DATE.format(date);
		final Path directory = path.resolve(day);
		if (Files.isDirectory(directory)) {
			final List<Packet> archives = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory);) {
				for (Path file : stream) {
					archives.add(new Packet(file));
				}
			}
			return archives;
		}
		return null;
	}

	/** 归档文件到指定代码 */
	public void save(File file, String code, String number) throws IOException {
		save(file, code, file.getName(), number);
	}

	/** 归档文件到指定代码并指定文件名 */
	public void save(File file, String code, String name, String number) throws IOException {
		save(file, LocalDate.now(), code, name, number);
	}

	/** 归档文件到指定代码并指定日期和文件名 */
	private void save(File file, LocalDate date, String code, String name, String number) throws IOException {
		final long chars = Coder.check(code);
		final Path archive = indexer.find(code, chars);
		if (archive == null) {
			final String day = DateTimeFormatter.BASIC_ISO_DATE.format(date);
			final Path directory = path.resolve(day);
			if (Files.notExists(directory)) {
				Files.createDirectory(directory);
			}
			final Packet a = new Packet(directory.resolve(code));
			indexer.update(directory, code, chars, a.add(file, name, number));
		} else {
			final Packet a = new Packet(archive);
			indexer.update(archive.getParent(), code, chars, a.add(file, name, number));
		}
	}

	/** 删除过期数据 */
	public final Sweep sweep() throws IOException {
		if (expire <= 0) {
			return null;
		}

		final Sweep sweep = new Sweep();
		final LocalDate expired = LocalDate.now().minusDays(expire);
		final List<Path> paths = new ArrayList<>();

		Files.walkFileTree(path, Collections.emptySet(), 2, new SimpleFileVisitor<Path>() {
			String name;
			LocalDate date;

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				name = dir.getFileName().toString();
				if (name.length() == 8) {
					date = LocalDate.parse(name, DateTimeFormatter.BASIC_ISO_DATE);
					if (date.isBefore(expired)) {
						return FileVisitResult.CONTINUE;
					}
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				sweep.addFileSize(attrs.size());
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc == null) {
					Files.delete(dir);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		indexer.remove(paths);
		return sweep;
	}

	public void close() throws IOException {
		indexer.close();
	}

	public Indexer indexer() {
		return indexer;
	}

	public int expire() {
		return expire;
	}

	public Path path() {
		return path;
	}

	public int id() {
		return id;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 允许创建多个库，但不能路径冲突
	private static final List<Archive> ARCHIVES = new ArrayList<>();

	public static List<Archive> all() {
		return Collections.unmodifiableList(ARCHIVES);
	}

	public static synchronized Archive get(int id) {
		if (id < 0 || id >= ARCHIVES.size()) {
			return null;
		}
		return ARCHIVES.get(id);
	}

	/** 检查路径是否冲突 */
	public static boolean conflict(Path path) {
		if (ARCHIVES.isEmpty()) {
			return false;
		}
		// 转换为绝对路径并规范化（去除冗余部分）
		path = path.toAbsolutePath().normalize();
		Path b;
		for (Archive a : ARCHIVES) {
			b = a.path.toAbsolutePath().normalize();
			// 检查是否相互包含
			if (path.startsWith(b) || b.startsWith(path)) {
				return true;
			}
		}
		return false;
	}
}
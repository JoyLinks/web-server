/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 归档文件包，多个文件归档为单个文件进行存储；文件只能添加不能修改和删除。
 * <p>
 * 归档文件本身文件名为指定代码，添加的每个文件由文件头和文件体组成，
 * 文件头包含文件的大小和原始文件名和扩展名，支持中文（UTF-8），文件头固定512字节；后跟文件数据内容，填充为512的整倍数。
 * 目的在于：隐藏被归档文件的原始文件名，并允许多文件归档到同一个代码下；原始文件名直接存储会导致安全风险。
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月27日
 */
public final class Packet {

	/** 数据块大小 */
	final static int BLOCK = 512;

	/*-
	 * 多文件归档为单个文件
	 * +----------------------------------------------------------+------------------------+
	 * |                       FILE HEAD                          |        FILE DATA       |
	 * +---------+---------+-----------+-------------+------------+-----------+------------+
	 * | SIZE(4) | TIME(8) | NAME(1,n) | NUMBER(1,n) | PADDING(n) | FILE DATA | PADDING(n) |
	 * +---------+---------+-----------+-------------+------------+-----------+------------+
	 * 
	 * Linux 文件名长度 255 Byte，路径长度 4096 Byte
	 * Window 文件名长度 255 Char
	 */

	private final Path path;

	public Packet(Path path) {
		this.path = path;
	}

	/** 添加文件，返回增加的字节数 */
	public long add(File file) throws IOException {
		return add(file, file.getName(), null);
	}

	/** 添加文件，返回增加的字节数 */
	public long add(File file, String number) throws IOException {
		return add(file, file.getName(), number);
	}

	/** 添加文件，返回增加的字节数 */
	public long add(final File file, final String name, final String number) throws IOException {
		final long size = file.length();
		if (size <= 0) {
			throw new IOException("不能存入空文件");
		}
		if (size > Integer.MAX_VALUE) {
			throw new IOException("文件大小超过限制");
		}
		// 文件名必须有
		final byte[] names = name.getBytes(StandardCharsets.UTF_8);
		if (names.length > BLOCK - 8) {
			throw new IOException("文件名长度超过限制");
		}
		// 编号可以为空
		final byte[] numbers;
		if (number != null) {
			numbers = number.getBytes(StandardCharsets.UTF_8);
			if (numbers.length > BLOCK - names.length - 8) {
				throw new IOException("编号长度超过限制");
			}
		} else {
			numbers = null;
		}

		long temp;
		try (final FileOutputStream output = new FileOutputStream(path, true)) {
			temp = output.size();
			int padding = BLOCK;

			// HEAD
			padding = padding - 4 - 8;
			// HEAD.SIZE
			output.write4byte((int) size);
			// HEAD.TIME
			output.write8byte(System.currentTimeMillis());
			// HEAD.NAME
			padding = padding - names.length - 1;
			output.write(names.length);
			output.write(names);
			// HEAD.NUMBER
			if (numbers != null && numbers.length > 0) {
				padding = padding - numbers.length - 1;
				output.write(numbers.length);
				output.write(numbers);
			}
			// HEAD.PADDING
			while (padding-- > 0) {
				output.write(0);
			}

			// DATA.FILE
			output.write(file);
			// DATA.PADDING
			padding = (int) (BLOCK - size % BLOCK);
			while (padding-- > 0) {
				output.write(0);
			}

			output.flush();
			temp = output.size() - temp;
		}

		// 文件增加字节数
		return temp;
	}

	public List<Document> list() throws IOException {
		final List<Document> list = new ArrayList<>();
		try (final FileInputStream input = new FileInputStream(path)) {
			long time;
			int size, length;
			byte[] name, number;
			while (input.available() > 0) {
				// HEAD.SIZE
				size = input.read4Byte();
				// HEAD.TIME
				time = input.read8Byte();
				// HEAD.NAME
				length = input.read();
				name = new byte[length];
				input.read(name);
				// HEAD.NUMBER
				length = input.read();
				if (length > 0) {
					input.read(number = new byte[length]);
					list.add(new Document(list.size(), time, size, new String(name, StandardCharsets.UTF_8), new String(number, StandardCharsets.UTF_8)));
					// SKIP HEAD.PADDING
					input.skip(BLOCK - name.length - number.length - 14);
				} else {
					list.add(new Document(list.size(), time, size, new String(name, StandardCharsets.UTF_8)));
					// SKIP HEAD.PADDING
					input.skip(BLOCK - name.length - 14);
				}

				// SKIP DATA.FILE
				input.skip(size);
				// SKIP DATA.PADDING
				input.skip(BLOCK - size % BLOCK);
			}
		}
		return list;
	}

	public Document get(final String name) throws IOException {
		final byte[] names = name.getBytes(StandardCharsets.UTF_8);
		final byte[] temp = new byte[names.length];

		long time;
		byte[] number;
		int index = 0, size, length;
		@SuppressWarnings("resource")
		final FileInputStream input = new FileInputStream(path);
		while (input.available() > 0) {
			// HEAD.SIZE
			size = input.read4Byte();
			// HEAD.TIME
			time = input.read8Byte();
			// HEAD.NAME
			length = input.read();
			if (length == names.length) {
				input.read(temp);
				if (Arrays.equals(names, temp)) {
					// HEAD.NUMBER
					length = input.read();
					if (length > 0) {
						input.read(number = new byte[length]);
						input.skip(BLOCK - names.length - length - 14);
						return new Document(index, time, size, name, new String(number, StandardCharsets.UTF_8), input.range(input.position(), size));
					} else {
						input.skip(BLOCK - names.length - 14);
						return new Document(index, time, size, name, null, input.range(input.position(), size));
					}
				} else {
					input.skip(BLOCK - length - 13);
				}
			} else {
				input.skip(BLOCK - 13);
			}
			// DATA
			input.skip(size);
			input.skip(BLOCK - size % BLOCK);
			index++;
		}
		return null;
	}

	public Document get(final int index) throws IOException {
		if (index < 0) {
			throw new IllegalArgumentException("index:" + index);
		}

		long time;
		byte[] name, number;
		int i = index, size, length;
		@SuppressWarnings("resource")
		final FileInputStream input = new FileInputStream(path);
		while (input.available() > 0) {
			// HEAD.SIZE
			size = input.read4Byte();
			if (i > 0) {
				input.skip(BLOCK - 4);
				// DATA
				input.skip(size);
				input.skip(BLOCK - size % BLOCK);
				i--;
			} else {
				// HEAD.TIME
				time = input.read8Byte();
				// HEAD.NAME
				length = input.read();
				input.read(name = new byte[length]);
				// HEAD.NUMBER
				length = input.read();
				if (length > 0) {
					input.read(number = new byte[length]);
					input.skip(BLOCK - name.length - length - 14);
					return new Document(index, time, size, new String(name, StandardCharsets.UTF_8), new String(number, StandardCharsets.UTF_8), input.range(input.position(), size));
				} else {
					input.skip(BLOCK - name.length - 14);
					return new Document(index, time, size, new String(name, StandardCharsets.UTF_8), null, input.range(input.position(), size));
				}
			}
		}
		return null;
	}

	public Document last() throws IOException {
		int size = 0, index = -1;
		@SuppressWarnings("resource")
		final FileInputStream input = new FileInputStream(path);
		while (input.available() > 0) {
			// HEAD
			size = input.read4Byte();
			input.mark(0);
			input.skip(BLOCK - 4);
			// DATA
			input.skip(size);
			input.skip(BLOCK - size % BLOCK);
			index++;
		}
		input.reset();
		// HEAD.TIME
		final long time = input.read8Byte();
		final byte[] name, number;
		// HEAD.NAME
		int length = input.read();
		input.read(name = new byte[length]);
		// HEAD.NUMBER
		length = input.read();
		if (length > 0) {
			input.read(number = new byte[length]);
			input.skip(BLOCK - name.length - length - 14);
			return new Document(index, time, size, new String(name, StandardCharsets.UTF_8), new String(number, StandardCharsets.UTF_8), input.range(input.position(), size));
		} else {
			input.skip(BLOCK - name.length - 14);
			return new Document(index, time, size, new String(name, StandardCharsets.UTF_8), null, input.range(input.position(), size));
		}
	}

	public String name() {
		return path.getFileName().toString();
	}

	public Path path() {
		return path;
	}
}
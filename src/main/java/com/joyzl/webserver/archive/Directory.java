/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 归档日期目录索引
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月29日
 */
public final class Directory {

	/** 目录路径 */
	private final Path path;
	/** 目录所有代码文件数 */
	private int codes;
	/** 目录所有文件字节数 */
	private long size;

	/** 文件名长度特征 */
	private int minChars, maxChars;
	/** 文件名字母特征 'A'65 ~ '~'126 */
	private long chars;

	Directory(Path path) {
		this(path, 0, 0, 0, 256, 0);
	}

	Directory(Path path, int size, long bytes, long chars, int minChars, int maxChars) {
		this.minChars = minChars;
		this.maxChars = maxChars;
		this.chars = chars;
		this.size = bytes;
		this.path = path;
		this.codes = size;
	}

	/** 更新特征 */
	void update(String code, long chars, long length) {
		size += length;
		codes++;
		if (code.length() < minChars) {
			minChars = code.length();
		}
		if (code.length() > maxChars) {
			maxChars = code.length();
		}
		this.chars |= chars;
	}

	/** 检查特征 */
	public boolean feature(String code, long chars) {
		if (codes > 0) {
			if (code.length() >= minChars && code.length() <= maxChars) {
				return Coder.contains(this.chars, chars);
			}
		}
		return false;
	}

	/** 检查文件 */
	public Path exists(String code) {
		final Path file = path.resolve(code);
		if (Files.isRegularFile(file)) {
			return file;
		}
		return null;
	}

	@Override
	public String toString() {
		return path + " size:" + codes + ",bytes:" + size + ",chars:" + chars;
	}

	public int minChars() {
		return minChars;
	}

	public int maxChars() {
		return maxChars;
	}

	public long chars() {
		return chars;
	}

	public String name() {
		return path.getFileName().toString();
	}

	public Path path() {
		return path;
	}

	public long size() {
		return size;
	}

	public int codes() {
		return codes;
	}
}
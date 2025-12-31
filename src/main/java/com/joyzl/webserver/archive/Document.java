/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.InputStream;

/**
 * 归档文件描述类，对应文件包中的单个文件，文件包中可以有多个文件
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月27日
 */
public class Document {

	private final String name;
	private final String number;
	private final long time;
	private final int index;
	private final int size;

	private final InputStream stream;

	public Document(int index, long time, int size, String name) {
		this(index, time, size, name, null, null);
	}

	public Document(int index, long time, int size, String name, String number) {
		this(index, time, size, name, number, null);
	}

	public Document(int index, long time, int size, String name, String number, InputStream stream) {
		this.stream = stream;
		this.number = number;
		this.index = index;
		this.name = name;
		this.size = size;
		this.time = time;

	}

	public InputStream stream() {
		return stream;
	}

	/**
	 * 获取文件名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取文件附加编号
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * 获取文件索引
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 获取文件时间戳
	 */
	public long getTime() {
		return time;
	}

	/**
	 * 获取文件大小
	 */
	public int getSize() {
		return size;
	}
}
/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

/**
 * 清理结果
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月28日
 */
public class Sweep {

	private int files = 0;
	private long size = 0;

	void addFileSize(long value) {
		size += value;
		files++;
	}

	@Override
	public String toString() {
		return "FILES:" + files + ",SIZE:" + size;
	}

	public long getSize() {
		return size;
	}

	public int getFiles() {
		return files;
	}
}
/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

/**
 * 代码工具类
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年10月29日
 */
public final class Coder {

	// 检查代码是否合法
	// 检查过程中建立代码的特征码

	// 代码的字符特征计算：
	// 排除使用频率较高的数字字符和频率低的特殊字符
	// 提取代码中 'A' 到 '~' 之间的字符，总共62个字符
	// 采用长整数(long)的64个位记录出现的字符
	// 低位到高位对应 'A' 到 '~'

	public final static long check(String code) {
		if (code.length() <= 0) {
			throw new IllegalArgumentException("CODE不能为空");
		}
		if (code.length() > 255) {
			throw new IllegalArgumentException("CODE超过长度限制255");
		}
		char c;
		long value = 0;
		for (int i = 0; i < code.length(); i++) {
			c = code.charAt(i);
			if (c < 0x1F || c > 0x7F) {
				// 0x1F 及其之前为不可显示的控制字符
				// 0x7F DEL，之后不属于ASCII范围
				throw new IllegalArgumentException("CODE不能包含字符" + c);
			}
			if (c == '<' || c == '>' || c == ':' || c == '"' || c == '/' || c == '\\' || c == '|' || c == '?' || c == '*') {
				throw new IllegalArgumentException("CODE不能包含字符" + c);
			}

			// 字符特征
			if (c >= 'A' && c <= '~') {
				// 出现过的字符对应位置1
				// 排除出现频率较高的数字
				// long 有64位，字符有62个
				value = setBit(value, c - 'A');
			}
		}
		return value;
	}

	/** [63~0] */
	public final static int getBit(long value, int position) {
		return (value & (1L << position)) != 0 ? 1 : 0;
	}

	/** [63~0] */
	public final static long setBit(long value, int position) {
		return value | (1L << position);
	}

	/** b contains a */
	public final static boolean contains(long b, long a) {
		// 如果a是b的位子集，那么 a | b 应该等于 b
		return (a | b) == b;
	}
}
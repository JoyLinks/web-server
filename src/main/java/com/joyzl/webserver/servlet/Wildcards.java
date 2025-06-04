/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.servlet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 缓存仓库,仅有一个实例的缓存对象,支持通配符*匹配,仅能单个通配符
 * 
 * <p>
 * 匹配有效降：全字符匹配 > 前缀匹配 > 后缀匹配 > 前缀和后缀匹配<br>
 * 全字符匹配："/abcd.html"<br>
 * 前缀匹配： "/abcd.*"<br>
 * 后缀匹配： "/*.html"<br>
 * 前缀和后缀匹配："/test/*.html"<br>
 * 
 * @author ZhangXi
 * @date 2020年10月29日
 */
public final class Wildcards<T> {

	/** 通配符 */
	public static final char ANY = '*';
	public static final String STAR = "*";

	private Item<T>[] ITEMS;

	@SuppressWarnings("unchecked")
	public Wildcards() {
		ITEMS = (Item<T>[]) Array.newInstance(Item.class, 0);
	}

	public final T find(String key) {
		Item<T> item;
		for (int index = 0; index < ITEMS.length; index++) {
			item = ITEMS[index];
			if (item.match(key)) {
				return item.t;
			}
		}
		return null;
	}

	public final void bind(String text, T target) {
		ITEMS = Arrays.copyOf(ITEMS, ITEMS.length + 1);
		ITEMS[ITEMS.length - 1] = new Item<T>(target, text);
		Arrays.sort(ITEMS, Item.COMPARATOR);
	}

	/**
	 * 获取所有绑定项,修改返回的集合不会影响此对象实例内部绑定关系
	 */
	public final List<T> elements() {
		final List<T> items = new ArrayList<>(ITEMS.length);
		for (int index = 0; index < ITEMS.length; index++) {
			items.add(ITEMS[index].t);
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	public final void clear() {
		ITEMS = (Item<T>[]) Array.newInstance(Item.class, 0);
	}

	// 20250527 路径配置为"path":"/webdav/*"
	// 测试WEBDAV时发现Windows在添加网络位置时
	// 请求"http://192.168.2.12/webdav/"实际为"http://192.168.2.12/webdav"
	// 请求"http://192.168.2.12"实际为""http://192.168.2.12/"
	// 方案1：匹配时忽略路径配置中的尾部'/'
	// 方案2：LocationServlet

	static class Item<T> {
		final int wildcard;
		final String text;
		final T t;

		public Item(T t, String text) {
			this.t = t;
			this.text = text;
			wildcard = text.indexOf(ANY);
		}

		/** 前缀字符数 */
		int prefix() {
			return wildcard < 0 ? 0 : wildcard;
		}

		/** 后缀字符数 */
		int suffix() {
			return wildcard < 0 ? 0 : text.length() - wildcard - 1;
		}

		@Override
		public String toString() {
			return text;
		}

		final boolean match(String key) {
			// 匹配有效降：全字符匹配 > 前缀和后缀匹配 > 前缀匹配 > 后缀匹配
			// - /test.html
			// 6 /test/*.html
			// 6 /test/*
			// 0 *.html
			// 0 *.do
			// 0 *

			if (wildcard < 0) {
				// 全字符匹配
				if (key.length() != text.length()) {
					return false;
				}
				return key.regionMatches(0, text, 0, text.length());
			} else {
				if (wildcard > 0) {
					// 前缀匹配
					if (key.length() < wildcard) {
						return false;
					}
					if (key.regionMatches(0, text, 0, wildcard)) {
						// CONTINUE
					} else {
						return false;
					}
				}
				if (text.length() - wildcard > 1) {
					// 后缀匹配
					int size = text.length() - wildcard - 1;
					if (key.length() - wildcard < size) {
						return false;
					}
					return key.regionMatches(key.length() - size, text, wildcard + 1, size);
				} else {
					return true;
				}
			}
		}

		final static Comparator<Item<?>> COMPARATOR = new Comparator<>() {
			// 排序应为字符较多的优先于字符较少的
			// 否则"/*"和"/actions/*"后者永远无法匹配
			@Override
			public int compare(Item<?> a, Item<?> b) {
				if (a.wildcard < 0) {
					if (b.wildcard < 0) {
						return b.text.length() - a.text.length();
					} else {
						return -1;
					}
				} else {
					if (b.wildcard < 0) {
						return 1;
					} else {
						if (b.wildcard == a.wildcard) {
							return b.text.length() - a.text.length();
						}
						return b.wildcard - a.wildcard;
					}
				}
			}
		};
	}
}

/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.entities;

import java.util.Arrays;

/**
 * 虚拟主机
 * 
 * @author ZhangXi 2024年11月12日
 */
public class Host extends Domain {

	private String[] names;

	public boolean hasNames() {
		return names != null && names.length > 0;
	}

	/**
	 * 获取主机名
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * 设置主机名
	 */
	public void setNames(String... values) {
		names = values;
	}

	/**
	 * 比较不含内部集合对象
	 */
	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) {
			if (o instanceof Host h) {
				return equals(this, h);
			}
		}
		return false;
	}

	/**
	 * 比较服务参数是否相同，如果不同则意味着服务须重置
	 */
	public static boolean equals(Host a, Host b) {
		if (a.names != b.names) {
			if (a.names != null && b.names != null) {
				if (!Arrays.equals(a.names, b.names)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
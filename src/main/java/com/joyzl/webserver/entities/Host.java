package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟主机
 * 
 * @author ZhangXi 2024年11月12日
 */
public class Host extends Domain {

	private final List<String> names = new ArrayList<>();

	/**
	 * 获取主机名
	 */
	public List<String> getNames() {
		return names;
	}

	/**
	 * 设置主机名
	 */
	public void setNames(List<String> values) {
		if (values != names) {
			names.clear();
			names.addAll(values);
		}
	}
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.Set;

/**
 * 指定仅返回资源上属性名称的列表
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Propname {
	/*-
	 * <!ELEMENT propname EMPTY >
	 */

	Set<String> prop();

	Set<String> getProp();

	void setProp(Set<String> values);

	boolean hasProp();
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.List;

/**
 * 资源相关的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Prop {
	/*-
	 * <!ELEMENT prop ANY >
	 */

	List<Property> prop();

	List<Property> getProp();

	void setProp(List<Property> values);

	boolean hasProp();
}
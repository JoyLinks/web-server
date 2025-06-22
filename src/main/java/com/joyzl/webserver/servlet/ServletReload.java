/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.servlet;

import java.util.Map;

/**
 * 服务程序热重载支持
 * 
 * @author ZhangXi 2025年6月19日
 */
public abstract class ServletReload {

	/**
	 * 服务程序名称
	 */
	public abstract String name();

	/**
	 * 创建服务程序实例
	 * 
	 * @param path 路径
	 * @param parameters 配置参数
	 * @return null 配置参数无效 / Servlet 配置参数创建的实例
	 */
	public abstract Servlet create(String path, Map<String, String> parameters);

	/**
	 * 检查服务程序实例与指定参数是否不同
	 * 
	 * @param servlet 实例
	 * @param parameters 配置参数
	 * @return true 不同
	 */
	public abstract boolean differently(Servlet servlet, Map<String, String> parameters);

}
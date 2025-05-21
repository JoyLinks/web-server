package com.joyzl.webserver.entities;

/**
 * 资源编辑
 * 
 * @author ZhangXi 2025年5月13日
 */
public class Webdav {

	private String path;
	private String content;

	/**
	 * 获取资源路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置资源路径
	 */
	public void setPath(String value) {
		path = value;
	}

	/**
	 * 获取内容目录
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 设置内容目录
	 */
	public void setContent(String value) {
		content = value;
	}
}
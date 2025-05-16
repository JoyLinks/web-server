package com.joyzl.webserver.entities;

import java.util.List;

/**
 * 资源请求
 * 
 * @author ZhangXi 2024年11月27日
 */
public class Resource {

	private String path;
	private String cache;
	private String content;
	private String error;

	private List<String> defaults;
	private List<String> compresses;
	private List<String> caches;
	private boolean browse;
	private boolean weak;

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
	 * 获取默认文件名
	 */
	public List<String> getDefaults() {
		return defaults;
	}

	/**
	 * 设置默认文件名
	 */
	public void setDefaults(List<String> values) {
		defaults = values;
	}

	/**
	 * 获取应压缩文件的扩展名（同时缓存）
	 */
	public List<String> getCompresses() {
		return compresses;
	}

	/**
	 * 设置应压缩文件的扩展名（同时缓存）
	 */
	public void setCompresses(List<String> values) {
		compresses = values;
	}

	/**
	 * 获取应缓存文件的扩展名
	 */
	public List<String> getCaches() {
		return caches;
	}

	/**
	 * 设置应缓存文件的扩展名
	 */
	public void setCaches(List<String> values) {
		caches = values;
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

	/**
	 * 获取缓存目录
	 */
	public String getCache() {
		return cache;
	}

	/**
	 * 设置缓存目录
	 */
	public void setCache(String value) {
		cache = value;
	}

	/**
	 * 获取错误页面目录
	 */
	public String getError() {
		return error;
	}

	/**
	 * 设置错误页面目录
	 */
	public void setError(String value) {
		error = value;
	}

	/**
	 * 获取是否使用弱验证器
	 */
	public boolean isWeak() {
		return weak;
	}

	/**
	 * 设置是否使用弱验证器
	 */
	public void setWeak(boolean value) {
		weak = value;
	}

	/**
	 * 获取是否可浏览目录
	 */
	public boolean isBrowse() {
		return browse;
	}

	/**
	 * 设置是否可浏览目录
	 */
	public void setBrowse(boolean value) {
		browse = value;
	}
}
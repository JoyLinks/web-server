package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务
 * 
 * @author ZhangXi 2024年11月12日
 */
public class Server {

	private int port;
	private String ip;

	private String cache;
	private String content;

	private List<String> defaults;
	private List<String> compresses;
	private List<String> caches;
	private final List<String> servlets = new ArrayList<>();
	private final List<Host> hosts = new ArrayList<>();

	public void start() throws Exception {
	}

	public void stop() {
	}

	/**
	 * 获取主机
	 */
	public List<Host> getHosts() {
		return hosts;
	}

	/**
	 * 设置主机
	 */
	public void setHosts(List<Host> values) {
		if (values != hosts) {
			hosts.clear();
			hosts.addAll(values);
		}
	}

	/**
	 * 获取服务组包名
	 */
	public List<String> getServlets() {
		return servlets;
	}

	/**
	 * 设置服务组包名
	 */
	public void setServlets(List<String> values) {
		if (values != servlets) {
			servlets.clear();
			servlets.addAll(values);
		}
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
	 * 获取监听端口
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 设置监听端口
	 */
	public void setPort(int value) {
		port = value;
	}

	/**
	 * 获取监听地址
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * 设置监听地址
	 */
	public void setIP(String value) {
		ip = value;
	}
}
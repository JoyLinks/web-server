package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务
 * 
 * @author ZhangXi 2024年11月12日
 */
public class Server extends Domain {

	private int port;
	private String ip;

	private final List<Host> hosts = new ArrayList<>();

	public void start() throws Exception {
	};

	public void stop() throws Exception {
	};

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
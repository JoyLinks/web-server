/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.service.HTTPSService;
import com.joyzl.webserver.service.HTTPService;
import com.joyzl.webserver.service.Service;

/**
 * 主服务
 * 
 * @author ZhangXi 2024年11月12日
 */
public class Server extends Domain {

	public static final String HTTP = "HTTP";
	public static final String HTTPS = "HTTPS";

	private int port;
	private String ip;
	private String type;
	private Integer backlog;

	private final List<Host> hosts = new ArrayList<>();

	/** 网络服务实例 */
	private Service server;

	/** 网络服务实例 */
	public Service server() {
		return server;
	}

	/** 重置网络服务实例 */
	public void reset() throws Exception {
		super.reset();
		for (Host host : hosts) {
			host.reset();
		}

		if (differently()) {
			if (server != null) {
				server.close();
			}
			if (type == HTTP) {
				server = new HTTPService(service(), ip, port, backlog());
			} else if (type == HTTPS) {
				server = new HTTPSService(service(), ip, port, backlog());
			} else {
				server = null;
			}
		}

		if (server != null) {
			server.virtuals().clear();
			for (Host host : hosts) {
				if (host.hasNames()) {
					for (String name : host.getNames()) {
						server.virtuals().put(name, host.service());
					}
				}
			}
		}
	}

	/** 检查管理对象参数与服务实例是否不同 */
	public boolean differently() {
		if (server == null) {
			return type != null;
		}
		if (type == null) {
			return true;
		}

		if (HTTP.equalsIgnoreCase(type)) {
			if (server instanceof HTTPService s) {
				if (!Utility.equal(ip, s.getIp())) {
					return true;
				}
				if (backlog() != s.getBacklog()) {
					return true;
				}
				if (port != s.getPort()) {
					return true;
				}
				return false;
			} else {
				return true;
			}
		}

		if (HTTPS.equalsIgnoreCase(type)) {
			if (server instanceof HTTPSService s) {
				if (!Utility.equal(ip, s.getIp())) {
					return true;
				}
				if (backlog() != s.getBacklog()) {
					return true;
				}
				if (port != s.getPort()) {
					return true;
				}
				return false;
			} else {
				return true;
			}
		}

		return true;
	}

	public synchronized void close() throws IOException {
		if (server != null) {
			server.close();
			server = null;
		}
		for (Host host : hosts) {
			host.close();
		}
		super.close();
	};

	////////////////////////////////////////////////////////////////////////////////

	public Host find(String name) {
		for (Host host : hosts) {
			if (Utility.same(name, host.getName())) {
				return host;
			}
		}
		return null;
	}

	/**
	 * 获取状态
	 */
	public boolean getState() {
		return server != null;
	}

	/**
	 * 获取协议类型
	 */
	public String getType() {
		return type;
	}

	/**
	 * 设置协议类型
	 */
	public void setType(String value) {
		if (HTTP.equalsIgnoreCase(value)) {
			type = HTTP;
		} else if (HTTPS.equalsIgnoreCase(value)) {
			type = HTTPS;
		} else {
			type = value;
		}
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

	/**
	 * 获取待连接的最大数量
	 */
	public int backlog() {
		return backlog == null ? 0 : backlog.intValue();
	}

	/**
	 * 获取待连接的最大数量
	 */
	public Integer getBacklog() {
		return backlog;
	}

	/**
	 * 设置待连接的最大数量
	 */
	public void setBacklog(Integer value) {
		backlog = value;
	}

	/**
	 * 比较不含内部集合对象(HOSTS)
	 */
	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) {
			if (o instanceof Server s) {
				return equals(this, s);
			}
		}
		return false;
	}

	/**
	 * 比较服务参数是否相同
	 */
	public static boolean equals(Server a, Server b) {
		if (a.port != b.port) {
			return false;
		}
		if (a.backlog != b.backlog) {
			if (a.backlog != null && b.backlog != null) {
				if (!a.backlog.equals(b.backlog)) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (a.ip != b.ip) {
			if (a.ip != null && b.ip != null) {
				if (!a.ip.equals(b.ip)) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (a.type != b.type) {
			if (a.type != null && b.type != null) {
				if (!a.type.equals(b.type)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

import com.joyzl.logger.Logger;
import com.joyzl.webserver.service.HTTPSService;
import com.joyzl.webserver.service.HTTPService;
import com.joyzl.webserver.service.Service;

/**
 * 服务
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

	private Service server;

	public synchronized void start() throws Exception {
		if (server == null) {
			if (type == HTTP) {
				reset();
				server = new HTTPService(service(), ip, port, backlog());
			} else if (type == HTTPS) {
				reset();
				server = new HTTPSService(service(), ip, port, backlog());
			} else {
				server = null;
				return;
			}
			for (Host host : hosts) {
				if (host.getNames().size() > 0) {
					host.reset();
					for (String name : host.getNames()) {
						server.virtuals().put(name, host.service());
					}
				}
			}
			Logger.info(getName(), " ", ip == null ? "ANY" : ip, ':', port);
		}
	};

	public synchronized void stop() throws Exception {
		if (server != null) {
			server.close();
			server = null;
		}
	};

	public int backlog() {
		return backlog == null ? 0 : backlog.intValue();
	}

	public Service server() {
		return server;
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
	public Integer getBacklog() {
		return backlog;
	}

	/**
	 * 设置待连接的最大数量
	 */
	public void setBacklog(Integer value) {
		backlog = value;
	}
}
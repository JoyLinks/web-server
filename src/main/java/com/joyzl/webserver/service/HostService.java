package com.joyzl.webserver.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.authenticate.Authenticate;
import com.joyzl.webserver.entities.Domain;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.Wildcards;

/**
 * 虚拟主机服务
 * 
 * @author ZhangXi 2025年6月7日
 */
public class HostService {

	private final Set<InetAddress> allows = new CopyOnWriteArraySet<>();
	private final Set<InetAddress> denies = new CopyOnWriteArraySet<>();
	private final List<Authenticate> authenticates = new CopyOnWriteArrayList<>();
	private final Wildcards<Servlet> servlets = new Wildcards<>();

	/** 获取路径匹配的服务程序 */
	public Servlet findServlet(String path) {
		return servlets.find(path);
	}

	/** 客户端地址是否被阻止 */
	public boolean deny(SocketAddress address) {
		if (address instanceof InetSocketAddress a) {
			if (allows.isEmpty()) {
				if (denies.isEmpty()) {
					return false;
				} else {
					return denies.contains(a.getAddress());
				}
			} else {
				return !allows.contains(a.getAddress());
			}
		}
		return true;
	}

	/** 客户端地址是否被允许 */
	public boolean allow(Domain domain, SocketAddress address) {
		if (address instanceof InetSocketAddress a) {
			if (allows.isEmpty()) {
				if (denies.isEmpty()) {
					return true;
				} else {
					return !denies.contains(a.getAddress());
				}
			} else {
				return allows.contains(a.getAddress());
			}
		}
		return false;
	}

	public boolean authenticate(Request request, Response response) {
		if (authenticates.isEmpty()) {
			return true;
		}

		boolean result = true;
		Authenticate authenticate;
		for (int index = 0; index < authenticates.size(); index++) {
			authenticate = authenticates.get(index);
			if (request.pathStart(authenticate.getPath())) {
				if (authenticate.require(request.getMethod())) {
					if (authenticate.verify(request, response)) {
						return true;
					} else {
						result = false;
					}
				}
			}
		}
		return result;
	}

	public void record(HTTPSlave slave, Request request) {

	}

	public void record(HTTPSlave slave, Response response) {

	}

	public Set<InetAddress> allows() {
		return allows;
	};

	public Set<InetAddress> denies() {
		return denies;
	};

	public List<Authenticate> authenticates() {
		return authenticates;
	};

	public Wildcards<Servlet> servlets() {
		return servlets;
	};
}
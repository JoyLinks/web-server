package com.joyzl.webserver.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.joyzl.logger.access.AccessLogger;
import com.joyzl.logger.access.AccessRecord;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.authenticate.Authenticate;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.Wildcards;

/**
 * 虚拟主机服务
 * 
 * @author ZhangXi 2025年6月7日
 */
public class HostService {

	private final List<Authenticate> authenticates = new CopyOnWriteArrayList<>();
	private final Wildcards<Servlet> servlets = new Wildcards<>();
	private volatile AccessLogger logger;
	private volatile String name;

	/** 获取路径匹配的服务程序 */
	public Servlet findServlet(String path) {
		return servlets.find(path);
	}

	/** 身份验证 */
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

	/** 设置主机名称 */
	public void name(String value) {
		name = value;
	}

	/** 设置访问日志 */
	public void access(String file) throws IOException {
		final AccessLogger a = logger;
		logger = null;
		if (a != null) {
			a.close();
		}
		if (file != null) {
			logger = new AccessLogger(file);
		}
	}

	/** 记录访问日志 */
	public void record(HTTPSlave slave, String host, Servlet servlet, Request request, Response response) {
		if (logger != null) {
			logger.record(new AccessRecord() {

				@Override
				public int getServerPort() {
					return ((InetSocketAddress) (slave.server().getLocalAddress())).getPort();
				}

				@Override
				public InetSocketAddress getRemoteAddress() {
					return (InetSocketAddress) slave.getRemoteAddress();
				}

				@Override
				public String getHost() {
					return host;
				}

				@Override
				public long getRequestTimestamp() {
					return request.getTimestamp();
				}

				@Override
				public String getRequestMethod() {
					return request.getMethod();
				}

				@Override
				public String getRequestURI() {
					return request.getURL();
				}

				@Override
				public String getRequestVersion() {
					return request.getVersion();
				}

				@Override
				public int getRequestBodySize() {
					try {
						return request.contentSize();
					} catch (IOException e) {
						return 0;
					}
				}

				@Override
				public String getServletName() {
					return servlet == null ? null : servlet.name();
				}

				@Override
				public int getServletSpend() {
					return (int) (System.currentTimeMillis() - request.getTimestamp());
				}

				@Override
				public int getResponseStatus() {
					return response.getStatus();
				}

				@Override
				public int getResponseBodySize() {
					try {
						return response.contentSize();
					} catch (IOException e) {
						return 0;
					}
				}
			});
		}
	}

	public List<Authenticate> authenticates() {
		return authenticates;
	};

	public Wildcards<Servlet> servlets() {
		return servlets;
	};

	public AccessLogger logger() {
		return logger;
	}

	public String name() {
		return name;
	}
}
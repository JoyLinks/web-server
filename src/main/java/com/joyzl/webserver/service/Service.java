package com.joyzl.webserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.logger.Logger;
import com.joyzl.network.Utility;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPServerHandler;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.web.WEBServlet;

/**
 * 基于超文本传输协议的网络服务，根据默认主机和虚拟主机的配置，分发请求到主机的服务程序。
 * 
 * @author ZhangXi 2025年6月7日
 */
public abstract class Service extends HTTPServerHandler {

	/** 默认主机 */
	private final HostService defaute;
	/** 虚拟主机映射<域名,虚拟主机> */
	private final Map<String, HostService> virtuals = new ConcurrentHashMap<>();
	private final long timestamp = System.currentTimeMillis();

	public Service(HostService service) {
		this.defaute = service;
	}

	/** 服务实例时刻时间戳 */
	public long timestamp() {
		return timestamp;
	}

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		if (Roster.intercept(slave.getRemoteAddress())) {
			// 连接阻止
			defaute.intercept();
			slave.close();
		} else {
			super.connected(slave);
		}
	}

	@Override
	public void received(HTTPSlave slave, Request request, Response response) {
		// Logger.debug(request);

		HostService service;
		final String host = request.getHeader(HTTP1.Host);
		if (Utility.noEmpty(host)) {
			// 虚拟主机服务集
			service = virtuals.get(host);
			if (service == null) {
				service = defaute;
			}
		} else {
			// 未指定主机名(Host)
			// reject(response, HTTPStatus.BAD_REQUEST);
			// 默认主机服务集
			service = defaute;
		}

		if (Roster.deny(service.name(), slave.getRemoteAddress())) {
			// 黑白名单阻止地址
			service.intercept();
			reject(response, HTTPStatus.FORBIDDEN);
			service.record(slave, host, null, request, response);
		} else {
			service.visit();
			if (Utility.noEmpty(request.getURL())) {
				if (service.authenticate(request, response)) {
					final Servlet servlet = service.findServlet(request.getPath());
					if (servlet != null) {
						try {
							servlet.service(slave, request, response);
							if (response.getStatus() > 0) {
								// HEADERS
								// response.setAttachHeaders(host.getHeaders());
							} else {
								// 挂起异步
								return;
							}
						} catch (Exception e) {
							// 处理对象内部错误
							reject(response, HTTPStatus.INTERNAL_SERVER_ERROR);
							Logger.error(e);
						}
						service.record(slave, host, servlet.name(), request, response);
					} else {
						// 无匹配服务程序(Servlet)
						reject(response, HTTPStatus.NOT_FOUND);
						service.record(slave, host, null, request, response);
					}
				} else {
					service.record(slave, host, null, request, response);
				}
			} else {
				// 未指定资源路径(URI)
				reject(response, HTTPStatus.BAD_REQUEST);
				service.record(slave, host, null, request, response);
			}
		}
		slave.send(response);
		// Logger.debug(response);
	}

	@Override
	public void error(HTTPSlave slave, Throwable e) {
		Logger.error(e);
	}

	/** 请求拒绝 */
	private void reject(Response response, HTTPStatus status) {
		response.addHeader(ContentLength.NAME, "0");
		response.addHeader(WEBServlet.DATE);
		response.setStatus(status);
	}

	public abstract void close();

	public Map<String, HostService> virtuals() {
		return virtuals;
	}
}
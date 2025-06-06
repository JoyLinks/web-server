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
	private final HostService defaut;
	/** 虚拟主机映射<域名,虚拟主机> */
	private final Map<String, HostService> virtuals = new ConcurrentHashMap<>();

	public Service(HostService service) {
		this.defaut = service;
	}

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		if (defaut.deny(slave.getRemoteAddress())) {
			// 黑名单阻止连接
			slave.close();
		} else {
			super.connected(slave);
		}
	}

	@Override
	public void received(HTTPSlave slave, Request request, Response response) {
		// Logger.debug(request);

		if (Utility.isEmpty(request.getURL())) {
			defaut.record(slave, request);
			// 未指定 URL/URI
			response(response, HTTPStatus.BAD_REQUEST);
		} else {
			final String name = request.getHeader(HTTP1.Host);
			if (Utility.isEmpty(name)) {
				defaut.record(slave, request);
				// 未指定 Host
				response(response, HTTPStatus.BAD_REQUEST);
			} else {
				final Servlet servlet;
				final HostService host = virtuals.get(name);
				if (host == null) {
					// DEFAULT
					defaut.record(slave, request);
					if (defaut.authenticate(request, response)) {
						servlet = defaut.findServlet(request.getPath());
						if (servlet == null) {
							// 无匹配处理程序 Servlet
							response(response, HTTPStatus.NOT_FOUND);
						} else {
							try {
								servlet.service(slave, request, response);
								if (response.getStatus() > 0) {
									// HEADERS
									// response.setAttachHeaders(defaut.getHeaders());
									// CONTINUE
								} else {
									// 挂起异步
									return;
								}
							} catch (Exception e) {
								// 处理对象内部错误
								response(response, HTTPStatus.INTERNAL_SERVER_ERROR);
								Logger.error(e);
							}
						}
					}
				} else {
					// HOST
					host.record(slave, request);
					if (host.deny(slave.getRemoteAddress())) {
						// 黑名单阻止
						response(response, HTTPStatus.FORBIDDEN);
					} else {
						if (host.authenticate(request, response)) {
							servlet = host.findServlet(request.getPath());
							if (servlet == null) {
								// 无匹配处理对象 Servlet
								response(response, HTTPStatus.NOT_FOUND);
							} else {
								try {
									servlet.service(slave, request, response);
									if (response.getStatus() > 0) {
										// HEADERS
										// response.setAttachHeaders(host.getHeaders());
										// CONTINUE
									} else {
										// 挂起异步
										return;
									}
								} catch (Exception e) {
									// 处理对象内部错误
									response(response, HTTPStatus.INTERNAL_SERVER_ERROR);
									Logger.error(e);
								}
							}
						}
					}
					host.record(slave, response);
					slave.send(response);
					return;
				}
			}
		}

		defaut.record(slave, response);
		slave.send(response);
		// Logger.debug(response);
	}

	@Override
	public void error(HTTPSlave slave, Throwable e) {
		Logger.error(e);
	}

	/** 异常响应 */
	private void response(Response response, HTTPStatus status) {
		response.addHeader(ContentLength.NAME, "0");
		response.addHeader(WEBServlet.DATE);
		response.setStatus(status);
	}

	public abstract void close();

	public Map<String, HostService> virtuals() {
		return virtuals;
	}
}
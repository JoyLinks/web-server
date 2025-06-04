package com.joyzl.webserver.service;

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
 * Server Handler
 * 
 * @author ZhangXi 2024年12月11日
 */
public final class Handler extends HTTPServerHandler {

	private final Server server;

	public Handler(Server server) {
		this.server = server;
	}

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		if (server.deny(slave.getRemoteAddress())) {
			// 黑名单阻止
			slave.close();
		} else {
			super.connected(slave);
		}
	}

	@Override
	public void received(HTTPSlave slave, Request request, Response response) {
		// Logger.debug(request);

		if (Utility.isEmpty(request.getURL())) {
			server.access().record(slave, request);
			// 未指定 URL/URI
			response(response, HTTPStatus.BAD_REQUEST);
		} else {
			final String name = request.getHeader(HTTP1.Host);
			if (Utility.isEmpty(name)) {
				server.access().record(slave, request);
				// 未指定 Host
				response(response, HTTPStatus.BAD_REQUEST);
			} else {
				final Servlet servlet;
				final Host host = server.findHost(name);
				if (host == null) {
					// SERVER
					server.access().record(slave, request);
					if (server.check(request, response)) {
						servlet = server.findServlet(request.getPath());
						if (servlet == null) {
							// 无匹配处理对象 Servlet
							response(response, HTTPStatus.NOT_FOUND);
						} else {
							try {
								servlet.service(slave, request, response);
								if (response.getStatus() > 0) {
									// HEADERS
									response.setAttachHeaders(server.getHeaders());
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
					host.access().record(slave, request);
					if (host.deny(slave.getRemoteAddress())) {
						// 黑名单阻止
						response(response, HTTPStatus.FORBIDDEN);
					} else {
						if (host.check(request, response)) {
							servlet = host.findServlet(request.getPath());
							if (servlet == null) {
								// 无匹配处理对象 Servlet
								response(response, HTTPStatus.NOT_FOUND);
							} else {
								try {
									servlet.service(slave, request, response);
									if (response.getStatus() > 0) {
										// HEADERS
										response.setAttachHeaders(host.getHeaders());
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
					host.access().record(slave, response);
					slave.send(response);
					return;
				}
			}
		}

		server.access().record(slave, response);
		slave.send(response);
		// Logger.debug(response);
	}

	/** 异常响应 */
	private void response(Response response, HTTPStatus status) {
		response.addHeader(ContentLength.NAME, "0");
		response.addHeader(WEBServlet.DATE);
		response.setStatus(status);
	}

	@Override
	public void error(HTTPSlave slave, Throwable e) {
		Logger.error(e);
	}
}
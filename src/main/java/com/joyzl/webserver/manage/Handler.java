package com.joyzl.webserver.manage;

import com.joyzl.logger.Logger;
import com.joyzl.network.Utility;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.HTTPServerHandler;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.WEBServlet;

public final class Handler extends HTTPServerHandler {

	private final Server server;

	public Handler(Server server) {
		this.server = server;
	}

	@Override
	public void received(HTTPSlave slave, Request request, Response response) {
		if (server.deny(slave.getRemoteAddress())) {
			server.access().record(slave, request);
			// 黑名单阻止
			response.addHeader(WEBServlet.DATE);
			response.setStatus(HTTPStatus.FORBIDDEN);
		} else {
			if (Utility.isEmpty(request.getURL())) {
				server.access().record(slave, request);
				// 未指定 URL/URI
				response.addHeader(WEBServlet.DATE);
				response.setStatus(HTTPStatus.BAD_REQUEST);
			} else {
				final String name = request.getHeader(com.joyzl.network.http.Host.NAME);
				if (Utility.isEmpty(name)) {
					server.access().record(slave, request);
					// 未指定 Host
					response.addHeader(WEBServlet.DATE);
					response.setStatus(HTTPStatus.BAD_REQUEST);
				} else {
					final Servlet servlet;
					final Host host = server.findHost(name);
					if (host == null) {
						server.access().record(slave, request);
						if (server.check(request, response)) {
							servlet = server.findServlet(request.getPath());
							if (servlet == null) {
								// 无匹配处理对象 Servlet
								response.addHeader(WEBServlet.DATE);
								response.setStatus(HTTPStatus.NOT_FOUND);
							} else {
								try {
									servlet.service(slave, request, response);
									if (response.getStatus() > 0) {
									} else {
										// 挂起异步
										return;
									}
								} catch (Exception e) {
									// 处理对象内部错误
									response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
									Logger.error(e);
								}
							}
						}
					} else {
						host.access().record(slave, request);
						if (host.deny(slave.getRemoteAddress())) {
							// 黑名单阻止
							response.addHeader(WEBServlet.DATE);
							response.setStatus(HTTPStatus.FORBIDDEN);
						} else {
							if (host.check(request, response)) {
								servlet = host.findServlet(request.getPath());
								if (servlet == null) {
									// 无匹配处理对象 Servlet
									response.setStatus(HTTPStatus.NOT_FOUND);
								} else {
									try {
										servlet.service(slave, request, response);
										if (response.getStatus() > 0) {
										} else {
											// 挂起异步
											return;
										}
									} catch (Exception e) {
										// 处理对象内部错误
										response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
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
		}
		server.access().record(slave, response);
		slave.send(response);
		// Logger.debug(response);
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		super.error(chain, e);
		Logger.error(e);
	}
}
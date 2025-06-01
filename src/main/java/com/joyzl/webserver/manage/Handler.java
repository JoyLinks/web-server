package com.joyzl.webserver.manage;

import com.joyzl.logger.Logger;
import com.joyzl.network.Utility;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.HTTPServerHandler;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.web.Servlet;
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
	public void received(HTTPSlave slave, Request request, Response response) {
		// TODO TEST
		// System.out.println(request);
		// for (Map.Entry<String, String> entry :
		// request.getHeaders().entrySet()) {
		// System.out.println(entry.getKey() + ":" + entry.getValue());
		// }
		// if (request.hasContent()) {
		// HTTP1Coder.toString((DataBuffer) request.getContent());
		// }

		// Logger.debug(request);
		if (server.deny(slave.getRemoteAddress())) {
			server.access().record(slave, request);
			// 黑名单阻止
			response(response, HTTPStatus.FORBIDDEN);
		} else {
			if (Utility.isEmpty(request.getURL())) {
				server.access().record(slave, request);
				// 未指定 URL/URI
				response(response, HTTPStatus.BAD_REQUEST);
			} else {
				final String name = request.getHeader(com.joyzl.network.http.Host.NAME);
				if (Utility.isEmpty(name)) {
					server.access().record(slave, request);
					// 未指定 Host
					response(response, HTTPStatus.BAD_REQUEST);
				} else {
					final Servlet servlet;
					final Host host = server.findHost(name);
					if (host == null) {
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
		}

		// TODO TEST
		// System.out.println(response);
		// for (Map.Entry<String, String> entry :
		// response.getHeaders().entrySet()) {
		// System.out.println(entry.getKey() + ":" + entry.getValue());
		// }

		server.access().record(slave, response);
		slave.send(response);
		// Logger.debug(response);
	}

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
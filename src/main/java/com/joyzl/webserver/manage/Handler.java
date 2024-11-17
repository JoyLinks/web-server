package com.joyzl.webserver.manage;

import com.joyzl.logger.Logger;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.WEBRequest;
import com.joyzl.network.web.WEBResponse;
import com.joyzl.network.web.WEBServerHandler;
import com.joyzl.network.web.WEBSlave;

public class Handler extends WEBServerHandler {

	private final Server server;

	public Handler(Server server) {
		this.server = server;
	}

	@Override
	public void received(WEBSlave slave, WEBRequest request, WEBResponse response) {
		if (Roster.isDeny(slave.getRemoteAddress())) {
			response.setStatus(HTTPStatus.FORBIDDEN);
		}

		final String host = request.getHeader(com.joyzl.network.http.Host.NAME);
		if (host == null || host.length() < 1) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
		} else {
			request.setURI(HTTPCoder.parseQuery(request.getURI(), request.getParametersMap()));

			final Servlet servlet = server.find(host, request.getURI());
			if (servlet == null) {
				response.setStatus(HTTPStatus.NOT_FOUND);
				slave.send(response);
			} else {
				try {
					servlet.service(slave, request, response);
				} catch (Exception e) {
					response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
					Logger.error(e);
				}
			}
		}
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		super.error(chain, e);
		Logger.error(e);
	}
}
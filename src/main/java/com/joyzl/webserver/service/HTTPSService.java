package com.joyzl.webserver.service;

import java.io.IOException;

import com.joyzl.network.http.HTTPServer;

public class HTTPSService extends Service {

	private final HTTPServer server;

	public HTTPSService(HostService s, String ip, int port, int backlog) throws IOException {
		super(s);
		if (backlog > 0) {
			server = new HTTPServer(this, ip, port, backlog);
		} else {
			server = new HTTPServer(this, ip, port);
		}
	}

	@Override
	public void close() {
		server.close();
	}
}
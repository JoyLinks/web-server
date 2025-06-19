package com.joyzl.webserver.service;

import java.io.IOException;

import com.joyzl.network.http.HTTPServer;

public class HTTPSService extends Service {

	private final HTTPServer server;
	private final String ip;
	private final int port;
	private final int backlog;

	public HTTPSService(HostService s, String ip, int port, int backlog) throws IOException {
		super(s);

		this.ip = ip;
		this.port = port;
		this.backlog = backlog;

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

	public int getBacklog() {
		return backlog;
	}

	public int getPort() {
		return port;
	}

	public String getIp() {
		return ip;
	}
}
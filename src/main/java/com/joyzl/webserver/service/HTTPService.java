/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.service;

import java.io.IOException;

import com.joyzl.network.http.HTTPServer;

public final class HTTPService extends Service {

	private final HTTPServer server;
	private final String ip;
	private final int port;
	private final int backlog;

	public HTTPService(HostService s, String ip, int port, int backlog) throws IOException {
		super(s);

		this.ip = ip;
		this.port = port;
		this.backlog = backlog;

		if (backlog > 0) {
			server = new HTTPServer(this, ip, port, backlog);
		} else {
			server = new HTTPServer(this, ip, port);
		}
		server.receive();
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
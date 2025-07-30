/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.service;

import java.io.IOException;

import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.http.HTTPServer;
import com.joyzl.network.tls.ApplicationLayerProtocolNegotiation;
import com.joyzl.network.tls.TLSParameters;
import com.joyzl.network.tls.TLSServerHandler;

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

		final TLSParameters parameters = new TLSParameters();
		parameters.setAlpns(//
			ApplicationLayerProtocolNegotiation.H2, //
			ApplicationLayerProtocolNegotiation.HTTP_1_1, //
			ApplicationLayerProtocolNegotiation.HTTP_1_0);
		final ChainHandler handler = new TLSServerHandler(this, parameters);
		if (backlog > 0) {
			server = new HTTPServer(handler, ip, port, backlog);
		} else {
			server = new HTTPServer(handler, ip, port);
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
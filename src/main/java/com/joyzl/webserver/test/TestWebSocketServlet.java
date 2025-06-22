/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.WEBSocketHandler;
import com.joyzl.network.http.WEBSocketMessage;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBSocket;

@ServletPath(path = "/ws")
public class TestWebSocketServlet extends WEBSocket {

	public TestWebSocketServlet(String path) {
		super(path);

	}

	@Override
	protected WEBSocketHandler create(HTTPSlave chain) {
		return new Handler();
	}

	class Handler extends com.joyzl.webserver.web.WEBSocketHandler {
		@Override
		public void connected(HTTPSlave slave) throws Exception {
			System.out.println("WEB Socket connected");

			final WEBSocketMessage message = new WEBSocketMessage();
			message.setType(WEBSocketMessage.TEXT);
			slave.send(message);
		}

		@Override
		public void received(HTTPSlave slave, WEBSocketMessage message) throws Exception {
			System.out.println(message);
			if (message.getContent() != null) {
				System.out.println(HTTP1Coder.toString((DataBuffer) message.getContent()));

			}
		}

		@Override
		public void sent(HTTPSlave slave, WEBSocketMessage message) throws Exception {

		}

		@Override
		public void disconnected(HTTPSlave slave) throws Exception {
			System.out.println("WEB Socket disconnected");

		}
	}
}
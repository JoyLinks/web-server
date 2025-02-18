package com.joyzl.webserver.servlets;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.WEBSocketHandler;
import com.joyzl.network.http.WEBSocketMessage;
import com.joyzl.network.web.ServletPath;
import com.joyzl.network.web.WEBSocket;

@ServletPath(path = "/ws")
public class TestWebSocketServlet extends WEBSocket {

	@Override
	protected WEBSocketHandler create(HTTPSlave chain) {
		return new Handler();
	}

	class Handler extends com.joyzl.network.web.WEBSocketHandler {
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
				System.out.println(HTTPCoder.toString((DataBuffer) message.getContent()));

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
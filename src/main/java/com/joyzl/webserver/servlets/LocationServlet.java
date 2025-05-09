package com.joyzl.webserver.servlets;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPClient;
import com.joyzl.network.http.HTTPClientHandler;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Host;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.network.web.MIMEType;
import com.joyzl.network.web.ServletPath;
import com.joyzl.network.web.WEBServlet;

@ServletPath(path = "/a5-test/location.cgi")
public class LocationServlet extends WEBServlet {

	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		final HTTPClientHandler handler = new HTTPClientHandler() {
			@Override
			public void connected(HTTPClient client) throws Exception {
				final Request r = new Request();
				r.setMethod(HTTP1.GET);
				r.setURL("/~mln/");
				r.addHeader(Host.NAME, "www.cs.odu.edu");
				client.send(r);
			}

			@Override
			public void received(HTTPClient client, Message message) throws Exception {
				final Response response = (Response) message;
				if (response.getStatus() == 301) {
					// https://www.cs.odu.edu/~mln/
					System.out.println(response.getHeader(HTTP1.Location));
					return;
				} else {
					System.out.println(HTTPCoder.toString((DataBuffer) response.getContent()));
				}

				final DataBuffer buffer = DataBuffer.instance();
				HTTPCoder.writeCommand(buffer, response);
				HTTPCoder.writeHeaders(buffer, response);
				HTTPCoder.writeContent(buffer, response);
				response.setContent(buffer);
				response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
				response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
				response(chain, response);
			}
		};
		final HTTPClient client = new HTTPClient(handler, "www.cs.odu.edu", 80);
		client.connect();
	}
}
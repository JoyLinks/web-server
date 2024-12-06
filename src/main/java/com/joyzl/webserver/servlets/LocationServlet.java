package com.joyzl.webserver.servlets;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPClient;
import com.joyzl.network.http.HTTPClientHandler;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.Host;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.network.web.MIMEType;
import com.joyzl.network.web.ServletURI;
import com.joyzl.network.web.WEBServlet;

@ServletURI(uri = "/a5-test/location.cgi")
public class LocationServlet extends WEBServlet {

	public void service(ChainChannel<Message> chain, Request request, Response response) throws Exception {
		final HTTPClientHandler handler = new HTTPClientHandler() {
			@Override
			public void connected(ChainChannel<Message> chain) throws Exception {
				final Request r = new Request();
				r.setMethod(HTTP.GET);
				r.setURL("/~mln/");
				r.addHeader(Host.NAME, "www.cs.odu.edu");
				chain.send(r);
			}

			@Override
			public void received(ChainChannel<Message> c, Message message) throws Exception {
				final Response rps = (Response) message;
				if (rps.getStatus() == 301) {
					// https://www.cs.odu.edu/~mln/
					System.out.println(rps.getHeader(HTTP.Location));
					return;
				} else {
					System.out.println(HTTPCoder.toString((DataBuffer) message.getContent()));
				}

				final DataBuffer buffer = DataBuffer.instance();
				HTTPCoder.writeCommand(buffer, rps);
				HTTPCoder.writeHeaders(buffer, rps);
				HTTPCoder.writeContent(buffer, rps);
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
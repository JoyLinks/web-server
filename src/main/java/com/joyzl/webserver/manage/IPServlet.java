/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBufferWriter;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 返回客户端请求IP地址
 * 
 * @author ZhangXi 2025年6月12日
 */
@ServletPath(path = "/manage/api/ip")
public class IPServlet extends CROSServlet {

	public IPServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "IP";
	}

	/** 可能的代理头 */
	final static String[] HEADERS = new String[] { //
			"X-Forwarded-For" //
			, "Proxy-Client-IP"//
			, "X-Real-IP" //
			, "WL-Proxy-Client-IP"//
			, "HTTP_CLIENT_IP"//
			, "HTTP_X_FORWARDED_FOR"//
	};

	@Override
	public void service(HTTPSlave slave, Request request, Response response) throws Exception {
		if (checkOrigin(request, response)) {
			if (HTTP1.OPTIONS.equals(request.getMethod())) {
				options(request, response);
			} else {
				final List<String> ip = ip(slave, request);

				final DataBufferWriter writer = new DataBufferWriter();
				Serializer.JSON().writeEntities(ip, writer);
				response.setContent(writer.buffer());
				response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
				response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
				response.addHeader(ContentLength.NAME, Long.toString(response.contentSize()));
			}
		} else {
			return;
		}
	}

	final static List<String> ip(HTTPSlave slave, Request request) {
		final List<String> ips = new ArrayList<>();
		ips.add(((InetSocketAddress) slave.getRemoteAddress()).getHostString());

		String ip;
		for (String name : HEADERS) {
			ip = request.getHeader(name);
			if (Utility.noEmpty(ip)) {
				if (ip.indexOf(',') > 0) {
					final String[] t = ip.split(",");
					if (t != null && t.length > 0) {
						for (String i : t) {
							ips.add(i.trim());
						}
					}
				} else {
					ips.add(ip.trim());
				}
			}
		}
		return ips;
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET";
	}

	@Override
	protected String allowHeaders() {
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
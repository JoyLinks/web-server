/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;
import com.joyzl.network.http.Request;

/**
 * 解析目标地址(URI)
 * 
 * @author ZhangXi 2025年6月3日
 */
public class Destination extends Header {

	public final static String NAME = HTTP1.Destination;

	private String uri;
	// SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	private int host = -1, port, path = -1, query, anchor;

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		return uri;
	}

	@Override
	public void setHeaderValue(String value) {
		uri = value;
		host = path = -1;
		port = query = anchor = 0;
		if (value != null) {
			host = value.indexOf(':');
			if (host >= 0) {
				host += 3;
				port = value.indexOf(':', host);
				if (port > 0) {
					port += 1;
				}
				path = value.indexOf('/', host);
				query = value.indexOf('?', path);
				anchor = value.indexOf('#', path);
			} else {
				path = 0;
				query = value.indexOf('?');
				anchor = value.indexOf('#');
			}
		}
	}

	public static Destination get(Request request) {
		final String value = request.getHeader(NAME);
		if (value == null || value.length() == 0) {
			return null;
		}
		final Destination destination = new Destination();
		destination.setHeaderValue(value);
		return destination;
	}

	/**
	 * 检查URL中的path部分是否与指定的路径匹配，此方法用于避免前缀匹配时创建新字符串对象
	 */
	public boolean pathStart(String base) {
		return uri.startsWith(base, path);
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public String getScheme() {
		if (host > 0) {
			return uri.substring(0, host - 3);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public String getHost() {
		if (host >= 0) {
			if (port > host) {
				return uri.substring(host, port - 1);
			} else //
			if (path > host) {
				return uri.substring(host, path);
			}
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public int getPort() {
		if (port > 0) {
			if (path > 0) {
				return Integer.parseUnsignedInt(uri, port, path, 10);
			}
			return Integer.parseUnsignedInt(uri, port, uri.length(), 10);
		}
		return 0;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public String getPath() {
		if (path == 0) {
			if (query > 0) {
				return uri.substring(path, query);
			}
			if (anchor > 0) {
				return uri.substring(path, anchor);
			}
			return uri;
		}
		if (path > 0) {
			if (query > 0) {
				return uri.substring(path, query);
			}
			if (anchor > 0) {
				return uri.substring(path, anchor);
			}
			return uri.substring(path);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public String getQuery() {
		if (query > 0) {
			if (anchor > 0) {
				return uri.substring(query, anchor);
			}
			return uri.substring(query);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?QUERY#ANCHOR
	 */
	public String getAnchor() {
		if (anchor > 0) {
			return uri.substring(anchor);
		}
		return null;
	}
}
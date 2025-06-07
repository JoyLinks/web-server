package com.joyzl.webserver.servlet;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.web.WEBServlet;

/**
 * 请求重定向
 * 
 * @author ZhangXi 2025年5月28日
 */
public class Location extends WEBServlet {

	private final String path;
	private final String location;
	private final HTTPStatus status;

	private final int pathWildcard;
	private final int locationWildcard;
	private final String locationPrefix, locationSuffix;

	public Location(String path, String location) {
		this(path, location, null);
	}

	public Location(String path, String location, HTTPStatus status) {
		if (status == null || status == HTTPStatus.UNKNOWN) {
			this.status = HTTPStatus.MOVED_PERMANENTLY;
		} else {
			this.status = status;
		}

		if (Utility.isEmpty(path) || Wildcards.STAR.equals(path)) {
			this.path = Wildcards.STAR;
		} else {
			this.path = path;
		}
		pathWildcard = path.indexOf(Wildcards.ANY);

		this.location = location;
		locationWildcard = location.indexOf(Wildcards.ANY);
		if (locationWildcard < 0) {
			locationPrefix = null;
			locationSuffix = null;
		} else if (locationWildcard == 0) {
			locationPrefix = null;
			locationSuffix = location.substring(locationWildcard + 1);
		} else if (locationWildcard == location.length() - 1) {
			locationPrefix = location.substring(0, locationWildcard);
			locationSuffix = null;
		} else {
			locationPrefix = location.substring(0, locationWildcard);
			locationSuffix = location.substring(locationWildcard + 1);
		}
	}

	// 域名重定向 example.com -> www.example.com
	// 协议重定向 http://www.example.com -> https://www.example.com
	// 路径重定向 /webdav -> /webdav/
	// 通配重定向 /images/* -> http://www.example.com/error.jpg
	// 通配重定向 /images/* -> http://www.example.com/*
	// 通配重定向 /images/ -> http://www.example.com/*
	// 替换规则：原路径通配部分替换目标路径通配部分，如果原路径无通配符则表示整个路径

	// 301 Permanent
	// 302 Found
	// 307 Temporary
	// 308 Permanent Redirect

	public void service(HTTPSlave slave, Request request, Response response) throws Exception {
		String uri;
		// 1提取原始路径的通配部分
		if (pathWildcard < 0) {
			// 无通配符时请求路径必然与匹配路径相同
			uri = path;
		} else if (path == Wildcards.STAR) {
			uri = request.getPath();
		} else if (pathWildcard == 0) {
			// *a
			uri = request.getPath();
			uri = uri.substring(0, uri.length() - path.length() - pathWildcard - 1);
		} else if (pathWildcard == path.length() - 1) {
			// a*
			uri = request.getPath();
			uri = uri.substring(pathWildcard);
		} else {
			// a*b
			uri = request.getPath();
			uri = request.getPath().substring(pathWildcard, uri.length() - path.length() - pathWildcard - 1);
		}
		// 2替换目标路径通配部分
		if (locationWildcard < 0) {
			uri = location;
		} else if (locationWildcard == 0) {
			uri = uri + locationSuffix;
		} else if (locationWildcard == location.length() - 1) {
			uri = locationPrefix + uri;
		} else {
			uri = locationPrefix + uri + locationSuffix;
		}

		response.addHeader(HTTP1.Location, uri);
		response.setStatus(status);
		response(slave, response);
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}

	public String getPath() {
		return path;
	}
}
package com.joyzl.webserver.service;

import com.joyzl.odbs.JSONName;
import com.joyzl.odbs.ODBS;
import com.joyzl.odbs.ODBSBinary;
import com.joyzl.odbs.ODBSJson;

/**
 * 序列化支持
 * 
 * @author ZhangXi 2024年11月13日
 */
public final class Serializer {

	private final static ODBS _ODBS;
	private final static ODBSBinary BINARY;
	private final static ODBSJson JSON;

	static {
		_ODBS = ODBS.initialize("com.joyzl.webserver/com.joyzl.webserver.entities");
		_ODBS.override(com.joyzl.webserver.service.Server.class);
		_ODBS.override(com.joyzl.webserver.service.Host.class);

		// 构建二进制序列化对象
		BINARY = new ODBSBinary(ODBS());
		// 构建二进制序列化对象
		JSON = new ODBSJson(ODBS());
		JSON.setKeyNameFormat(JSONName.LOWER_CASE);
	}

	public static ODBSJson JSON() {
		return JSON;
	}

	public static ODBSBinary BINARY() {
		return BINARY;
	}

	public static ODBS ODBS() {
		return _ODBS;
	}
}
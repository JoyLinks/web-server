package com.joyzl.webserver.manage;

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

	private final static ODBS odbs;
	private final static ODBSBinary binary;
	private final static ODBSJson json;

	static {
		odbs = ODBS.initialize("com.joyzl.webserver/com.joyzl.webserver.entities");
		odbs.override(com.joyzl.webserver.manage.Server.class);
		odbs.override(com.joyzl.webserver.manage.Host.class);

		// 构建二进制序列化对象
		binary = new ODBSBinary(getOdbs());
		// 构建二进制序列化对象
		json = new ODBSJson(getOdbs());
		json.setKeyNameFormat(JSONName.LOWER_CASE);
	}

	public static ODBSJson getJson() {
		return json;
	}

	public static ODBSBinary getBinary() {
		return binary;
	}

	public static ODBS getOdbs() {
		return odbs;
	}
}
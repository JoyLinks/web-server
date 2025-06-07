package com.joyzl.webserver.entities;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 用户
 * 
 * @author ZhangXi 2025年5月22日
 */
public class User {

	/** 可访问的资源 */
	private URI[] uris;
	/** 全局唯一名称 */
	private String name;
	/** 明文密码 */
	private String password;
	/** 是否可用 */
	private boolean enable;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean value) {
		enable = value;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String value) {
		password = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public String[] getURIs() {
		if (uris == null) {
			return null;
		}
		if (uris.length == 0) {
			return new String[0];
		}
		if (uris.length == 1) {
			return new String[] { uris[0].toString() };
		}

		final String items[] = new String[uris.length];
		for (int index = 0; index < items.length; index++) {
			items[index] = uris[index].toString();
		}
		return items;
	}

	public void setURIs(String[] values) throws URISyntaxException {
		if (values == null) {
			uris = null;
		} else if (values.length == 0) {
			uris = new URI[0];
		} else if (values.length == 1) {
			uris = new URI[] { new URI(values[0]) };
		} else {
			final URI items[] = new URI[values.length];
			for (int index = 0; index < items.length; index++) {
				items[index] = new URI(values[index]);
			}
			uris = items;
		}
	}

	public URI[] URIs() {
		return uris;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof User u) {
			return name.equals(u.name);
		}
		return false;
	}
}
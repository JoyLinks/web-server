package com.joyzl.webserver.webdav.elements;

/**
 * 客户端提供的锁持有人信息
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Owner extends Element implements Href {

	private String href;

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public void setHref(String value) {
		href = value;
	}
}
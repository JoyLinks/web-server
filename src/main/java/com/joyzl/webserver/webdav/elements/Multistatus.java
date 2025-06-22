/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.webdav.elements;

import java.util.ArrayList;
import java.util.List;

import com.joyzl.network.http.HTTP1;

/**
 * 多状态返回
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Multistatus extends Element implements ResponseDescription {
	/*-
	 * <!ELEMENT multistatus (response*, responsedescription?) >
	 */

	private String version = HTTP1.V11;
	private final List<Response> responses = new ArrayList<>();
	private String description;

	public Multistatus() {
	}

	public Multistatus(String version) {
		this.version(version);
	}

	public List<Response> getResponses() {
		return responses;
	}

	public void setResponses(List<Response> values) {
		if (responses != values) {
			responses.clear();
			responses.addAll(values);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String value) {
		description = value;
	}

	public String version() {
		return version;
	}

	public void version(String value) {
		version = value;
	}
}
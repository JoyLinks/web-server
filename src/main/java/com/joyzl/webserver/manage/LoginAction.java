/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

/**
 * 身份验证
 * 
 * @author ZhangXi 2025年6月13日
 */
public class LoginAction extends Action {

	private String name;
	private String password;

	@Override
	protected void execute() throws Exception {
		// TODO Auto-generated method stub

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
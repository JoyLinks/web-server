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
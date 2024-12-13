package com.joyzl.webserver.entities;

/**
 * 身份验证
 * 
 * @author ZhangXi 2024年11月27日
 */
public class Authenticate {

	private String path;
	private String type;
	private String realm;
	private String users;
	private String algorithm;

	/**
	 * 获取须验证的路径，指定路径及其子路径须身份验证
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置须验证的路径，指定路径及其子路径须身份验证
	 */
	public void setPath(String value) {
		path = value;
	}

	/**
	 * 获取用户信息存储位置
	 */
	public String getUsers() {
		return users;
	}

	/**
	 * 设置用户信息存储位置
	 */
	public void setUsers(String value) {
		users = value;
	}

	/**
	 * 获取身份验证提示信息
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * 设置身份验证提示信息
	 */
	public void setRealm(String value) {
		realm = value;
	}

	/**
	 * 获取身份验证类型
	 */
	public String getType() {
		return type;
	}

	/**
	 * 设置身份验证类型
	 */
	public void setType(String value) {
		type = value;
	}

	/**
	 * 获取用户信息加密方式
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * 设置用户信息加密方式
	 */
	public void setAlgorithm(String value) {
		algorithm = value;
	}
}
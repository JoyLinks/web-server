/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

/**
 * ODBS Controller Action
 * 
 * @author ZhangXi 2025年6月13日
 */
public abstract class Action implements Runnable {

	private int state;

	@Override
	public void run() {
		try {
			execute();
		} catch (Exception e) {

		}
	}

	protected abstract void execute() throws Exception;

	public int getState() {
		return state;
	}

	public void setState(int value) {
		state = value;
	}
}
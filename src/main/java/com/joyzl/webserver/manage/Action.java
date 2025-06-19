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
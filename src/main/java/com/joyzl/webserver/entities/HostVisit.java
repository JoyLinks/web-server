package com.joyzl.webserver.entities;

/**
 * 访问量
 * 
 * @author ZhangXi 2025年6月15日
 */
public class HostVisit {

	/** 服务名 */
	private String name;
	/** 访问量 */
	private long visits;
	/** 拦截量 */
	private long intercepts;
	/** 是否有记录访问日志 */
	private boolean logs;

	public long getVisits() {
		return visits;
	}

	public void setVisits(long value) {
		visits = value;
	}

	public long getIntercepts() {
		return intercepts;
	}

	public void setIntercepts(long value) {
		intercepts = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public boolean isLogs() {
		return logs;
	}

	public void setLogs(boolean value) {
		logs = value;
	}
}
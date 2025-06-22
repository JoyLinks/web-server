/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
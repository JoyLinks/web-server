package com.joyzl.webserver.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问量
 * 
 * @author ZhangXi 2025年6月15日
 */
public class ServerVisit extends HostVisit {

	private long timestamp;
	private List<HostVisit> hosts = new ArrayList<>();

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<HostVisit> getHosts() {
		return hosts;
	}

	public void setHosts(List<HostVisit> values) {
		if (values != hosts) {
			hosts.clear();
			hosts.addAll(values);
		}
	}
}

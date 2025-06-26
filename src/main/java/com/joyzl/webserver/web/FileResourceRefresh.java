package com.joyzl.webserver.web;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.joyzl.logger.Logger;
import com.joyzl.network.Executor;

/**
 * 刷新缓存的文件资源，定时检查文件资源变化移除过期的文件缓存
 * 
 * @author ZhangXi 2025年6月26日
 */
public class FileResourceRefresh implements Runnable {

	/** 检查时间(秒) */
	private final int time = 60 * 9;
	/** 资源映射 path,Resource */
	private final Map<String, WEBResource> resources;
	private ScheduledFuture<?> scheduled;

	public FileResourceRefresh(Map<String, WEBResource> rs) {
		resources = rs;
		scheduled = Executor.schedule(this, time, TimeUnit.SECONDS);
	}

	public void close() {
		if (scheduled != null) {
			scheduled.cancel(true);
			scheduled = null;
		}
	}

	@Override
	public void run() {
		if (scheduled == null) {
			return;
		}
		if (scheduled.isCancelled()) {
			return;
		}
		try {
			refresh();
		} catch (Exception e) {
			Logger.error(e);
		}
		scheduled = Executor.schedule(this, time, TimeUnit.SECONDS);
	}

	void refresh() throws Exception {
		final Iterator<Entry<String, WEBResource>> iterator = resources.entrySet().iterator();
		Entry<String, WEBResource> entry;
		while (iterator.hasNext()) {
			entry = iterator.next();
			if (entry.getValue() instanceof FileResource fr) {
				if (fr.getFile().exists()) {
					if (fr.getModified() < fr.getFile().lastModified()) {
						resources.remove(entry.getKey());
						fr.clear();
					}
				} else {
					resources.remove(entry.getKey());
				}
				continue;
			}
			if (entry.getValue() instanceof DirectoryResource dr) {
				if (dr.getDirectory().exists()) {
					dr.clear();
				} else {
					resources.remove(entry.getKey());
				}
			}
		}
	}
}
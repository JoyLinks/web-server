/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver;

import com.joyzl.logger.Logger;
import com.joyzl.logger.LoggerCleaner;
import com.joyzl.logger.LoggerService;

/**
 * 守护
 * 
 * @author ZhangXi 2025年6月1日
 */
public class Daemon implements Runnable {

	final static Daemon INSTANCE = new Daemon();

	private Daemon() {
	}

	public static void execute() {
		INSTANCE.run();
	}

	@Override
	public void run() {
		while (true) {
			long e = 86400 - System.currentTimeMillis() % 86400;
			try {
				Thread.sleep(e);

				final LoggerCleaner cleaner = LoggerService.clean();
				Logger.info(cleaner);
			} catch (InterruptedException x) {

			}
		}
	}
}
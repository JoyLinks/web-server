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
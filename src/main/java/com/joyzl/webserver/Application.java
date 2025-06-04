package com.joyzl.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.joyzl.logger.LogSetting;
import com.joyzl.logger.Logger;
import com.joyzl.network.Executor;
import com.joyzl.webserver.service.Service;
import com.joyzl.webserver.service.Users;

/**
 * WEB HTTP Server
 * 
 * @author ZhangXi TEL:13883833982
 * @date 2024年3月7日
 */
public class Application {

	private static Application instance = null;

	private Application() {
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			start(args);
		} else {
			if ("start".equalsIgnoreCase(args[0])) {
				start(args);
			} else//
			if ("stop".equalsIgnoreCase(args[0])) {
				stop(args);
			} else {
				//
			}
		}
	}

	public static void start(String[] args) {
		if (instance == null) {
			instance = new Application();
			try {
				instance.initialize();
			} catch (Exception e) {
				Logger.error(e);
				try {
					instance.destroy();
				} catch (Exception e1) {
					Logger.error(e);
				} finally {
					instance = null;
				}
				return;
			}
			try {
				instance.start();
			} catch (Exception e) {
				Logger.error(e);
				try {
					instance.stop();
				} catch (Exception e1) {
					Logger.error(e);
				}
				try {
					instance.destroy();
				} catch (Exception e1) {
					Logger.error(e);
				} finally {
					instance = null;
				}
				return;
			}
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					stop(args);
				}
			}, "SHUTDOWN"));
			try {
				synchronized (instance) {
					instance.wait();
				}
			} catch (InterruptedException e) {
				Logger.error("主线程意外终止");
			}
		}
	}

	public static void stop(String[] args) {
		if (instance != null) {
			synchronized (instance) {
				instance.notifyAll();
			}
			try {
				instance.stop();
			} catch (Exception e) {
				Logger.error(e);
			}
			try {
				instance.destroy();
			} catch (Exception e) {
				Logger.error(e);
			} finally {
				instance = null;
			}
		}
	}

	/**
	 * 载入配置文件
	 */
	public static Properties loadProperties() {
		final File file = new File("server.properties");
		final Properties properties = new Properties();
		if (file.exists()) {
			try (FileInputStream input = new FileInputStream(file)) {
				properties.load(input);
			} catch (IOException e) {
				// 必须成功加载配置文件，否则无法运行
				throw new RuntimeException(e);
			}
		}
		return properties;
	}

	void initialize() throws Exception {
		Logger.info("INITIALIZE");

		// 载入配置文件
		final Properties properties = loadProperties();
		LogSetting.LEVEL = Utility.value(properties.getProperty("LOG_LEVEL"), 1);
		LogSetting.EXPIRES = Utility.value(properties.getProperty("LOG_EXPIRES"), 30);

		// 初始化线程池
		Executor.initialize(Utility.value(properties.getProperty("THREAD"), 0));
		Logger.info("THREAD SIZE: " + Executor.getThreadSize());

		// Logger.debug(Serializer.ODBS().checkString());

		// 初始化用户集
		Users.initialize(properties.getProperty("USERS"));

		// 初始化服务集
		Service.initialize(properties.getProperty("SERVERS"));
	}

	void start() throws Exception {
		Logger.info("START");
		Service.start();
	}

	void stop() throws Exception {
		Logger.info("STOP");
		Service.stop();
	}

	void destroy() throws Exception {
		Logger.info("DESTROY");
		try {
			Service.destroy();
		} finally {
			Executor.shutdown();
		}
	}
}
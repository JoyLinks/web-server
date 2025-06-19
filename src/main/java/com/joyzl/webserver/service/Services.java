package com.joyzl.webserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Authentication;
import com.joyzl.webserver.entities.Domain;
import com.joyzl.webserver.entities.Host;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.entities.Servlet;

/**
 * 服务集
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Services {

	/** 管理实例 */
	private static final List<Server> SERVERS = new ArrayList<>();
	/** 初始化的配置文件 */
	private static File file;

	private Services() {
	}

	/**
	 * 读取配置文件初始化服务
	 */
	public static void initialize(String servers) throws IOException, ParseException {
		file = new File(servers);
		if (file.exists() && file.isFile()) {
			if (Utility.ends(file.getPath(), ".json", true)) {
				try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
					Serializer.JSON().readEntities(SERVERS, Server.class, reader);
				}
			} else {
				try (final FileInputStream input = new FileInputStream(file)) {
					Serializer.BINARY().readEntities(SERVERS, input);
				}
			}
		}
	}

	public static void start() throws Exception {
		for (Server server : SERVERS) {
			server.reset();
		}
	}

	public static void stop() throws Exception {
		for (Server server : SERVERS) {
			server.close();
		}
	}

	public static void destroy() throws Exception {
		stop();
		SERVERS.clear();
		file = null;
	}

	public static void save() throws IOException, ParseException {
		if (Utility.ends(file.getPath(), ".json", true)) {
			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				Serializer.JSON().writeEntities(SERVERS, writer);
			}
		} else {
			try (final FileOutputStream output = new FileOutputStream(file, false)) {
				Serializer.BINARY().writeEntities(SERVERS, output);
			}
		}
	}

	/** 热重置服务 */
	public static void apply(List<Server> freshs) throws Exception {
		// 首先更改参数
		if (SERVERS != freshs) {
			if (SERVERS.isEmpty()) {
				SERVERS.addAll(freshs);
			} else {
				Server fs, us;
				int fi = 0, ui = 0;
				for (; fi < freshs.size(); fi++, ui++) {
					fs = freshs.get(fi);
					if (fs == null) {
						// 忽略空对象
						ui--;
						continue;
					}

					if (ui < SERVERS.size()) {
						us = SERVERS.get(ui);
						if (us == null) {
							// 替换空位
							SERVERS.set(ui, us = fs);
						} else {
							// 主域服务实例
							if (Domain.equals(fs, us)) {
								// 主域服务参数相同
							} else {
								// 主域服务更新参数
								us.setName(fs.getName());
								us.setAccess(fs.getAccess());
							}
							// 主域身份验证
							applyAuthentications(fs, us);
							// 主域服务程序
							applyServlets(fs, us);
							// 子域服务
							applyHosts(fs, us);

							// 网络服务实例
							if (Server.equals(fs, us)) {
								// 参数相同
							} else {
								// 更新参数
								us.setBacklog(fs.getBacklog());
								us.setType(fs.getType());
								us.setPort(fs.getPort());
								us.setIP(fs.getIP());
							}
						}
					} else {
						SERVERS.add(fs);
					}
				}

				// 移除并关闭额外的服务
				while (SERVERS.size() > ui) {
					us = SERVERS.remove(SERVERS.size() - 1);
					if (us != null) {
						us.close();
					}
				}
			}
		}

		// 其次重置服务
		// 重置方法将判断参数并执行必要的调整
		for (Server server : SERVERS) {
			server.reset();
		}
	}

	private static void applyHosts(Server fresh, Server using) throws IOException {
		if (fresh == using) {
			return;
		}

		// freshIndex,usingIndex
		int fi = 0, ui = 0;

		Host fh, uh;
		for (; fi < fresh.getHosts().size(); fi++, ui++) {
			fh = fresh.getHosts().get(fi);
			if (fh == null) {
				// 忽略空对象
				ui--;
				continue;
			}

			if (ui < using.getHosts().size()) {
				uh = using.getHosts().get(ui);
				if (uh == null) {
					// 替换空位为新对象
					using.getHosts().set(ui, fh);
				} else {
					if (Host.equals(fh, uh)) {
						// 参数相同
					} else {
						// 更新参数
						uh.setNames(fh.getNames());
					}

					// 域服务实例
					if (Domain.equals(fh, uh)) {
						// 域服务参数相同
					} else {
						// 域服务更新参数
						uh.setName(fh.getName());
						uh.setAccess(fh.getAccess());
					}
					// 域身份验证
					applyAuthentications(fh, uh);
					// 域服务程序
					applyServlets(fh, uh);
				}
			} else {
				// 添加新的服务
				using.getHosts().add(fh);
			}
		}
		// 移除额外的域服实例
		while (using.getHosts().size() > ui) {
			uh = using.getHosts().remove(using.getHosts().size() - 1);
			if (uh != null) {
				uh.close();
			}
		}
	}

	/**
	 * 将新的域参数应用到当前运行的域服务实例
	 */
	private static void applyAuthentications(Domain fresh, Domain using) {
		if (fresh == using) {
			return;
		}

		// freshIndex,usingIndex
		int fi = 0, ui = 0;

		// 身份验证服务实例
		Authentication fa, ua;
		for (; fi < fresh.getAuthentications().size(); fi++, ui++) {
			fa = fresh.getAuthentications().get(fi);
			if (fa == null) {
				// 忽略空对象
				ui--;
				continue;
			}

			if (ui < using.getAuthentications().size()) {
				ua = using.getAuthentications().get(ui);
				if (ua == null) {
					// 替换空位为新对象
					using.getAuthentications().set(ui, fa);
				} else {
					if (fa.equals(ua)) {
						// 参数相同
						continue;
					} else {
						// 更新参数
						ua.setType(fa.getType());
						ua.setPath(fa.getPath());
						ua.setRealm(fa.getRealm());
						ua.setAlgorithm(fa.getAlgorithm());
						ua.setMethods(fa.getMethods());
					}
				}
			} else {
				// 添加新的服务
				using.getAuthentications().add(fa);
			}
		}
		// 移除额外的身份验证服务实例
		while (using.getAuthentications().size() > ui) {
			using.getAuthentications().remove(using.getAuthentications().size() - 1);
		}
	}

	/**
	 * 将新的域参数应用到当前运行的域服务实例
	 */
	private static void applyServlets(Domain fresh, Domain using) {
		if (fresh == using) {
			return;
		}

		// freshIndex,usingIndex
		int fi = 0, ui = 0;

		// 服务程序服务实例
		Servlet fs, us;
		for (fi = 0, ui = 0; fi < fresh.getServlets().size(); fi++, ui++) {
			fs = fresh.getServlets().get(fi);
			if (fs == null) {
				// 忽略空对象
				ui--;
				continue;
			}

			if (ui < using.getServlets().size()) {
				us = using.getServlets().get(ui);
				if (us == null) {
					// 替换空位为新对象
					using.getServlets().set(ui, fs);
				} else {
					if (fs.equals(us)) {
						// 参数相同
						continue;
					} else {
						// 更新参数
						us.setType(fs.getType());
						us.setPath(fs.getPath());
						us.setHeaders(fs.getHeaders());
						us.setParameters(fs.getParameters());
					}
				}
			} else {
				// 添加新的服务
				using.getServlets().add(fs);
			}
		}
		// 移除额外的服务程序服务实例
		while (using.getServlets().size() > ui) {
			using.getServlets().remove(using.getServlets().size() - 1);
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	public static Server find(String name) {
		for (Server server : SERVERS) {
			if (Utility.same(name, server.getName())) {
				return server;
			}
		}
		return null;
	}

	public static List<Server> all() {
		return Collections.unmodifiableList(SERVERS);
	}

	public static Server get(int index) {
		return SERVERS.get(index);
	}

	public static synchronized void add(Server server) {
		SERVERS.add(server);
	}

	public static synchronized Server remove(String name) {
		int index = 0;
		Server server;
		for (; index < SERVERS.size(); index++) {
			server = SERVERS.get(index);
			if (Utility.same(name, server.getName())) {
				break;
			}
		}
		return SERVERS.remove(index);
	}

	public static synchronized void remove(Server server) {
		SERVERS.remove(server);
	}
}
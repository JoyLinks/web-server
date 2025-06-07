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
import com.joyzl.webserver.entities.Server;

/**
 * 服务集
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Services {

	private static final List<Server> SERVERS = new ArrayList<>();
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
			server.start();
		}
	}

	public static void stop() throws Exception {
		for (Server server : SERVERS) {
			server.stop();
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

	////////////////////////////////////////////////////////////////////////////////

	public static List<Server> all() {
		return Collections.unmodifiableList(SERVERS);
	}

	public static Server find(String name) {
		for (Server server : SERVERS) {
			if (Utility.same(name, server.getName())) {
				return server;
			}
		}
		return null;
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
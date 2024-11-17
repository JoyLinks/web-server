package com.joyzl.webserver.manage;

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

import com.joyzl.webserver.entities.Server;

/**
 * 服务管理
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Manager {

	private static final List<Server> SERVERS = new ArrayList<>();
	private static File file;

	private Manager() {
	}

	public static void initialize(String servers) throws IOException, ParseException {
		file = new File(servers);
		if (file.exists() && file.isFile()) {
			try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
				List<?> entities = (List<?>) Serializer.getJson().readEntity(Server.class, reader);
				if (entities != null && entities.size() > 0) {
					for (int index = 0; index < entities.size(); index++) {
						SERVERS.add((Server) entities.get(index));
					}
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
	}

	public static List<Server> all() {
		return Collections.unmodifiableList(SERVERS);
	}

	public static void add(Server server) {
		SERVERS.add(server);
	}

	public static void save() throws Exception {
		try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			Serializer.getJson().writeEntity(SERVERS, writer);
		}
	}
}
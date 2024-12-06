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

import com.joyzl.network.web.AuthenticateBasic;
import com.joyzl.network.web.AuthenticateBearer;
import com.joyzl.network.web.AuthenticateDigest;
import com.joyzl.network.web.DiskFileServlet;
import com.joyzl.network.web.FileResourceServlet;
import com.joyzl.network.web.RAMFileServlet;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Resource;
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

	/**
	 * 读取配置文件初始化服务
	 */
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

	public static FileResourceServlet instance(Resource resource) throws IOException {
		final FileResourceServlet servlet;
		if (Utility.noEmpty(resource.getCache())) {
			if ("RAM".equals(resource.getCache())) {
				servlet = new RAMFileServlet(resource.getContent());
				if (resource.getCaches() != null) {
					((RAMFileServlet) servlet).setCaches(resource.getCaches());
				}
			} else {
				servlet = new DiskFileServlet(resource.getContent(), resource.getCache());
			}
		} else {
			servlet = new DiskFileServlet(resource.getContent());
		}

		servlet.setErrorPages(resource.getError());
		servlet.setBrowse(resource.isBrowse());
		servlet.setWeak(resource.isWeak());
		if (resource.getDefaults() != null) {
			servlet.setDefaults(resource.getDefaults());
		}
		if (resource.getCompresses() != null) {
			servlet.setCompresses(resource.getCompresses());
		}
		return servlet;
	}

	public static com.joyzl.network.web.Authenticate instance(Authenticate authenticate) throws IOException {
		if (AuthenticateBasic.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateBasic a = new AuthenticateBasic(authenticate.getURI());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			a.setUsers(authenticate.getUsers());
			return a;
		}
		if (AuthenticateDigest.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateDigest a = new AuthenticateDigest(authenticate.getURI());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			a.setUsers(authenticate.getUsers());
			return a;
		}
		if (AuthenticateBearer.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateBearer a = new AuthenticateBearer(authenticate.getURI());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			a.setUsers(authenticate.getUsers());
			return a;
		}
		throw new IllegalArgumentException("Authenticate.Type:" + authenticate.getType());
	}
}
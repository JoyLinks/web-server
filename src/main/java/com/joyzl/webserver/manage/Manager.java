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

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.authenticate.AuthenticateBasic;
import com.joyzl.webserver.authenticate.AuthenticateBearer;
import com.joyzl.webserver.authenticate.AuthenticateDigest;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Location;
import com.joyzl.webserver.entities.Resource;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.entities.Webdav;
import com.joyzl.webserver.web.FileResourceServlet;
import com.joyzl.webserver.webdav.FileWEBDAVServlet;

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
			if (Utility.ends(file.getPath(), ".json", true)) {
				try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
					final List<Server> entities = Serializer.JSON().readEntities(Server.class, reader);
					if (entities != null && entities.size() > 0) {
						SERVERS.addAll(entities);
					}
				}
			} else {
				try (final FileInputStream input = new FileInputStream(file)) {
					final List<Server> entities = Serializer.BINARY().readEntities(input);
					if (entities != null && entities.size() > 0) {
						SERVERS.addAll(entities);
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

	public static void save() throws IOException {
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

	public static void add(Server server) {
		SERVERS.add(server);
	}

	static FileWEBDAVServlet instance(Webdav webdav) throws IOException {
		final FileWEBDAVServlet servlet = new FileWEBDAVServlet(webdav.getPath(), webdav.getContent());
		return servlet;
	}

	static com.joyzl.webserver.servlets.Location instance(Location location) throws IOException {
		return new com.joyzl.webserver.servlets.Location(location.getPath(), location.getLocation(), HTTPStatus.fromCode(location.getStatus()));
	}

	static FileResourceServlet instance(Resource resource) throws IOException {
		final FileResourceServlet servlet;
		if (Utility.noEmpty(resource.getCache())) {
			servlet = new FileResourceServlet(resource.getPath(), resource.getContent(), resource.getCache());
		} else {
			servlet = new FileResourceServlet(resource.getPath(), resource.getContent());
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
		if (resource.getCaches() != null) {
			servlet.setCaches(resource.getCaches());
		}
		return servlet;
	}

	static com.joyzl.webserver.authenticate.Authenticate instance(Authenticate authenticate) throws IOException {
		if (AuthenticateBasic.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateBasic a = new AuthenticateBasic(authenticate.getPath());
			a.setPreflight(authenticate.getPreflight());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			return a;
		}
		if (AuthenticateDigest.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateDigest a = new AuthenticateDigest(authenticate.getPath());
			a.setPreflight(authenticate.getPreflight());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			return a;
		}
		if (AuthenticateBearer.TYPE.equalsIgnoreCase(authenticate.getType())) {
			final AuthenticateBearer a = new AuthenticateBearer(authenticate.getPath());
			a.setPreflight(authenticate.getPreflight());
			a.setAlgorithm(authenticate.getAlgorithm());
			a.setRealm(authenticate.getRealm());
			return a;
		}
		throw new IllegalArgumentException("Authenticate.Type:" + authenticate.getType());
	}
}
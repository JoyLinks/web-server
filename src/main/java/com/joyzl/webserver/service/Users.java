package com.joyzl.webserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Request;
import com.joyzl.webserver.entities.User;

/**
 * 用户管理，用户调整即刻生效
 * 
 * @author ZhangXi 2025年5月24日
 */
public class Users {

	private final static ConcurrentHashMap<String, User> USERS = new ConcurrentHashMap<>();
	private static File file;

	private Users() {
	}

	/**
	 * 读取配置文件初始化服务
	 */
	public static void initialize(String users) throws IOException, ParseException {
		file = new File(users);
		if (file.exists() && file.isFile()) {
			if (Utility.ends(file.getPath(), ".json", true)) {
				try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
					final List<User> entities = Serializer.JSON().readEntities(User.class, reader);
					if (entities != null && entities.size() > 0) {
						for (User user : entities) {
							USERS.put(user.getName(), user);
						}
					}
				}
			} else {
				try (final FileInputStream input = new FileInputStream(file)) {
					final List<User> entities = Serializer.BINARY().readEntities(input);
					if (entities != null && entities.size() > 0) {
						for (User user : entities) {
							USERS.put(user.getName(), user);
						}
					}
				}
			}
		}
	}

	public static void destroy() throws Exception {
		USERS.clear();
	}

	public static void save() throws IOException {
		if (Utility.ends(file.getPath(), ".json", true)) {
			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				Serializer.JSON().writeEntities(USERS.values(), writer);
			}
		} else {
			try (final FileOutputStream output = new FileOutputStream(file, false)) {
				Serializer.BINARY().writeEntities(USERS.values(), output);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 指定用户名获取用户
	 * 
	 * @param username
	 * @return User / null
	 */
	public static User get(String username) {
		return USERS.get(username);
	}

	public static Collection<User> all() {
		return Collections.unmodifiableCollection(USERS.values());
	}

	public static boolean add(User user) {
		if (USERS.containsKey(user.getName())) {
			// throw new IllegalArgumentException("用户已存在");
			return false;
		}
		USERS.put(user.getName(), user);
		return true;
	}

	public static User remove(String username) {
		return USERS.remove(username);
	}

	/**
	 * 检查用户是否可执行请求，既允许的URI
	 * 
	 * @param request
	 * @param user
	 * @return true / false
	 */
	public static boolean check(Request request, User user) {
		if (user == null) {
			return false;
		}
		if (user.isEnable()) {
			if (user.URIs() == null) {
				return false;
			}
			URI uri;
			for (int index = 0; index < user.URIs().length; index++) {
				uri = user.URIs()[index];
				if (uri.getHost() != null) {
					if (Utility.same(uri.getHost(), request.getHeader(HTTP1.Host))) {
						// HOST OK
					} else {
						continue;
					}
				}

				if (uri.getPath() != null) {
					if (request.pathStart(uri.getPath())) {
						return true;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}
}
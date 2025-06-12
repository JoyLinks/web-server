package com.joyzl.webserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.logger.Logger;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Address;

/**
 * 地址黑白名单
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Roster {

	private final static Map<String, Address> ADDRESSES = new ConcurrentHashMap<>();
	private static File file;

	// 白名单优先级高于黑名单
	// 如果配置白名单则除此之外的所有地址被阻止
	// 如果配置黑名单则除此之外的所有地址都允许
	// Server / Host

	private Roster() {
	}

	/**
	 * 读取配置文件初始化服务
	 */
	public static void initialize(String roster) throws IOException, ParseException {
		file = new File(roster);
		if (file.exists() && file.isFile()) {
			final List<Address> addresses = new ArrayList<>();
			if (Utility.ends(file.getPath(), ".json", true)) {
				try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
					Serializer.JSON().readEntities(addresses, Address.class, reader);
				}
			} else {
				try (final FileInputStream input = new FileInputStream(file)) {
					Serializer.BINARY().readEntities(addresses, input);
				}
			}
			for (Address address : addresses) {
				add(address);
			}

			Logger.debug("ROSTER:", addresses.size());
		}
	}

	public static void destroy() throws Exception {
		file = null;
		clear();
	}

	public static void save() throws IOException, ParseException {
		if (Utility.ends(file.getPath(), ".json", true)) {
			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				Serializer.JSON().writeEntities(ADDRESSES.values(), writer);
			}
		} else {
			try (final FileOutputStream output = new FileOutputStream(file, false)) {
				Serializer.BINARY().writeEntities(ADDRESSES.values(), output);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	private final static Map<InetAddress, Address> INTERCEPTS = new ConcurrentHashMap<>();
	private final static Map<InetAddress, Address> IPS = new ConcurrentHashMap<>();

	// private final static BLACKS=null;
	// private final static WHITES=null;

	/** 客户端地址是否被阻止连接 */
	public static boolean intercept(SocketAddress sa) {
		if (sa instanceof InetSocketAddress a) {
			return INTERCEPTS.containsKey(a.getAddress());
		}
		return true;
	}

	/** 客户端地址是否被阻止访问 */
	public static boolean deny(String name, SocketAddress sa) {
		// 黑名单内拒绝访问，反之允许；
		// 白名单内允许访问，反之拒绝；
		if (sa instanceof InetSocketAddress a) {
			Address address = IPS.get(a.getAddress());

		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////

	public static Collection<Address> all() {
		return Collections.unmodifiableCollection(ADDRESSES.values());
	}

	public static Address get(String address) {
		return ADDRESSES.get(address);
	}

	public static void add(Address address) {
		if (Utility.isEmpty(address.getAddress())) {
			return;
		}
		final Address previous = ADDRESSES.put(address.getAddress(), address);
		if (address.inetAddress() != null) {
			if (address.hasHost()) {
				IPS.put(address.inetAddress(), address);
			} else {
				INTERCEPTS.put(address.inetAddress(), address);
			}
		}
		if (previous != null) {
			if (previous.inetAddress() != null) {
				IPS.remove(previous.inetAddress(), previous);
			}
		}
	}

	public static void remove(Address address) {
		if (ADDRESSES.remove(address.getAddress(), address)) {
			if (address.inetAddress() != null) {
				IPS.remove(address.inetAddress(), address);
			}
		}
	}

	public static Address remove(String address) {
		final Address a = ADDRESSES.remove(address);
		if (a != null) {
			if (a.inetAddress() != null) {
				IPS.remove(a.inetAddress(), a);
			}
		}
		return a;
	}

	public static void clear() {
		ADDRESSES.clear();
		IPS.clear();
	}
}
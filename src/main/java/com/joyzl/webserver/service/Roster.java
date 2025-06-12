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
import java.util.Set;
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

	/** 缓存用于管理 */
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
	// TODO 实现更适合InetAddress的Set集合，Java没有合适的Set集合
	// TODO 实现IP地址段阻止或通过，例如：192.168.0.*

	/** 全局名单 */
	private final static Set<InetAddress> BLACKS = ConcurrentHashMap.newKeySet();
	private final static Set<InetAddress> WHITES = ConcurrentHashMap.newKeySet();
	/** 域名单 */
	private final static Map<String, DomainList> DOMAINS = new ConcurrentHashMap<>();

	/** 客户端地址是否被阻止连接 */
	public static boolean intercept(SocketAddress sa) {
		if (sa instanceof InetSocketAddress a) {
			if (BLACKS.contains(a.getAddress())) {
				return true;
			}
			if (WHITES.isEmpty()) {
				return false;
			}
			return !WHITES.contains(a.getAddress());
		}
		return true;
	}

	/** 客户端地址是否被阻止访问 */
	public static boolean deny(String name, SocketAddress sa) {
		// 黑名单内拒绝访问，反之允许；
		// 白名单内允许访问，反之拒绝；
		if (sa instanceof InetSocketAddress a) {
			final DomainList d = DOMAINS.get(name);
			if (d != null) {
				if (d.BLACKS.contains(a.getAddress())) {
					return true;
				}
				if (d.WHITES.isEmpty()) {
					return false;
				}
				return !d.WHITES.contains(a.getAddress());
			} else {
				return false;
			}
		}
		return true;
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
		addToDomain(address);
		if (previous != null) {
			removeFromDomain(previous);
		}
	}

	public static void remove(Address address) {
		if (ADDRESSES.remove(address.getAddress(), address)) {
			removeFromDomain(address);
		}
	}

	public static Address remove(String address) {
		final Address a = ADDRESSES.remove(address);
		if (a != null) {
			removeFromDomain(a);
		}
		return a;
	}

	public static void clear() {
		ADDRESSES.clear();
		DOMAINS.clear();
	}

	/**
	 * 域名单，为每个域提供黑白名单
	 */
	static class DomainList {
		final Set<InetAddress> BLACKS = ConcurrentHashMap.newKeySet();
		final Set<InetAddress> WHITES = ConcurrentHashMap.newKeySet();
	}

	static void addToDomain(Address address) {
		if (address.inetAddress() != null) {
			if (address.hasHost()) {
				DomainList dl;
				for (String name : address.getHost()) {
					dl = DOMAINS.get(name);
					if (dl == null) {
						DOMAINS.put(name, dl = new DomainList());
					}
					if (address.isAllow()) {
						dl.WHITES.add(address.inetAddress());
					} else {
						dl.BLACKS.add(address.inetAddress());
					}
				}
			} else {
				if (address.isAllow()) {
					WHITES.add(address.inetAddress());
				} else {
					BLACKS.add(address.inetAddress());
				}
			}
		}
	}

	static void removeFromDomain(Address address) {
		if (address.inetAddress() != null) {
			if (address.hasHost()) {
				DomainList dl;
				for (String name : address.getHost()) {
					dl = DOMAINS.get(name);
					if (dl != null) {
						if (address.isAllow()) {
							dl.WHITES.remove(address.inetAddress());
						} else {
							dl.BLACKS.remove(address.inetAddress());
						}
						if (dl.WHITES.isEmpty() && dl.BLACKS.isEmpty()) {
							DOMAINS.remove(name, dl);
						}
					}
				}
			} else {
				if (address.isAllow()) {
					WHITES.remove(address.inetAddress());
				} else {
					BLACKS.remove(address.inetAddress());
				}
			}
		}
	}
}
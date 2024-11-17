package com.joyzl.webserver.manage;

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
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joyzl.webserver.entities.Address;

/**
 * 黑白名单
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Roster {

	private static final List<Address> ADDRESSES = new ArrayList<>();
	private static Map<InetAddress, Address> ALLOWS = new HashMap<>();
	private static Map<InetAddress, Address> DENIES = new HashMap<>();
	private static File file;

	public static void initialize(String blacks) throws IOException, ParseException {
		file = new File(blacks);
		if (file.exists() && file.isFile()) {
			try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
				List<?> entities = (List<?>) Serializer.getJson().readEntity(Address.class, reader);
				if (entities != null && entities.size() > 0) {
					for (int index = 0; index < entities.size(); index++) {
						add((Address) entities.get(index));
					}
				}
			}
		}
	}

	public static boolean isDeny(SocketAddress address) {
		if (ALLOWS.isEmpty()) {
			if (DENIES.isEmpty()) {
				return false;
			} else {
				final InetSocketAddress a = (InetSocketAddress) address;
				return DENIES.containsKey(a.getAddress());
			}
		} else {
			final InetSocketAddress a = (InetSocketAddress) address;
			return !ALLOWS.containsKey(a.getAddress());
		}
	}

	public static boolean isAllow(SocketAddress address) {
		if (ALLOWS.isEmpty()) {
			if (DENIES.isEmpty()) {
				return true;
			} else {
				final InetSocketAddress a = (InetSocketAddress) address;
				return !DENIES.containsKey(a.getAddress());
			}
		} else {
			final InetSocketAddress a = (InetSocketAddress) address;
			return ALLOWS.containsKey(a.getAddress());
		}
	}

	public static Collection<Address> all() {
		return Collections.unmodifiableCollection(ADDRESSES);
	}

	public static void add(Address address) throws UnknownHostException {
		final InetAddress a = InetAddress.getByName(address.getHost());
		if (address.isAllow()) {
			ALLOWS.put(a, address);
		} else {
			DENIES.put(a, address);
		}
		ADDRESSES.add(address);
	}

	public static void remove(Address address) throws UnknownHostException {
		final InetAddress a = InetAddress.getByName(address.getHost());
		if (address.isAllow()) {
			ALLOWS.remove(a, address);
		} else {
			DENIES.remove(a, address);
		}
		ADDRESSES.remove(address);
	}

	public static void save() throws Exception {
		try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			Serializer.getJson().writeEntity(ADDRESSES, writer);
		}
	}
}
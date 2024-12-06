package com.joyzl.webserver.manage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.webserver.entities.Address;

/**
 * 地址黑白名单
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Roster {

	private final Map<InetAddress, Address> ALLOWS = new HashMap<>();
	private final Map<InetAddress, Address> DENIES = new HashMap<>();

	public final boolean isDeny(SocketAddress address) {
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

	public final boolean isAllow(SocketAddress address) {
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

	public final void add(Address address) throws UnknownHostException {
		final InetAddress a = InetAddress.getByName(address.getHost());
		if (address.isAllow()) {
			ALLOWS.put(a, address);
		} else {
			DENIES.put(a, address);
		}
	}

	public final void remove(Address address) throws UnknownHostException {
		final InetAddress a = InetAddress.getByName(address.getHost());
		if (address.isAllow()) {
			ALLOWS.remove(a, address);
		} else {
			DENIES.remove(a, address);
		}
	}

	public void clear() {
		ALLOWS.clear();
		DENIES.clear();
	}
}
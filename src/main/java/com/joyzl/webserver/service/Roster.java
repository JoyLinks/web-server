package com.joyzl.webserver.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.webserver.entities.Address;

/**
 * 地址黑白名单
 * 
 * @author ZhangXi 2024年11月12日
 */
public final class Roster {

	private final Map<String, Address> ADDRESS = new ConcurrentHashMap<>();

	private final Map<InetAddress, Address> ALLOWS = new ConcurrentHashMap<>();
	private final Map<InetAddress, Address> DENIES = new ConcurrentHashMap<>();

	// 白名单优先级高于黑名单
	// 如果配置白名单则除此之外的所有地址被阻止
	// 如果配置黑名单则除此之外的所有地址都允许
	// Server / Host

	private Roster() {
	}

	/** 地址是否禁止 */
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

	/** 地址是否允许 */
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
		final InetAddress a = InetAddress.getByName(address.getAddress());
		if (address.isAllow()) {
			ALLOWS.put(a, address);
		} else {
			DENIES.put(a, address);
		}
	}

	public final void remove(Address address) throws UnknownHostException {
		final InetAddress a = InetAddress.getByName(address.getAddress());
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
package com.joyzl.webserver.manage;

import java.util.HashMap;
import java.util.Map;

import com.joyzl.logger.Logger;
import com.joyzl.network.web.DiskFileServlet;
import com.joyzl.network.web.RAMFileServlet;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.WEBServer;
import com.joyzl.network.web.Wildcards;
import com.joyzl.webserver.Utility;

public final class Server extends com.joyzl.webserver.entities.Server {

	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private final Map<String, Host> HOSTS = new HashMap<>();
	private WEBServer server;

	@Override
	public void start() throws Exception {
		if (server != null) {
			return;
		}
		if (getPort() <= 0 || getPort() >= 65535) {
			return;
		}

		SERVLETS.clear();
		Utility.scanServlets(SERVLETS, getServlets());
		if (Utility.noEmpty(getContent())) {
			if (Utility.noEmpty(getCache())) {
				if ("RAM".equals(getCache())) {
					final RAMFileServlet servlet = new RAMFileServlet(getContent());
					if (getDefaults() != null) {
						servlet.setDefaults(getDefaults());
					}
					if (getCompresses() != null) {
						servlet.setCompresses(getCompresses());
					}
					if (getCaches() != null) {
						servlet.setCaches(getCaches());
					}
					SERVLETS.bind("*", servlet);
				} else {
					final DiskFileServlet servlet = new DiskFileServlet(getContent(), getCache());
					if (getDefaults() != null) {
						servlet.setDefaults(getDefaults());
					}
					if (getCompresses() != null) {
						servlet.setCompresses(getCompresses());
					}
					SERVLETS.bind("*", servlet);
				}
			} else {
				final DiskFileServlet servlet = new DiskFileServlet(getContent());
				if (getDefaults() != null) {
					servlet.setDefaults(getDefaults());
				}
				if (getCompresses() != null) {
					servlet.setCompresses(getCompresses());
				}
				SERVLETS.bind("*", servlet);
			}
		}

		HOSTS.clear();
		for (com.joyzl.webserver.entities.Host host : getHosts()) {
			if (host.getHosts().size() > 0) {
				host.reset();
				for (String name : host.getHosts()) {
					HOSTS.put(name, (Host) host);
				}
			}
		}
		server = new WEBServer(new Handler(this), getIP(), getPort());

		Logger.info("SERVER:", getIP(), getPort());
	}

	@Override
	public void stop() {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	public Servlet find(String host, String uri) {
		final Host h = HOSTS.get(host);
		if (h == null) {
			return SERVLETS.find(uri);
		} else {
			return h.find(uri);
		}
	}
}
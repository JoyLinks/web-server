package com.joyzl.webserver.manage;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.logger.Logger;
import com.joyzl.network.http.HTTPServer;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.Authenticates;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.Wildcards;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Resource;
import com.joyzl.webserver.manage.Access.AccessCommonLogger;

public final class Server extends com.joyzl.webserver.entities.Server {

	private final Roster ROSTER = new Roster();
	private final Authenticates AUTHENTICATES = new Authenticates();
	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private final Map<String, Host> HOSTS = new HashMap<>();
	private Access access = Access.EMPTY;

	private HTTPServer server;

	public void start() throws Exception {
		if (server != null) {
			return;
		}
		if (getPort() <= 0 || getPort() >= 65535) {
			return;
		}

		ROSTER.clear();
		for (Address address : getRoster()) {
			ROSTER.add(address);
		}

		SERVLETS.clear();
		Utility.scanServlets(SERVLETS, getServlets());
		for (Resource resource : getResources()) {
			if (Utility.noEmpty(resource.getContent())) {
				if (Utility.isEmpty(resource.getURI())) {
					SERVLETS.bind("*", Manager.instance(resource));
				} else {
					SERVLETS.bind(resource.getURI(), Manager.instance(resource));
				}
			}
		}

		AUTHENTICATES.clear();
		for (Authenticate authenticate : getAuthenticates()) {
			AUTHENTICATES.addAuthenticate(Manager.instance(authenticate));
		}

		HOSTS.clear();
		for (com.joyzl.webserver.entities.Host host : getHosts()) {
			if (host.getNames().size() > 0) {
				host.reset();
				for (String name : host.getNames()) {
					HOSTS.put(name, (Host) host);
				}
			}
		}

		if (Utility.noEmpty(getAccess())) {
			access = new AccessCommonLogger(getAccess());
		} else {
			access = Access.EMPTY;
		}

		server = new HTTPServer(new Handler(this), getIP(), getPort());
		Logger.info("SERVER:", getIP(), getPort());
	}

	public void stop() {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	public boolean deny(SocketAddress address) {
		return ROSTER.isDeny(address);
	}

	public boolean check(Request request, Response response) {
		return AUTHENTICATES.check(request, response);
	}

	public Host findHost(String host) {
		return HOSTS.get(host);
	}

	public Servlet findServlet(String uri) {
		return SERVLETS.find(uri);
	}

	public Access access() {
		return access;
	}
}
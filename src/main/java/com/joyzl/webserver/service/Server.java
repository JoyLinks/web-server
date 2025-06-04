package com.joyzl.webserver.service;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.logger.Logger;
import com.joyzl.network.http.HTTPServer;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.authenticate.Authenticates;
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Location;
import com.joyzl.webserver.entities.Resource;
import com.joyzl.webserver.entities.Webdav;
import com.joyzl.webserver.service.Access.AccessCommonLogger;
import com.joyzl.webserver.servlet.Servlet;
import com.joyzl.webserver.servlet.Wildcards;

public final class Server extends com.joyzl.webserver.entities.Server {

	private final Roster ROSTER = new Roster();
	private final Authenticates AUTHENTICATES = new Authenticates();
	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private final Map<String, Host> HOSTS = new HashMap<>();
	private Access access;

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

		// LOCATION
		for (Location location : getLocations()) {
			if (Utility.noEmpty(location.getLocation())) {
				if (Utility.isEmpty(location.getPath())) {
					SERVLETS.bind(Wildcards.STAR, Service.instance(location));
				} else {
					SERVLETS.bind(location.getPath(), Service.instance(location));
				}
			}
		}
		// WEBDAV
		for (Webdav webdav : getWebdavs()) {
			if (Utility.noEmpty(webdav.getContent())) {
				if (Utility.isEmpty(webdav.getPath())) {
					SERVLETS.bind(Wildcards.STAR, Service.instance(webdav));
				} else {
					SERVLETS.bind(webdav.getPath(), Service.instance(webdav));
				}
			}
		}
		// WEB Resource
		for (Resource resource : getResources()) {
			if (Utility.noEmpty(resource.getContent())) {
				if (Utility.isEmpty(resource.getPath())) {
					SERVLETS.bind(Wildcards.STAR, Service.instance(resource));
				} else {
					SERVLETS.bind(resource.getPath(), Service.instance(resource));
				}
			}
		}

		AUTHENTICATES.clear();
		for (Authenticate authenticate : getAuthenticates()) {
			AUTHENTICATES.addAuthenticate(Service.instance(authenticate));
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
			access = new Access();
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
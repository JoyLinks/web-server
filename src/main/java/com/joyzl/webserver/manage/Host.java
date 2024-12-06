package com.joyzl.webserver.manage;

import java.net.SocketAddress;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.Authenticates;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.Wildcards;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Resource;

public class Host extends com.joyzl.webserver.entities.Host {

	private final Roster ROSTER = new Roster();
	private final Authenticates AUTHENTICATES = new Authenticates();
	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();

	public void reset() throws Exception {
		ROSTER.clear();
		for (Address address : getRoster()) {
			ROSTER.add(address);
		}

		SERVLETS.clear();
		Utility.scanServlets(SERVLETS, getServlets());
		for (Resource resource : getResources()) {
			if (Utility.isEmpty(resource.getURI())) {
				SERVLETS.bind("*", Manager.instance(resource));
			} else {
				SERVLETS.bind(resource.getURI(), Manager.instance(resource));
			}
		}

		AUTHENTICATES.clear();
		for (Authenticate authenticate : getAuthenticates()) {
			AUTHENTICATES.addAuthenticate(Manager.instance(authenticate));
		}
	}

	public boolean deny(SocketAddress address) {
		return ROSTER.isDeny(address);
	}

	public boolean check(Request request, Response response) {
		return AUTHENTICATES.check(request, response);
	}

	public final Servlet findServlet(String uri) {
		return SERVLETS.find(uri);
	}
}
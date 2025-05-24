package com.joyzl.webserver.manage;

import java.net.SocketAddress;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.authenticate.Authenticates;
import com.joyzl.webserver.entities.Address;
import com.joyzl.webserver.entities.Authenticate;
import com.joyzl.webserver.entities.Resource;
import com.joyzl.webserver.entities.Webdav;
import com.joyzl.webserver.manage.Access.AccessCommonLogger;
import com.joyzl.webserver.web.Servlet;
import com.joyzl.webserver.web.Wildcards;

public class Host extends com.joyzl.webserver.entities.Host {

	private final Roster ROSTER = new Roster();
	private final Authenticates AUTHENTICATES = new Authenticates();
	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private Access access;

	public void reset() throws Exception {
		ROSTER.clear();
		for (Address address : getRoster()) {
			ROSTER.add(address);
		}

		SERVLETS.clear();
		Utility.scanServlets(SERVLETS, getServlets());
		for (Resource resource : getResources()) {
			if (Utility.noEmpty(resource.getContent())) {
				if (Utility.isEmpty(resource.getPath())) {
					SERVLETS.bind("*", Manager.instance(resource));
				} else {
					SERVLETS.bind(resource.getPath(), Manager.instance(resource));
				}
			}
		}
		for (Webdav webdav : getWebdavs()) {
			if (Utility.noEmpty(webdav.getContent())) {
				if (Utility.isEmpty(webdav.getPath())) {
					SERVLETS.bind("*", Manager.instance(webdav));
				} else {
					SERVLETS.bind(webdav.getPath(), Manager.instance(webdav));
				}
			}
		}

		AUTHENTICATES.clear();
		for (Authenticate authenticate : getAuthenticates()) {
			AUTHENTICATES.addAuthenticate(Manager.instance(authenticate));
		}

		if (Utility.noEmpty(getAccess())) {
			access = new AccessCommonLogger(getAccess());
		} else {
			access = new Access();
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

	public Access access() {
		return access;
	}
}
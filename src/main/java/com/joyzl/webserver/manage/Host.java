package com.joyzl.webserver.manage;

import com.joyzl.network.web.DiskFileServlet;
import com.joyzl.network.web.RAMFileServlet;
import com.joyzl.network.web.Servlet;
import com.joyzl.network.web.Wildcards;
import com.joyzl.webserver.Utility;

public class Host extends com.joyzl.webserver.entities.Host {

	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();

	@Override
	public void reset() throws Exception {
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
	}

	public final Servlet find(String key) {
		return SERVLETS.find(key);
	}
}
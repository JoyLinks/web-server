package com.joyzl.webserver.webdav;

import com.joyzl.network.Utility;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.MIMEType;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Propfind;
import com.joyzl.webserver.webdav.elements.Propstat;

public class FileWEBDAVServlet extends WEBDAVServlet {

	final static int XML = 1, JSON = 2;

	public FileWEBDAVServlet() {
	}

	private int check(Request request) {
		final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
		if (contentType != null) {
			if (Utility.same(MIMEType.APPLICATION_JSON, contentType.getType())) {
				return JSON;
			} else //
			if (Utility.same(MIMEType.APPLICATION_XML, contentType.getType())) {
				return XML;
			}
		}
		return 0;
	}

	@Override
	protected void propfind(Request request, Response response) throws Exception {
		final int type = check(request);

		// propfind
		final Propfind propfind;
		if (type == XML) {
			propfind = XMLCoder.read(Propfind.class, request);
		} else if (type == JSON) {
			propfind = JSONCoder.read(Propfind.class, request);
		} else {
			propfind = null;
		}

		if (propfind != null) {
			if (propfind.isAllprop()) {
				if (propfind.isPropname()) {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
				if (propfind.getProp().size() > 0) {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}
			}
			if (propfind.isPropname()) {

			}
			if (propfind.getProp().size() > 0) {

			}
		}

		// multistatus
		final Multistatus multistatus = new Multistatus();
		multistatus.getResponses().add(null);
		com.joyzl.webserver.webdav.elements.Response r = new com.joyzl.webserver.webdav.elements.Response();
		r.getPropstats().add(null);
		Propstat propstat = new Propstat();
		propstat.setDescription(null);
		propstat.setProp(null);
		propstat.setStatus(null);

		if (type == XML) {
			XMLCoder.write(multistatus, response);
		} else if (type == JSON) {
			JSONCoder.write(multistatus, response);
		}
	}

	@Override
	protected void proppatch(Request request, Response response) throws Exception {
		// propertyupdate
		// multistatus
	}

	@Override
	protected void mkcol(Request request, Response response) throws Exception {
		// NO Content
		request.getPath();

	}

	@Override
	protected void delete(Request request, Response response) throws Exception {
		request.getHeader(HTTP1.Depth);
		request.getPath();
		// multistatus

	}

	@Override
	protected void put(Request request, Response response) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void copy(Request request, Response response) throws Exception {
		request.getHeader(HTTP1.Destination);
		request.getHeader(HTTP1.Overwrite);
		request.getPath();

		// multistatus
	}

	@Override
	protected void move(Request request, Response response) throws Exception {
		// TODO Auto-generated method stub
		// multistatus
	}

	@Override
	protected void lock(Request request, Response response) throws Exception {
		// lockinfo
		// prop
		// multistatus
	}

	@Override
	protected void unlock(Request request, Response response) throws Exception {
		// No Content

	}

}
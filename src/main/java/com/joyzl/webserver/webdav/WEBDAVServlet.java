package com.joyzl.webserver.webdav;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.servlet.CROSServlet;

/**
 * WEBDAV
 * 
 * @author ZhangXi 2025年2月14日
 */
public abstract class WEBDAVServlet extends CROSServlet {

	@Override
	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		if (request.getVersion() == HTTP1.V20 || request.getVersion() == HTTP1.V11 || request.getVersion() == HTTP1.V10) {
			if (checkOrigin(request, response)) {
				switch (request.getMethod()) {
					case HTTP1.PROPFIND:
						propfind(request, response);
						break;
					case HTTP1.PROPPATCH:
						proppatch(request, response);
						break;
					case HTTP1.MKCOL:
						mkcol(request, response);
						break;
					case HTTP1.COPY:
						copy(request, response);
						break;
					case HTTP1.MOVE:
						move(request, response);
						break;
					case HTTP1.LOCK:
						lock(request, response);
						break;
					case HTTP1.UNLOCK:
						unlock(request, response);
						break;
					case HTTP1.GET:
						get(request, response);
						break;
					case HTTP1.HEAD:
						head(request, response);
						break;
					case HTTP1.POST:
						post(request, response);
						break;
					case HTTP1.PUT:
						put(request, response);
						break;
					case HTTP1.PATCH:
						patch(request, response);
						break;
					case HTTP1.DELETE:
						delete(request, response);
						break;
					case HTTP1.TRACE:
						trace(request, response);
						break;
					case HTTP1.OPTIONS:
						options(request, response);
						break;
					case HTTP1.CONNECT:
						connect(request, response);
						break;
					default:
						response.setStatus(HTTPStatus.BAD_REQUEST);
				}
			}
		} else {
			response.setStatus(HTTPStatus.VERSION_NOT_SUPPORTED);
		}
		response(chain, response);
	}

	protected abstract void propfind(Request request, Response response) throws Exception;

	protected abstract void proppatch(Request request, Response response) throws Exception;

	protected abstract void mkcol(Request request, Response response) throws Exception;

	protected abstract void copy(Request request, Response response) throws Exception;

	protected abstract void move(Request request, Response response) throws Exception;

	protected abstract void lock(Request request, Response response) throws Exception;

	protected abstract void unlock(Request request, Response response) throws Exception;

	@Override
	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	protected void patch(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	protected void connect(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	protected void options(Request request, Response response) {
		super.options(request, response);
		response.addHeader(HTTP1.DAV, "1,2,3");
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,PROPFIND,PROPPATCH,MKCOL,DELETE,GET,PUT,COPY,MOVE,LOCK,UNLOCK,TRACE";
	}

	@Override
	protected String allowHeaders() {
		// 允许Content-Type:application/xml,application/json,因此须列出允许的Content-Type头
		return "*,Content-Type,Authorization,Depth,Destination,If,Lock-Token,Overwrite,Timeout";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
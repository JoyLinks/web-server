package com.joyzl.webserver.webdav;

import com.joyzl.network.http.FormDataCoder;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.QueryCoder;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.WEBServlet;

/**
 * WEBDAV
 * 
 * @author ZhangXi 2025年2月14日
 */
public abstract class WEBDAVServlet extends WEBServlet {

	@Override
	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		if (request.getVersion() != HTTP1.V11 && request.getVersion() != HTTP1.V10) {
			response.setStatus(HTTPStatus.VERSION_NOT_SUPPORTED);
		} else {
			// 将查询参数合并到请求参数中
			QueryCoder.parse(request);
			switch (request.getMethod()) {
				case HTTP1.PROPFIND:
					propfind((Request) request, (Response) response);
					break;
				case HTTP1.PROPPATCH:
					proppatch((Request) request, (Response) response);
					break;
				case HTTP1.MKCOL:
					mkcol((Request) request, (Response) response);
					break;
				case HTTP1.COPY:
					copy((Request) request, (Response) response);
					break;
				case HTTP1.MOVE:
					move((Request) request, (Response) response);
					break;
				case HTTP1.LOCK:
					lock((Request) request, (Response) response);
					break;
				case HTTP1.UNLOCK:
					unlock((Request) request, (Response) response);
					break;
				case HTTP1.GET:
					get((Request) request, (Response) response);
					break;
				case HTTP1.HEAD:
					head((Request) request, (Response) response);
					break;
				case HTTP1.POST:
					FormDataCoder.read(request);
					post((Request) request, (Response) response);
					break;
				case HTTP1.PUT:
					put((Request) request, (Response) response);
					break;
				case HTTP1.PATCH:
					patch((Request) request, (Response) response);
					break;
				case HTTP1.DELETE:
					delete((Request) request, (Response) response);
					break;
				case HTTP1.TRACE:
					trace((Request) request, (Response) response);
					break;
				case HTTP1.OPTIONS:
					options((Request) request, (Response) response);
					break;
				case HTTP1.CONNECT:
					connect((Request) request, (Response) response);
					break;
				default:
					response.setStatus(HTTPStatus.BAD_REQUEST);
			}
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

}
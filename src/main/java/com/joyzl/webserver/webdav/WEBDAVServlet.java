package com.joyzl.webserver.webdav;

import com.joyzl.network.http.FormDataCoder;
import com.joyzl.network.http.HTTP;
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
		if (request.getVersion() != HTTP.V11 && request.getVersion() != HTTP.V10) {
			response.setStatus(HTTPStatus.VERSION_NOT_SUPPORTED);
		} else {
			// 将查询参数合并到请求参数中
			QueryCoder.parse(request);
			switch (request.getMethod()) {
				case HTTP.PROPFIND:
					propfind((Request) request, (Response) response);
					break;
				case HTTP.PROPPATCH:
					proppatch((Request) request, (Response) response);
					break;
				case HTTP.MKCOL:
					mkcol((Request) request, (Response) response);
					break;
				case HTTP.COPY:
					copy((Request) request, (Response) response);
					break;
				case HTTP.MOVE:
					move((Request) request, (Response) response);
					break;
				case HTTP.LOCK:
					lock((Request) request, (Response) response);
					break;
				case HTTP.UNLOCK:
					unlock((Request) request, (Response) response);
					break;
				case HTTP.GET:
					get((Request) request, (Response) response);
					break;
				case HTTP.HEAD:
					head((Request) request, (Response) response);
					break;
				case HTTP.POST:
					FormDataCoder.read(request);
					post((Request) request, (Response) response);
					break;
				case HTTP.PUT:
					put((Request) request, (Response) response);
					break;
				case HTTP.PATCH:
					patch((Request) request, (Response) response);
					break;
				case HTTP.DELETE:
					delete((Request) request, (Response) response);
					break;
				case HTTP.TRACE:
					trace((Request) request, (Response) response);
					break;
				case HTTP.OPTIONS:
					options((Request) request, (Response) response);
					break;
				case HTTP.CONNECT:
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
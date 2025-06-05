package com.joyzl.webserver.authenticate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;

/**
 * 身份验证集合
 * 
 * @author ZhangXi 2024年11月29日
 */
public class Authenticates {

	private volatile Authenticate[] authenticates = new Authenticate[0];

	public boolean verify(Request request, Response response) {
		if (authenticates.length == 0) {
			return true;
		}

		// 身份验证在Servlet之前执行
		// 简单请求(GET HEAD)时浏览器可能不事先发送预检
		// 复杂请求(DELETE)时浏览器会事先发送预检(OPTIONS)
		// 如果预检(OPTIONS)请求被身份验证拒绝，浏览器无法继续身份验证而失败

		// 身份验证失败可能返回有助于继续验证的信息
		// 跨域请求时应提供相应支持的标头

		boolean result = true;
		Authenticate authenticate;
		for (int index = 0; index < authenticates.length; index++) {
			authenticate = authenticates[index];
			if (request.pathStart(authenticate.getPath())) {
				if (authenticate.require(request.getMethod())) {
					if (authenticate.verify(request, response)) {
						return true;
					} else {
						result = false;
					}
				}
			}
		}
		return result;
	}

	public List<Authenticate> all() {
		return Collections.unmodifiableList(Arrays.asList(authenticates));
	}

	public void set(Collection<Authenticate> values) {
		synchronized (this) {
			authenticates = values.toArray(new Authenticate[values.size()]);
		}
	}

	public void add(Collection<Authenticate> values) {
		synchronized (this) {
			authenticates = Utility.arrayAdd(authenticates, values);
		}
	}

	public void add(Authenticate value) {
		synchronized (this) {
			authenticates = Utility.arrayAdd(authenticates, value);
		}
	}

	public void remove(Collection<Authenticate> values) {
		synchronized (this) {
			authenticates = Utility.arrayRemove(authenticates, values);
		}
	}

	public void remove(Authenticate value) {
		synchronized (this) {
			authenticates = Utility.arrayRemove(authenticates, value);
		}
	}

	public void clear() {
		synchronized (this) {
			authenticates = new Authenticate[0];
		}
	}
}
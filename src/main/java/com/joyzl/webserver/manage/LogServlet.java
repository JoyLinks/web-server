/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.manage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import com.joyzl.logger.access.AccessLogger;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.entities.Host;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.service.Services;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 日志接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manage/api/log/*")
public class LogServlet extends CROSServlet {

	public LogServlet(String path) {
		super(path);
		// if (path == null) {
		// base = "/manage/log/";
		// } else {
		// base = Utility.correctBase(path);
		// }
	}

	@Override
	public String name() {
		return "LOG";
	}

	/** 切分路径 */
	private String[] paths(String uri) {
		if (base != null) {
			if (uri.length() < base.length()) {
				return null;
			}
			uri = uri.substring(base.length());
		}
		return uri.split("/");
	}

	/**
	 * 列出或获取日志
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		// 列出日志
		// /base/server
		// /base/server/host
		// 获取日志
		// /base/server/access-20250611.log
		// /base/server/host/access-20250612.log

		final String[] path = paths(request.getPath());
		if (path == null || path.length == 0 || path.length > 3) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final Server server = Services.find(path[0]);
		if (server == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}

		final Host host;
		if (path.length > 1) {
			host = server.find(path[1]);
		} else {
			host = null;
		}

		final String file;
		if (path.length == 2) {
			file = host == null ? path[1] : null;
		} else if (path.length == 3) {
			file = path[2];
		} else {
			file = null;
		}

		final AccessLogger logger;
		if (host != null) {
			logger = host.service().logger();
		} else {
			logger = server.service().logger();
		}
		if (logger == null) {
			response.setStatus(HTTPStatus.GONE);
			return;
		}

		if (file != null) {
			final Path logFile = logger.getDirectory().resolve(file);
			if (Files.exists(logFile)) {
				response.addHeader(ContentType.NAME, MIMEType.TEXT_PLAIN);
				// 如果是当日日志文件，可能在发出时字节数已发生变化
				// response.addHeader(ContentLength.NAME,
				// Long.toString(Files.size(logFile)));
				response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
				response.setContent(logFile.toFile());
			} else {
				response.setStatus(HTTPStatus.NOT_FOUND);
			}
		} else {
			final Path dir = logger.getDirectory();
			String[] files = dir.toFile().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(logger.getFileExtension());
				}
			});
			if (files != null) {
				Arrays.sort(files, Comparator.reverseOrder());
			} else {
				files = new String[0];
			}

			response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
			final String type = request.getHeader(ContentType.NAME);
			if (Utility.same(type, MIMEType.APPLICATION_JSON)) {
				response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
				if (files.length > 0) {
					final DataBufferOutput output = new DataBufferOutput();
					try (final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
						Serializer.JSON().writeEntities(Arrays.asList(files), writer);
						writer.flush();
					}
					response.setContent(output.buffer());
				}
			} else {
				response.addHeader(ContentType.NAME, MIMEType.TEXT_PLAIN);
				if (files.length > 0) {
					final DataBufferOutput output = new DataBufferOutput();
					try (final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
						for (String name : files) {
							writer.append(name);
							writer.append('\n');
						}
						writer.flush();
					}
					response.setContent(output.buffer());
				}
			}
		}
	}

	/**
	 * 获取指定日志文件的大小
	 */
	@Override
	protected void head(Request request, Response response) throws Exception {
		final String[] path = paths(request.getPath());
		if (path == null || path.length < 2 || path.length > 3) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final Server server = Services.find(path[0]);
		if (server == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}

		final Host host = server.find(path[1]);

		final String file;
		if (path.length == 2) {
			file = host == null ? path[1] : null;
		} else if (path.length == 3) {
			file = path[2];
		} else {
			file = null;
		}
		if (file == null) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}

		final AccessLogger logger;
		if (host != null) {
			logger = host.service().logger();
		} else {
			logger = server.service().logger();
		}
		if (logger == null) {
			response.setStatus(HTTPStatus.GONE);
			return;
		}

		final Path logFile = logger.getDirectory().resolve(file);
		if (Files.exists(logFile)) {
			response.addHeader(ContentType.NAME, MIMEType.TEXT_PLAIN);
			response.addHeader(ContentLength.NAME, Long.toString(Files.size(logFile)));
		} else {
			response.setStatus(HTTPStatus.NOT_FOUND);
		}
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET";
	}

	@Override
	protected String allowHeaders() {
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.manage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import com.joyzl.logger.LoggerCleaner;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.archive.Archive;
import com.joyzl.webserver.archive.ArchiveServlet;
import com.joyzl.webserver.archive.Directory;
import com.joyzl.webserver.archive.Packet;
import com.joyzl.webserver.entities.Host;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.entities.Servlet;
import com.joyzl.webserver.service.Services;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 归档库管理接口
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年7月26日
 */
@ServletPath(path = "/manage/api/archives")
public final class ArchivesServlet extends CROSServlet {

	public ArchivesServlet(String path) {
		super(path);
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		post(request, response);
	}

	@Override
	protected void post(Request request, Response response) throws Exception {
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		if (Utility.noEmpty(id)) {
			int i = Utility.value(id, -1);
			if (i < 0) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
			final Archive archive = Archive.get(i);
			if (archive == null) {
				response.setStatus(HTTPStatus.NOT_FOUND);
				return;
			}

			if (Utility.noEmpty(name)) {
				// 按日期列出文件包
				final LocalDate date = dateValue(name);
				if (date == null) {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}

				final List<Packet> items = archive.find(date);
				if (items != null) {
					// JSON
					final DataBuffer buffer = DataBuffer.instance();
					boolean later = false;
					buffer.writeUTF8('[');
					for (Packet packet : items) {
						if (later) {
							buffer.writeUTF8(',');
						} else {
							later = true;
						}
						buffer.writeUTF8("{\"Name\":\"");
						buffer.writeUTF8(packet.name());
						buffer.writeUTF8("\"}");
					}
					buffer.writeUTF8(']');

					response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
					response.setContent(buffer);
				} else {
					response.setStatus(HTTPStatus.GONE);
				}
			} else {
				// 列出所有日期目录
				final Collection<Directory> directories = archive.indexer().list();

				// JSON
				final DataBuffer buffer = DataBuffer.instance();
				boolean later = false;
				buffer.writeUTF8('[');
				for (Directory directory : directories) {
					if (later) {
						buffer.writeUTF8(',');
					} else {
						later = true;
					}
					buffer.writeUTF8("{\"Name\":\"");
					buffer.writeUTF8(directory.name());
					buffer.writeUTF8("\",\"Code\":");
					buffer.writeUTF8(Integer.toString(directory.codes()));
					buffer.writeUTF8(",\"Size\":\"");
					buffer.writeUTF8(LoggerCleaner.byteSizeText(directory.size()));
					buffer.writeUTF8("\"}");
				}
				buffer.writeUTF8(']');

				response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
				response.setContent(buffer);
			}
		} else {
			// 列出所有归档库

			// JSON
			final DataBuffer buffer = DataBuffer.instance();
			buffer.writeUTF8('[');

			// 因缺失关联服务程序而废弃 20260103
			// Archive a;
			// final List<Archive> archives = Archive.all();
			// for (int index = 0; index < archives.size(); index++) {
			// if (index > 0) {
			// buffer.writeUTF8(',');
			// }
			// a = archives.get(index);
			// buffer.writeUTF8("{\"Index\":");
			// buffer.writeUTF8(Integer.toString(index));
			// buffer.writeUTF8(",\"Path\":\"");
			// buffer.writeUTF8(a.path().toString());
			// buffer.writeUTF8("\",\"Expire\":");
			// buffer.writeUTF8(Integer.toString(a.expire()));
			// buffer.writeUTF8('}');
			// }

			int size = 0;
			for (Server server : Services.all()) {
				for (Servlet servlet : server.getServlets()) {
					if (servlet.service() instanceof ArchiveServlet s) {
						if (size > 0) {
							buffer.writeUTF8(',');
						}
						buffer.writeUTF8("{\"Id\":");
						buffer.writeUTF8(Integer.toString(s.archive().id()));
						buffer.writeUTF8(",\"Content\":\"");
						buffer.writeUTF8(s.archive().path().toString());
						buffer.writeUTF8("\",\"Expire\":");
						buffer.writeUTF8(Integer.toString(s.archive().expire()));
						buffer.writeUTF8(",\"Path\":\"");
						buffer.writeUTF8(servlet.service().getBase());
						buffer.writeUTF8("\"}");
						size++;
					}
				}
				for (Host host : server.getHosts()) {
					for (Servlet servlet : host.getServlets()) {
						if (servlet.service() instanceof ArchiveServlet s) {
							if (size > 0) {
								buffer.writeUTF8(',');
							}
							buffer.writeUTF8("{\"Id\":");
							buffer.writeUTF8(Integer.toString(s.archive().id()));
							buffer.writeUTF8(",\"Content\":\"");
							buffer.writeUTF8(s.archive().path().toString());
							buffer.writeUTF8("\",\"Expire\":");
							buffer.writeUTF8(Integer.toString(s.archive().expire()));
							buffer.writeUTF8(",\"Path\":\"");
							buffer.writeUTF8(servlet.service().getBase());
							buffer.writeUTF8("\"}");
							size++;
						}
					}
				}
			}

			buffer.writeUTF8(']');

			response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
			response.setContent(buffer);
		}
	}

	// 20111203
	LocalDate dateValue(String text) {
		try {
			return LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET,POST";
	}

	@Override
	protected String allowHeaders() {
		// 允许Content-Type:application/x-www-form-urlencoded,application/x-form-www-urlencoded,因此须列出允许的Content-Type头
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
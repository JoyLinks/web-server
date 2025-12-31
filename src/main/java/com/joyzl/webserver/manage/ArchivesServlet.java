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
import com.joyzl.webserver.archive.Directory;
import com.joyzl.webserver.archive.Packet;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 归档库管理接口
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年7月26日
 */
@ServletPath(path = "manage/api/archives")
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
		final String indx = request.getParameter("index");
		final String date = request.getParameter("date");
		if (Utility.noEmpty(indx)) {
			int indxValue = Utility.value(indx, -1);
			if (indxValue < 0) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
				return;
			}
			final Archive archive = Archive.get(indxValue);
			if (archive == null) {
				response.setStatus(HTTPStatus.NOT_FOUND);
				return;
			}

			if (Utility.noEmpty(date)) {
				// 按日期列出文件包
				final LocalDate localDate = dateValue(date);
				if (localDate == null) {
					response.setStatus(HTTPStatus.BAD_REQUEST);
					return;
				}

				final List<Packet> items = archive.find(localDate);
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
			final List<Archive> archives = Archive.all();

			// JSON
			final DataBuffer buffer = DataBuffer.instance();
			buffer.writeUTF8('[');
			Archive a;
			for (int index = 0; index < archives.size(); index++) {
				if (index > 0) {
					buffer.writeUTF8(',');
				}
				a = archives.get(index);
				buffer.writeUTF8("{\"Index\":");
				buffer.writeUTF8(Integer.toString(index));
				buffer.writeUTF8(",\"Path\":\"");
				buffer.writeUTF8(a.path().toString());
				buffer.writeUTF8("\",\"Expire\":");
				buffer.writeUTF8(Integer.toString(a.expire()));
				buffer.writeUTF8('}');
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
}
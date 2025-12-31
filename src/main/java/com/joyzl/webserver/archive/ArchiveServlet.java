/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.archive;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.joyzl.logger.LoggerCleaner;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.codec.UTF8Coder;
import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.MultipartFile;
import com.joyzl.network.http.MultipartFile.MultipartFiles;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 归档库接口
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年7月26日
 */
@ServletPath(path = "/archive/*")
public final class ArchiveServlet extends CROSServlet implements Closeable {

	private final ContentType CONTENT_TYPE_JSON = new ContentType(MIMEType.APPLICATION_JSON, StandardCharsets.UTF_8.name());
	private final Archive archives;

	public ArchiveServlet(String path, String root, int expire) throws IOException {
		super(path);
		archives = new Archive(root, expire);
	}

	// 列出文件名
	// http://127.0.0.1/archive/code
	// http://127.0.0.1/archive/file?code=xxxx
	// 获取指定文件
	// http://127.0.0.1/archive/code/name.jpg
	// http://127.0.0.1/archive/code?index=1
	// http://127.0.0.1/archive/file?code=xxxx&name=xxxx.jpg
	// 获取最新文件
	// http://127.0.0.1/archive/code/last
	// http://127.0.0.1/archive/file?code=xxxx&name=last

	@Override
	protected void get(Request request, Response response) throws Exception {
		String code = request.getParameter("code");
		String name = request.getParameter("name");
		String indx = request.getParameter("index");
		if (Utility.isEmpty(code)) {
			// 从路径中分离参数
			code = request.getPath().substring(getBase().length());
			if (Utility.noEmpty(code)) {
				// 路径参数
				if (code.startsWith("/")) {
					final int i = code.indexOf('/', 1);
					if (i > 1) {
						// "/code/name"
						name = code.substring(i + 1);
						code = code.substring(1, i);
					} else {
						// "/code"
						code = code.substring(1);
					}
				} else {
					final int i = code.indexOf('/', 1);
					if (i > 1) {
						// "code/name"
						name = code.substring(i + 1);
						code = code.substring(0, i);
					} else {
						// "code"
						code = code.substring(0);
					}
				}
			}
		}

		if (Utility.noEmpty(code)) {
			final Packet archive = archives.find(code);
			if (archive != null) {
				if (Utility.noEmpty(indx)) {
					final int index = Utility.value(indx, -1);
					if (index >= 0) {
						final Document file = archive.get(index);
						if (file != null) {
							name = file.getName();
							response.setStatus(HTTPStatus.OK);
							response.addHeader(ContentType.NAME, MIMEType.getByFilename(name));
							response.addHeader(new ContentDisposition(ContentDisposition.ATTACHMENT, name));
							response.setContent(file.stream());
						} else {
							// 索引没有文件
							response.setStatus(HTTPStatus.NOT_FOUND);
						}
					} else {
						// 无效索引
						response.setStatus(HTTPStatus.BAD_REQUEST);
					}
				} else if (Utility.noEmpty(name)) {
					final Document file;
					if (Utility.same("last", name)) {
						// 下载文件（浏览器只能提示另存为）
						file = archive.last();
					} else {
						// 获取文件（浏览器有可能尝试显示内容）
						file = archive.get(name);
					}
					if (file != null) {
						response.setStatus(HTTPStatus.OK);
						response.addHeader(ContentType.NAME, MIMEType.getByFilename(name));
						response.addHeader(new ContentDisposition(ContentDisposition.ATTACHMENT, name));
						response.setContent(file.stream());
					} else {
						// 名称没有文件
						response.setStatus(HTTPStatus.NOT_FOUND);
					}
				} else {
					// 获取代码的文件名称列表
					final List<Document> files = archive.list();
					if (files == null) {
						response.setStatus(HTTPStatus.NOT_FOUND);
					} else {
						// JSON
						Document file;
						final DataBuffer buffer = DataBuffer.instance();
						buffer.writeUTF8('[');
						for (int i = 0; i < files.size(); i++) {
							if (i > 0) {
								buffer.writeUTF8(',');
							}
							file = files.get(i);
							buffer.writeUTF8("{\"Index\":");
							buffer.writeUTF8(Integer.toString(file.getIndex()));
							buffer.writeUTF8(",\"Size\":\"");
							buffer.writeUTF8(LoggerCleaner.byteSizeText(file.getSize()));
							buffer.writeUTF8(",\"Time\":\"");
							buffer.writeUTF8(Long.toString(file.getTime()));
							buffer.writeUTF8("\",\"Name\":\"");
							buffer.writeUTF8(file.getName());
							if (file.getNumber() != null) {
								buffer.writeUTF8("\",\"Number\":\"");
								buffer.writeUTF8(file.getNumber());
							}
							buffer.writeUTF8("\"}");
						}
						buffer.writeUTF8(']');

						response.setStatus(HTTPStatus.OK);
						response.addHeader(CONTENT_TYPE_JSON);
						response.setContent(buffer);
					}
				}
			} else {
				// 没有归档集
				response.setStatus(HTTPStatus.NOT_FOUND);
			}
		} else {
			// 缺失编号
			response.setStatus(HTTPStatus.BAD_REQUEST);
		}
	}

	@Override
	protected void post(Request request, Response response) throws Exception {
		if (archives == null) {
			response.setStatus(HTTPStatus.SERVICE_UNAVAILABLE);
			return;
		}

		// 必须指定有效代码
		final String code = request.getParameter("code");
		if (Utility.isEmpty(code)) {
			response.setStatus(HTTPStatus.BAD_REQUEST);
			return;
		}
		// 编号可选
		final String number = request.getParameter("number");

		if (request.hasContent()) {
			if (request.getContent() instanceof MultipartFiles files) {
				try {
					for (MultipartFile file : files) {
						if (file.getLength() > 0) {
							archives.save(file.getFile(), code, file.getFilename(), number);
							file.getFile().delete();
						}
					}
				} catch (Exception e) {
					for (MultipartFile file : files) {
						file.getFile().delete();
					}
					response.setContent(UTF8Coder.encode(e.getMessage()));
					response.setStatus(HTTPStatus.BAD_REQUEST);
				}
			} else {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}
		} else {
			response.setStatus(HTTPStatus.BAD_REQUEST);
		}
	}

	@Override
	public void close() throws IOException {
		archives.close();
	}

	public final Archive archives() {
		return archives;
	}
}
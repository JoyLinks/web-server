/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.webserver.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.joyzl.logger.Logger;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 目录资源，列出目录中的文件
 * 
 * @author ZhangXi 2024年11月21日
 */
public class DirectoryResource extends WEBResource {

	/** URI */
	private final String contentLocation;
	private final String contentType;

	private final File dir;
	private volatile DataBuffer buffer;

	public DirectoryResource(String path, File dir, boolean browse) {
		this.dir = dir;
		contentLocation = path;
		if (browse) {
			contentType = MIMEType.TEXT_HTML;
		} else {
			contentType = null;
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getContentLanguage() {
		return null;
	}

	@Override
	public String getContentLocation() {
		return contentLocation;
	}

	@Override
	public String getLastModified() {
		return null;
	}

	@Override
	public String getETag() {
		return null;
	}

	@Override
	public String fitEncoding(AcceptEncoding acceptEncoding) {
		return AcceptEncoding.IDENTITY;
	}

	@Override
	public long getLength(String encoding) throws IOException {
		if (contentType != null) {
			return list();
		}
		return 0;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		if (contentType != null) {
			list();
			return new DataBufferInput(buffer, true);
		}
		return null;
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		if (contentType != null) {
			list();
			return new DataBufferInput(buffer, true);
		}
		return null;
	}

	public File getDirectory() {
		return dir;
	}

	public void clear() {
		DataBuffer b = buffer;
		buffer = null;
		if (b != null) {
			b.release();
		}
	}

	/**
	 * 列出文件
	 */
	int list() {
		if (buffer == null) {
			buffer = DataBuffer.instance();
			try {
				buffer.writeASCIIs("<html>");
				buffer.writeASCIIs("<body>");

				final String[] files = dir.list();
				if (files != null) {
					buffer.writeASCIIs("<ul>");
					for (int index = 0; index < files.length; index++) {
						buffer.writeASCIIs("<li><a href=\"");
						buffer.writeUTF8(files[index]);
						buffer.writeASCIIs("\">");

						buffer.writeASCIIs("<span>");
						buffer.writeUTF8(files[index]);
						buffer.writeASCIIs("</span>");

						buffer.writeASCIIs("</a></li>");
					}
					buffer.writeASCIIs("</ul>");
				}

				buffer.writeASCIIs("</body>");
				buffer.writeASCIIs("</html>");
			} catch (IOException e) {
				Logger.error(e);
				buffer.release();
				buffer = null;
			}
		}
		return buffer == null ? 0 : buffer.readable();
	}
}
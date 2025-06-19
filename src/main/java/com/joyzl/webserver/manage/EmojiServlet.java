package com.joyzl.webserver.manage;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.servlet.ServletPath;
import com.joyzl.webserver.web.WEBServlet;

/**
 * Unicode Emoji
 * <p>
 * https://unicode.org/emoji/charts/full-emoji-list.html
 * </p>
 * 
 * @author ZhangXi 2025年6月12日
 */
@ServletPath(path = "/emoji")
public class EmojiServlet extends WEBServlet {

	private final static int A = 0x1F000;
	private final static int B = 0x1FFFF;

	public EmojiServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "EMOJI";
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		buffer.writeUTF8("<!DOCTYPE html><html lang=\"zh\">");
		buffer.writeUTF8("<head><meta charset=\"utf-8\"><title>JOYZL Unicode Emoji TEST</title></head>");
		buffer.writeUTF8("<body style=\"font-size:3rem;word-break:break-all;\">");
		buffer.writeUTF8("<code>");

		for (int c = A; c <= B; c++) {
			buffer.writeUTF8(Character.highSurrogate(c), Character.lowSurrogate(c));
		}

		buffer.writeUTF8("</code>");
		buffer.writeUTF8("</body>");
		buffer.writeUTF8("<!DOCTYPE html><html>");

		response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
		response.addHeader(ContentType.NAME, "text/html; charset=utf-8");
		response.setContent(buffer);
	}
}
package com.joyzl.webserver.manage;

import com.joyzl.network.codec.UTF8Coder;
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

	public final static String NAME = "EMOJI";

	private final static int A = 0x1F000;
	private final static int B = 0x1FFFF;

	@Override
	public String name() {
		return NAME;
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		final StringBuilder emoji = new StringBuilder(65536);
		emoji.append("<!DOCTYPE html><html lang=\"zh\">");
		emoji.append("<head><meta charset=\"utf-8\"><title>JOYZL Unicode Emoji TEST</title></head>");
		emoji.append("<body style=\"font-size:3rem;word-break:break-all;\">");
		emoji.append("<code>");

		for (int c = A; c <= B; c++) {
			emoji.appendCodePoint(c);
		}

		emoji.append("</code>");
		emoji.append("</body>");
		emoji.append("<!DOCTYPE html><html>");

		response.addHeader(ContentType.NAME, "text/html; charset=utf-8");
		response.setContent(UTF8Coder.encode(emoji));
	}
}
package com.joyzl.webserver.webdav;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.Header;

/**
 * If 优美的字符串格式
 * 
 * <pre>
 * final If IF = new If();
 * while (IF.nextGroup()) {
 * 	IF.getTag();
 * 	while (IF.nextValue()) {
 * 		IF.isToken();
 * 		IF.isETag();
 * 		IF.isNot();
 * 		IF.getValue();
 * 	}
 * }
 * </pre>
 * 
 * @author ZhangXi 2025年5月28日
 */
public class If extends Header {

	public static final String NAME = HTTP1.If;
	final static String NOT = "Not";

	/*-
	 * If: (a AND b) OR (c)
	 * If: (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> ["ETag1"]) (["ETag2"])
	 * If: (Not <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> <urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092>)
	 * If: (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>) (Not <DAV:no-lock>)
	 * If: </resource1> (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2> [W/"A ETag"]) (["strong ETag"])
	 * If: <http://www.example.com/specs/> (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>)
	 * If: </specs/rfc2518.doc> (["4217"])
	 * If: </specs/rfc2518.doc> (Not ["4217"])
	 */

	private String value;

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
		reset();
	}

	////////////////////////////////////////////////////////////////////////////////
	// 渐进式解析，直至完成，避免创建额外对象和集合
	// <Resource-Tag>(Condition)...
	// index当前字符位置，tagBegin和tagEnd表示条件值的前缀标记
	// Not 是 <urn:uuid:...>的标记
	// Not 是 ["ETag"]的标记

	private int index, c;
	private String tag;

	public void reset() {
		index = 0;
	}

	public boolean nextGroup() {
		if (value == null) {
			return false;
		}

		// (Condition)
		// <Resource-Tag>(Condition)

		tag = null;
		int b = 0, e = 0;
		while (index < value.length()) {
			c = value.charAt(index++);
			if (Character.isWhitespace(c)) {
				continue;
			} else if (c == '(') {
				if (b < e) {
					// <Resource-Tag>
					tag = value.substring(b, e);
				}
				return true;
			} else if (c == '<') {
				b = index;
			} else if (c == '>') {
				e = index - 1;
			}
		}
		return false;
	}

	/** Resource-Tag / Not */
	public String getTag() {
		return tag;
	}

	/** Token / ETag */
	public boolean nextValue() {
		if (value == null) {
			return false;
		}

		// ["ETag1"]
		// <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>
		// Not <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>

		tag = null;
		int i = 0;
		while (index < value.length()) {
			c = value.charAt(index++);
			if (Character.isWhitespace(c)) {
				continue;
			} else if (c == ')') {
				return false;
			} else if (c == '<' || c == '[') {
				if (i == 3) {
					tag = NOT;
				}
				return true;
			}
			if (i < NOT.length() && c == NOT.charAt(i)) {
				i++;
			}
		}
		return false;
	}

	/** Token / ETag */
	public String getValue() {
		int i = index;
		if (isToken()) {
			index = value.indexOf('>', index);
		} else if (isETag()) {
			index = value.indexOf(']', index);
		}
		if (i < index) {
			return value.substring(i, index++);
		}
		return null;
	}

	public boolean isToken() {
		return c == '<';
	}

	public boolean isETag() {
		return c == '[';
	}

	public boolean isNot() {
		return tag == NOT;
	}

	public final static If parse(String value) {
		if (Utility.noEmpty(value)) {
			If header = new If();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}
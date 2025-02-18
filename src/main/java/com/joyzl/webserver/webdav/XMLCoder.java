package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.io.Reader;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Propfind;

/**
 * 
 * @author ZhangXi 2025年2月14日
 */
public class XMLCoder {

	static void read(Request request) throws IOException {

	}

	static Propfind read(Class<Propfind> class1, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	static void write(Multistatus multistatus, Response response) {
		// TODO Auto-generated method stub

	}

	class XMLReader {

		final static char S = '<';
		final static char E = '>';
		final static char X = '/';

		private final StringBuilder builder;
		private final Reader reader;
		private int depth;

		public XMLReader(Reader reader) {
			builder = new StringBuilder();
			this.reader = reader;
		}

		public void read() {

		}
	}
}
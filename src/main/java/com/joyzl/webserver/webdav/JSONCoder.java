package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.odbs.ODBS;
import com.joyzl.odbs.ODBSJson;
import com.joyzl.webserver.web.MIMEType;
import com.joyzl.webserver.webdav.elements.Element;

class JSONCoder {

	final static ODBSJson JSON;
	static {
		final ODBS odbs = ODBS.initialize(Element.class.getPackageName());
		// System.out.println(odbs.checkString());
		JSON = new ODBSJson(odbs);
	}

	static <T extends Element> T read(Class<T> element, Request request) throws IOException, ParseException {
		if (request.getContent() instanceof DataBuffer) {
			try (//
				final DataBufferInput in = new DataBufferInput((DataBuffer) request.getContent());
				final Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				return JSON.readEntity(element, reader);
			}
		}
		return null;
	}

	static void write(Element element, Response response) throws IOException {
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		try (//
			final DataBufferOutput out = new DataBufferOutput();
			final Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			JSON.writeEntity(element, writer);
			writer.flush();
			response.setContent(out.buffer());
			response.addHeader(ContentLength.NAME, Integer.toString(out.buffer().readable()));
		}
	}
}
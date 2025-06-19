package com.joyzl.webserver.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.webserver.service.Serializer;

class TestSerializer {

	@Test
	void testBINARY() throws IOException {
		final List<Server> servers = new ArrayList<>();

		final Server server = new Server();
		final Host host = new Host();
		final Servlet servlet = new Servlet();

		host.getServlets().add(servlet);
		server.getHosts().add(host);
		servers.add(server);

		final DataBuffer buffer = DataBuffer.instance();
		Serializer.BINARY().writeEntities(servers, buffer);

		servers.clear();
		Serializer.BINARY().readEntities(servers, buffer);

		assertEquals(servers.size(), 1);

		final Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
		Serializer.JSON().writeEntities(servers, writer);
		writer.flush();
	}

	@Test
	void testJSON() throws IOException, ParseException {
		Address address = new Address();
		address.setAddress("192.168.8.88");
		address.setHosts("JOYZL");

		try (final Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
			Serializer.JSON().writeEntity(address, writer);
			writer.flush();
		}

		final DataBufferOutput output = new DataBufferOutput();
		try (final Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
			Serializer.JSON().writeEntity(address, writer);
			writer.flush();
		}

		final DataBufferInput input = new DataBufferInput(output.buffer());
		try (final Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			address = Serializer.JSON().readEntity(Address.class, reader);
		}
		assertEquals(address.getHosts()[0], "JOYZL");
	}

	@Test
	void testJSONMap() throws Exception {
		Servlet servlet = new Servlet();
		servlet.getParameters().put("name", "value");

		try (final Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
			Serializer.JSON().writeEntity(servlet, writer);
			writer.flush();
		}

		final DataBufferOutput output = new DataBufferOutput();
		try (final Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
			Serializer.JSON().writeEntity(servlet, writer);
			writer.flush();
		}

		final DataBufferInput input = new DataBufferInput(output.buffer());
		try (final Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			servlet = Serializer.JSON().readEntity(Servlet.class, reader);
		}
		assertEquals(servlet.getParameters().size(), 1);
	}
}
package com.joyzl.webserver.entities;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestSerializer {

	@Test
	void testServletList() throws IOException {
		// final List<Servlet> servlets = new ArrayList<>();
		// final Location location = new Location();
		// location.setLocation("http://www.joyzl.com");
		// location.setPath("/joyzl/*");
		// servlets.add(location);
		//
		// final Resource resource = new Resource();
		// servlets.add(resource);
		//
		// final Webdav webdav = new Webdav();
		// servlets.add(webdav);
		//
		// final DataBuffer buffer = DataBuffer.instance();
		// Serializer.BINARY().writeEntities(servlets, buffer);
		//
		// servlets.clear();
		// Serializer.BINARY().readEntities(servlets, buffer);
		//
		// // System.out.println(servlets);
		// assertEquals(servlets.size(), 3);
		// assertTrue(servlets.get(0) instanceof Location);
		// assertTrue(servlets.get(1) instanceof Resource);
		// assertTrue(servlets.get(2) instanceof Webdav);
		//
		// final Writer writer = new OutputStreamWriter(System.out,
		// StandardCharsets.UTF_8);
		// Serializer.JSON().writeEntities(servlets, writer);
		// writer.flush();
	}
}
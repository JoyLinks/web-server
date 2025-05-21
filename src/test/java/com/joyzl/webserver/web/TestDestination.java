package com.joyzl.webserver.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.joyzl.webserver.webdav.Destination;

class TestDestination {

	@Test
	void test() {
		final Destination destination = new Destination();

		destination.setHeaderValue("https://example.com/");
		assertEquals(destination.getScheme(), "https");
		assertEquals(destination.getHost(), "example.com");
		assertEquals(destination.getPort(), 0);
		assertEquals(destination.getPath(), "/");
		assertEquals(destination.getQuery(), null);
		assertEquals(destination.getAnchor(), null);
		assertTrue(destination.pathStart("/"));
		assertTrue(destination.pathStart(""));

		destination.setHeaderValue("https://localhost:8000/search?q=text#hello");
		assertEquals(destination.getScheme(), "https");
		assertEquals(destination.getHost(), "localhost");
		assertEquals(destination.getPort(), 8000);
		assertEquals(destination.getPath(), "/search");
		assertEquals(destination.getQuery(), "?q=text");
		assertEquals(destination.getAnchor(), "#hello");
		assertTrue(destination.pathStart("/search"));

		destination.setHeaderValue("file:///ada/Analytical Engine/README.md");
		assertEquals(destination.getScheme(), "file");
		assertEquals(destination.getHost(), null);
		assertEquals(destination.getPort(), 0);
		assertEquals(destination.getPath(), "/ada/Analytical Engine/README.md");
		assertEquals(destination.getQuery(), null);
		assertEquals(destination.getAnchor(), null);
		assertTrue(destination.pathStart("/ada/"));

		destination.setHeaderValue("/");
		assertEquals(destination.getScheme(), null);
		assertEquals(destination.getHost(), null);
		assertEquals(destination.getPort(), 0);
		assertEquals(destination.getPath(), "/");
		assertEquals(destination.getQuery(), null);
		assertEquals(destination.getAnchor(), null);
		assertTrue(destination.pathStart("/"));

		destination.setHeaderValue("/search?q=text#hello");
		assertEquals(destination.getScheme(), null);
		assertEquals(destination.getHost(), null);
		assertEquals(destination.getPort(), 0);
		assertEquals(destination.getPath(), "/search");
		assertEquals(destination.getQuery(), "?q=text");
		assertEquals(destination.getAnchor(), "#hello");
	}
}
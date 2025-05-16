package com.joyzl.webserver.webdav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.joyzl.webserver.webdav.elements.Element;
import com.joyzl.webserver.webdav.elements.LockInfo;
import com.joyzl.webserver.webdav.elements.PropertyUpdate;
import com.joyzl.webserver.webdav.elements.Propfind;

class TestXMLCoder {

	@Test
	void testPROPFIND1() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:propfind xmlns:D="DAV:">
				    <D:prop xmlns:R="http://ns.example.com/boxschema/">
				        <R:bigbox/>
				        <R:author/>
				        <R:DingALing/>
				        <R:Random/>
				    </D:prop>
				</D:propfind>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof Propfind);

		Propfind propfind = XMLCoder.readPropfind(input(xml));
		assertEquals(propfind.getProp().size(), 4);
		assertTrue(propfind.getProp().containsKey("bigbox"));
		assertTrue(propfind.getProp().containsKey("author"));
		assertTrue(propfind.getProp().containsKey("DingALing"));
		assertTrue(propfind.getProp().containsKey("Random"));
	}

	@Test
	void testPROPFIND2() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<propfind xmlns="DAV:">
				    <propname/>
				</propfind>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof Propfind);

		Propfind propfind = XMLCoder.readPropfind(input(xml));
		assertTrue(propfind.isPropname());
	}

	@Test
	void testPROPFIND3() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:propfind xmlns:D="DAV:">
				    <D:allprop/>
				</D:propfind>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof Propfind);

		Propfind propfind = XMLCoder.readPropfind(input(xml));
		assertTrue(propfind.isAllprop());
	}

	@Test
	void testPROPFIND4() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:propfind xmlns:D="DAV:">
				    <D:allprop/>
				    <D:include>
				        <D:supported-live-property-set/>
				        <D:supported-report-set/>
				    </D:include>
				</D:propfind>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof Propfind);

		Propfind propfind = XMLCoder.readPropfind(input(xml));
		assertTrue(propfind.isAllprop());
		assertEquals(propfind.getInclude().size(), 2);
		assertTrue(propfind.getInclude().contains("supported-live-property-set"));
		assertTrue(propfind.getInclude().contains("supported-report-set"));
	}

	@Test
	void testPROPPATCH1() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://ns.example.com/standards/z39.50/">
				    <D:set>
				        <D:prop>
				            <Z:Authors>
				                <Z:Author>Jim Whitehead</Z:Author>
				                <Z:Author>Roy Fielding</Z:Author>
				            </Z:Authors>
				        </D:prop>
				    </D:set>
				    <D:remove>
				        <D:prop><Z:Copyright-Owner/></D:prop>
				    </D:remove>
				</D:propertyupdate>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof PropertyUpdate);

		PropertyUpdate property = XMLCoder.readPropertyUpdate(input(xml));
		assertNotNull(property.getSet());
		assertNotNull(property.getRemove());
		assertEquals(property.getSet().getProp().size(), 2);
		assertEquals(property.getRemove().getProp().size(), 1);
		// {Authors=null, Author=Roy Fielding}
	}

	@Test
	void testLOCK() throws IOException {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:lockinfo xmlns:D='DAV:'>
				    <D:lockscope><D:exclusive/></D:lockscope>
				    <D:locktype><D:write/></D:locktype>
				    <D:owner>
				        <D:href>http://example.org/~ejw/contact.html</D:href>
				    </D:owner>
				</D:lockinfo>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof LockInfo);

		LockInfo lockInfo = XMLCoder.readLockInfo(input(xml));
		assertNotNull(lockInfo.getLockScope());
		assertNotNull(lockInfo.getLockType());
		assertNotNull(lockInfo.getOwner());
	}

	InputStream input(String text) {
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}
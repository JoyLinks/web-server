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
				    <D:prop>
				        <bigbox/>
				        <author/>
				        <DingALing/>
				        <Random/>
				    </D:prop>
				</D:propfind>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof Propfind);

		Propfind propfind = XMLCoder.readPropfind(input(xml));
		assertEquals(propfind.getProp().size(), 4);
		assertTrue(propfind.getProp().contains("bigbox"));
		assertTrue(propfind.getProp().contains("author"));
		assertTrue(propfind.getProp().contains("DingALing"));
		assertTrue(propfind.getProp().contains("Random"));
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
				<D:propertyupdate xmlns:D="DAV:">
				    <D:set>
				        <D:prop>
				            <Authors>
				                <Author>Jim Whitehead</Z:Author>
				                <Author>Roy Fielding</Z:Author>
				            </Authors>
				        </D:prop>
				    </D:set>
				    <D:remove>
				        <D:prop><Copyright-Owner/></D:prop>
				    </D:remove>
				</D:propertyupdate>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof PropertyUpdate);

		PropertyUpdate property = XMLCoder.readPropertyUpdate(input(xml));
		assertEquals(property.getProp().size(), 2);
	}

	@Test
	void testPROPPATCH2() throws Exception {
		final String xml = """
				<?xml version="1.0" encoding="utf-8" ?>
				<D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
					<D:set>
					<D:prop>
					<Z:Win32CreationTime>Fri, 10 May 2024 08:43:02 GMT</Z:Win32CreationTime>
					<Z:Win32LastAccessTime>Tue, 03 Jun 2025 08:17:59 GMT</Z:Win32LastAccessTime>
					<Z:Win32LastModifiedTime>Fri, 10 May 2024 08:43:02 GMT</Z:Win32LastModifiedTime>
					<Z:Win32FileAttributes>00000000</Z:Win32FileAttributes>
					</D:prop>
					</D:set>
				</D:propertyupdate>
				""";
		Element element = XMLCoder.read(input(xml));
		assertTrue(element instanceof PropertyUpdate);

		PropertyUpdate property = XMLCoder.readPropertyUpdate(input(xml));
		assertEquals(property.getProp().size(), 4);
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
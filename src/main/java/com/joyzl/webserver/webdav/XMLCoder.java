package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.io.InputStream;

import com.joyzl.codec.XMLElementType;
import com.joyzl.codec.XMLReader;
import com.joyzl.codec.XMLWriter;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Request;
import com.joyzl.webserver.web.MIMEType;
import com.joyzl.webserver.webdav.elements.Collection;
import com.joyzl.webserver.webdav.elements.Element;
import com.joyzl.webserver.webdav.elements.Error;
import com.joyzl.webserver.webdav.elements.Include;
import com.joyzl.webserver.webdav.elements.LockInfo;
import com.joyzl.webserver.webdav.elements.LockScope;
import com.joyzl.webserver.webdav.elements.LockType;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Prop;
import com.joyzl.webserver.webdav.elements.Property;
import com.joyzl.webserver.webdav.elements.PropertyUpdate;
import com.joyzl.webserver.webdav.elements.Propfind;
import com.joyzl.webserver.webdav.elements.Propname;
import com.joyzl.webserver.webdav.elements.Propstat;
import com.joyzl.webserver.webdav.elements.Response;
import com.joyzl.webserver.webdav.elements.ResponseDescription;
import com.joyzl.webserver.webdav.elements.Status;

/**
 * WEBDAV XML Coder
 * 
 * @author ZhangXi 2025年2月14日
 */
class XMLCoder extends WEBDAV {

	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> clazz, Request request) throws IOException {
		try (final DataBufferInput input = new DataBufferInput((DataBuffer) request.getContent(), true)) {
			if (clazz == Propfind.class) {
				return (T) readPropfind(input);
			}
			if (clazz == PropertyUpdate.class) {
				return (T) readPropertyUpdate(input);
			}
			if (clazz == LockInfo.class) {
				return (T) readLockInfo(input);
			}
		}
		return null;
	}

	public static Element read(InputStream input) throws IOException {
		final XMLReader reader = new XMLReader(input);
		while (reader.nextElement()) {
			if (reader.type() == XMLElementType.NORMAL) {
				if (reader.isName(PROPFIND)) {
					return readPropfind(reader);
				} else if (reader.isName(PROPERTYUPDATE)) {
					return readPropertyUpdate(reader);
				} else if (reader.isName(LOCKINFO)) {
					return readLockInfo(reader);
				} else {
					// 忽略无法识别的元素
				}
			}
		}
		return null;
	}

	public static LockInfo readLockInfo(InputStream input) throws IOException {
		final XMLReader reader = new XMLReader(input);
		while (reader.nextElement()) {
			if (reader.isName(LOCKINFO)) {
				return readLockInfo(reader);
			}
		}
		return null;
	}

	private static LockInfo readLockInfo(XMLReader reader) throws IOException {
		final LockInfo lockInfo = new LockInfo();
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(LOCKSCOPE)) {
				lockInfo.setLockScope(readLockScope(reader));
			} else if (reader.isName(LOCKTYPE)) {
				lockInfo.setLockType(readLockType(reader));
			} else if (reader.isName(OWNER)) {
				if (reader.isEnd()) {
					lockInfo.setOwner(reader.getContent());
				} else {
					lockInfo.setOwner(reader.getChildren());
					reader.nextElement();
				}
			} else {
				// 忽略无法识别的元素
			}
		}
		return lockInfo;
	}

	private static LockScope readLockScope(XMLReader reader) throws IOException {
		LockScope scope = null;
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(SHARED)) {
				scope = LockScope.SHARED;
			} else if (reader.isName(EXCLUSIVE)) {
				scope = LockScope.EXCLUSIVE;
			} else {
				// 忽略无法识别的元素
			}
		}
		return scope;
	}

	private static LockType readLockType(XMLReader reader) throws IOException {
		LockType type = null;
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(WRITE)) {
				type = LockType.WRITE;
			} else {
				// 忽略无法识别的元素
			}
		}
		return type;
	}

	public static PropertyUpdate readPropertyUpdate(InputStream input) throws IOException {
		final XMLReader reader = new XMLReader(input);
		while (reader.nextElement()) {
			if (reader.isName(PROPERTYUPDATE)) {
				return readPropertyUpdate(reader);
			}
		}
		return null;
	}

	private static PropertyUpdate readPropertyUpdate(XMLReader reader) throws IOException {
		final PropertyUpdate propertyUpdate = new PropertyUpdate();
		final int depth = reader.depth();
		int type = Property.NORMAL;
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(SET)) {
				type = Property.SET;
			} else if (reader.isName(REMOVE)) {
				type = Property.REMOVE;
			} else if (reader.isName(PROP)) {
				readProps(reader, propertyUpdate, type);
			} else {
				// 忽略无法识别的元素
			}
		}
		return propertyUpdate;
	}

	public static Propfind readPropfind(InputStream input) throws IOException {
		final XMLReader reader = new XMLReader(input);
		while (reader.nextElement()) {
			if (reader.isName(PROPFIND)) {
				return readPropfind(reader);
			}
		}
		return null;
	}

	private static Propfind readPropfind(XMLReader reader) throws IOException {
		final Propfind propfind = new Propfind();
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(PROP)) {
				readProps(reader, propfind, Property.NORMAL);
			} else if (reader.isName(PROPNAME)) {
				propfind.setPropname(true);
			} else if (reader.isName(ALLPROP)) {
				propfind.setAllprop(true);
			} else if (reader.isName(INCLUDE)) {
				readInclude(reader, propfind);
			} else {
				// 忽略无法识别的元素
			}
		}
		return propfind;
	}

	private static void readInclude(XMLReader reader, Include parent) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			parent.include().add(reader.getName());
		}
	}

	private static void readProps(XMLReader reader, Propname parent, int type) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isEnd()) {
				if (reader.hasContent()) {
					throw new IOException("WEBDAV无效的PROPERTY");
				}
				parent.prop().add(getName(reader));
			} else {
				throw new IOException("WEBDAV无效的PROPERTY");
			}
		}
	}

	private static void readProps(XMLReader reader, Prop parent, int type) throws IOException {
		Property property;
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			property = new Property(type);
			property.setName(getName(reader));
			if (reader.isEnd()) {
				if (reader.hasContent()) {
					property.setValue(reader.getContent());
				}
			} else {
				property.setValue(reader.getChildren());
				reader.nextElement();
			}
			parent.prop().add(property);
		}
	}

	private static String getName(XMLReader reader) throws IOException {
		// 注意：将规范之外的命名空间合并到名称中
		if (reader.hasPrefix()) {
			if (!reader.isPrefix("D")) {
				String xmlns = reader.getAttributeValue("xmlns:" + reader.getPrefix());
				if (xmlns == null || xmlns.length() == 0) {
					// <bar:foo xmlns:bar=""/>
					throw new IOException("WEBDAV命名空间缺失");
				} else {
					// <R:author
					// xmlns:R="http://ns.example.com/boxschema/">xxx</R:author>
					xmlns = xmlns.replace('/', '(');
					xmlns = xmlns.replace(':', ')');
					return reader.getName() + " " + xmlns;
				}
			}
		} else if (reader.hasAttributes()) {
			String xmlns = reader.getAttributeValue("xmlns");
			if (xmlns != null && xmlns.length() > 0) {
				if (!"DAV:".equals(xmlns)) {
					// <somename xmlns="http://example.com/alpha"/>
					xmlns = xmlns.replace('/', '(');
					xmlns = xmlns.replace(':', ')');
					return reader.getName() + " " + xmlns;
				}
			}
		}
		return reader.getName();
	}

	public static void write(Multistatus multistatus, com.joyzl.network.http.Response response) throws IOException {
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_XML);
		try (final DataBufferOutput output = new DataBufferOutput();
			final XMLWriter writer = new XMLWriter(output);) {
			write(multistatus, writer);
			writer.flush();
			response.setContent(output.buffer());
			response.addHeader(ContentLength.NAME, Integer.toString(output.buffer().readable()));
		}
	}

	private static void write(Multistatus multistatus, XMLWriter writer) throws IOException {
		writer.writeProlog("1.0", "utf-8");
		writer.writeElement("D", MULTISTATUS);
		writer.writeAttribute("xmlns:D", "DAV:");

		Propstat propstat;
		Response response;
		for (int r = 0; r < multistatus.getResponses().size(); r++) {
			response = multistatus.getResponses().get(r);
			if (response != null) {
				writer.writeElement("D", RESPONSE);

				if (response.getHref() != null) {
					writer.writeElement("D", HREF);
					writer.writeContent(response.getHref());
					writer.endElement("D", HREF);
				}
				if (response.getLocation() != null) {
					writer.writeElement("D", LOCATION);
					writer.writeElement("D", HREF);
					writer.writeContent(response.getHref());
					writer.endElement("D", HREF);
					writer.endElement("D", LOCATION);
				}

				for (int p = 0; p < response.getPropstats().size(); p++) {
					propstat = response.getPropstats().get(p);
					if (propstat != null) {
						writer.writeElement("D", PROPSTAT);

						writeProps(propstat, writer);
						writeError(propstat, writer);
						writeStatus(propstat, writer);
						writeResponseDescription(propstat, writer);
						writer.endElement("D", PROPSTAT);
					}
				}

				writeError(response, writer);
				writeStatus(response, writer);
				writeResponseDescription(response, writer);
				writer.endElement("D", RESPONSE);
			}
		}

		writeResponseDescription(multistatus, writer);
		writer.endElement("D", MULTISTATUS);
	}

	private static void writeProps(Prop parent, XMLWriter writer) throws IOException {
		// 命名空间错误 <bar:foo xmlns:bar=""/>
		// 自定义命名空间 <prop1 xmlns="http://example.com/neon/litmus/">value1</prop1>
		// 注意：自定义命名空间已合并到名称中

		String name, xmlns;
		writer.writeElement("D", PROP);
		if (parent.hasProp()) {
			for (Property property : parent.prop()) {
				if (RESOURCE_TYPE.equals(property.getName())) {
					// <resourcetype><collection/></resourcetype>

					writer.writeElement("D", property.getName());
					if (property.getValue() == Collection.INSTANCE) {
						writer.writeElement("D", COLLECTION);
						writer.endElement("D", COLLECTION);
					}
					writer.endElement("D", property.getName());
				} else {
					// 拆解含有命名空间的名称
					int s = property.getName().indexOf(' ');
					if (s > 0) {
						name = property.getName().substring(0, s);
						xmlns = property.getName().substring(s + 1);
						xmlns = xmlns.replace('(', '/');
						xmlns = xmlns.replace(')', ':');

					} else {
						name = property.getName();
						xmlns = null;
					}

					writer.writeElement(name);
					if (xmlns != null) {
						writer.writeAttribute("xmlns", xmlns);
					}
					if (property.getValue() != null) {
						writer.writeContent(property.getValue().toString());
					}
					writer.endElement(name);
				}
			}
		}
		writer.endElement("D", PROP);
	}

	private static void writeError(Error e, XMLWriter writer) throws IOException {
		if (e.getError() != null) {
			writer.writeElement("D", ERROR);
			writer.writeContent(e.getError());
			writer.endElement("D", ERROR);
		}
	}

	private static void writeStatus(Status s, XMLWriter writer) throws IOException {
		if (s.getStatus() != null) {
			writer.writeElement("D", STATUS);
			writer.writeContent(s.version());
			writer.writeContent(" ");
			writer.writeContent(s.getStatus());
			writer.endElement("D", STATUS);
		}
	}

	private static void writeResponseDescription(ResponseDescription d, XMLWriter writer) throws IOException {
		if (d.getDescription() != null) {
			writer.writeElement("D", RESPONSEDESCRIPTION);
			writer.writeContent(d.getDescription());
			writer.endElement("D", RESPONSEDESCRIPTION);
		}
	}
}
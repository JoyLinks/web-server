package com.joyzl.webserver.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;

import com.joyzl.codec.XMLElementType;
import com.joyzl.codec.XMLReader;
import com.joyzl.codec.XMLWriter;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.http.Request;
import com.joyzl.webserver.webdav.elements.Element;
import com.joyzl.webserver.webdav.elements.Error;
import com.joyzl.webserver.webdav.elements.Include;
import com.joyzl.webserver.webdav.elements.LockInfo;
import com.joyzl.webserver.webdav.elements.LockScope;
import com.joyzl.webserver.webdav.elements.LockType;
import com.joyzl.webserver.webdav.elements.Multistatus;
import com.joyzl.webserver.webdav.elements.Owner;
import com.joyzl.webserver.webdav.elements.Prop;
import com.joyzl.webserver.webdav.elements.PropertyUpdate;
import com.joyzl.webserver.webdav.elements.Propfind;
import com.joyzl.webserver.webdav.elements.Propstat;
import com.joyzl.webserver.webdav.elements.Remove;
import com.joyzl.webserver.webdav.elements.Response;
import com.joyzl.webserver.webdav.elements.ResponseDescription;
import com.joyzl.webserver.webdav.elements.Set;
import com.joyzl.webserver.webdav.elements.Status;

/**
 * WEBDAV XML Coder
 * 
 * @author ZhangXi 2025年2月14日
 */
public class XMLCoder extends WEBDAV {

	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> clazz, Request request) throws IOException {
		if (request.hasContent()) {
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
				lockInfo.setOwner(readOwner(reader));
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

	private static Owner readOwner(XMLReader reader) throws IOException {
		final Owner owner = new Owner();
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(HREF)) {
				owner.setHref(reader.getContent());
			} else {
				// 忽略无法识别的元素
			}
		}
		return owner;
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
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(SET)) {
				propertyUpdate.setSet(new Set());
				readSet(reader, propertyUpdate.getSet());
			} else if (reader.isName(REMOVE)) {
				propertyUpdate.setRemove(new Remove());
				readRemove(reader, propertyUpdate.getRemove());
			} else {
				// 忽略无法识别的元素
			}
		}
		return propertyUpdate;
	}

	private static void readSet(XMLReader reader, Set set) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(PROP)) {
				readProp(reader, set);
			} else {
				// 忽略无法识别的元素
			}
		}
	}

	private static void readRemove(XMLReader reader, Remove remove) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isName(PROP)) {
				readProp(reader, remove);
			} else {
				// 忽略无法识别的元素
			}
		}
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
				readProp(reader, propfind);
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

	private static void readInclude(XMLReader reader, Include include) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			include.getInclude().add(reader.getName());
		}
	}

	private static void readProp(XMLReader reader, Prop prop) throws IOException {
		final int depth = reader.depth();
		while (reader.nextElement() && reader.depth() > depth) {
			if (reader.isEnd()) {

			}
			if (reader.hasContent()) {
				prop.getProp().put(reader.getName(), reader.getContent());
			} else {
				prop.getProp().put(reader.getName(), null);
			}
		}
	}

	public static void write(Multistatus multistatus, com.joyzl.network.http.Response output) throws IOException {

	}

	public static void write(Multistatus multistatus, OutputStream output) throws IOException {
		write(multistatus, new XMLWriter(output));
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

						writer.writeElement("D", PROP);
						for (Entry<String, Object> entry : propstat.getProp().entrySet()) {
							writer.writeElement(entry.getKey());
							if (entry.getValue() != null) {
								writer.writeContent(entry.getValue().toString());
							}
							writer.endElement(entry.getKey());
						}
						writer.endElement("D", PROP);

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
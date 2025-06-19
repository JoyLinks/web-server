package com.joyzl.webserver.manage;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.MIMEType;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.webserver.entities.Host;
import com.joyzl.webserver.entities.HostVisit;
import com.joyzl.webserver.entities.Server;
import com.joyzl.webserver.entities.ServerVisit;
import com.joyzl.webserver.service.Serializer;
import com.joyzl.webserver.service.Services;
import com.joyzl.webserver.servlet.CROSServlet;
import com.joyzl.webserver.servlet.ServletPath;

/**
 * 访问量获取接口
 * 
 * @author ZhangXi 2024年11月15日
 */
@ServletPath(path = "/manage/visits")
public class VisitsServlet extends CROSServlet {

	public VisitsServlet(String path) {
		super(path);
	}

	@Override
	public String name() {
		return "VISITS";
	}

	/**
	 * 列出或获取日志
	 */
	@Override
	protected void get(Request request, Response response) throws Exception {
		final List<ServerVisit> servers = new ArrayList<>();
		HostVisit hv;
		ServerVisit sv;
		for (Server server : Services.all()) {
			if (server.server() != null) {
				sv = new ServerVisit();
				sv.setName(server.getName());
				sv.setTimestamp(server.server().timestamp());
				sv.setIntercepts(server.service().intercepts());
				sv.setVisits(server.service().visits());
				sv.setLogs(server.service().logger() != null);

				for (Host host : server.getHosts()) {
					hv = new HostVisit();
					hv.setName(host.getName());
					if (host.service() != null) {
						hv.setIntercepts(host.service().intercepts());
						hv.setVisits(host.service().visits());
						hv.setLogs(host.service().logger() != null);

						sv.setVisits(sv.getVisits() + hv.getVisits());
						sv.setIntercepts(sv.getIntercepts() + hv.getIntercepts());
					}
					sv.getHosts().add(hv);
				}
				servers.add(sv);
			}
		}

		response.addHeader(CacheControl.NAME, CacheControl.NO_STORE);
		response.addHeader(ContentType.NAME, MIMEType.APPLICATION_JSON);
		final DataBufferOutput output = new DataBufferOutput();
		try (final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
			Serializer.JSON().writeEntities(servers, writer);
			writer.flush();
		}
		response.setContent(output.buffer());

	}

	@Override
	protected String allowMethods() {
		return "OPTIONS,GET";
	}

	@Override
	protected String allowHeaders() {
		return "*,Content-Type,Authorization";
	}

	@Override
	protected boolean allowCredentials() {
		return true;
	}
}
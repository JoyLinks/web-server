package com.joyzl.webserver.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.DirectoryResource;
import com.joyzl.network.web.FileResource;
import com.joyzl.network.web.FileResourceServlet;
import com.joyzl.network.web.WEBResource;

public class FileServlet extends FileResourceServlet {

	public FileServlet(File root) {
		super(root);
	}

	@Override
	protected WEBResource find(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void delete(Request request, Response response) throws Exception {

	}

	@Override
	protected void put(Request request, Response response) throws Exception {
		final WEBResource resource = create(request);
		if (resource == null) {

		} else {
			response.addHeader(HTTP.Location, resource.getContentLocation());
			response.setStatus(HTTPStatus.CREATED);
		}
	}

	/**
	 * PUT 请求创建资源
	 */
	protected WEBResource create(Request request) {
		if (request.getContent() != null) {
			final File file = new File(getRoot(), request.getPath());
			try (FileOutputStream output = new FileOutputStream(file)) {
				final DataBuffer buffer = (DataBuffer) request.getContent();
				buffer.read(output);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return new FileResource(request.getPath(), file, isWeak());
		}
		return null;
	}

	/**
	 * DELETE 请求删除资源
	 */
	protected WEBResource delete(Request request) {
		final WEBResource resource = find(request.getPath());
		if (resource != null) {
			if (resource instanceof FileResource) {
				final FileResource file = (FileResource) resource;
				if (file.getFile().delete()) {
					return resource;
				}
			} else //
			if (resource instanceof DirectoryResource) {
				final FileResource file = (FileResource) resource;
				if (file.getFile().delete()) {
					return resource;
				}
			}
		}
		return null;
	}
}
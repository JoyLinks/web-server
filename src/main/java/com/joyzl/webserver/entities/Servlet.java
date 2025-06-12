package com.joyzl.webserver.entities;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.webserver.Utility;
import com.joyzl.webserver.manage.EmojiServlet;
import com.joyzl.webserver.manage.LogServlet;
import com.joyzl.webserver.manage.RosterServlet;
import com.joyzl.webserver.manage.SettingServlet;
import com.joyzl.webserver.manage.UserServlet;
import com.joyzl.webserver.servlet.Location;
import com.joyzl.webserver.servlet.Wildcards;
import com.joyzl.webserver.web.FileResourceServlet;
import com.joyzl.webserver.webdav.FileWEBDAVServlet;

/**
 * 服务程序
 * 
 * @author ZhangXi 2025年6月6日
 */
public class Servlet {

	/** 服务类型标识 */
	private String type;
	/** 匹配资源路径 */
	private String path;
	/** 附加标头 */
	private final Map<String, String> headers = new ConcurrentHashMap<>();

	/** 请求重定向:URI */
	private String location;
	/** 请求重定向:状态码 */
	private Integer status;

	/** 资源内容目录 */
	private String content;
	/** 资源缓存目录 */
	private String cache;
	/** 资源错误页面目录 */
	private String error;
	/** 资源默认页面文件名 */
	private List<String> defaults;
	/** 资源压缩文件扩展名 */
	private List<String> compresses;
	/** 资源缓存文件扩展名 */
	private List<String> caches;
	/** 资源可浏览 */
	private Boolean browsable;
	/** 资源可编辑 */
	private Boolean editable;
	/** 资源弱验证器 */
	private Boolean weak;

	/** 资源获取所有属性 */
	private Boolean allProperty;

	private com.joyzl.webserver.servlet.Servlet service;

	public com.joyzl.webserver.servlet.Servlet service() {
		return service;
	}

	public boolean different() {
		if (service == null) {
			return true;
		}
		if (service.name().equalsIgnoreCase(type)) {
			// continue;
		} else {
			return true;
		}

		if (service instanceof FileResourceServlet servlet) {
			if (Utility.equal(Utility.correctBase(path), servlet.getPath())) {
				if (Utility.equal(servlet.getRoot(), content)) {
					if (Utility.equal(servlet.getCache(), cache)) {
						if (Utility.equal(servlet.getErrorPages(), error)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		if (service instanceof FileWEBDAVServlet servlet) {
			if (Utility.equal(Utility.correctBase(path), servlet.getPath())) {
				if (allProperty() == servlet.isAllProperty()) {
					if (servlet.getRoot().equals(Path.of(content))) {
						return false;
					}
				}
			}
			return true;
		}

		if (service instanceof Location servlet) {
			if (Utility.isEmpty(path)) {
				if (!Utility.equal(Wildcards.STAR, servlet.getPath())) {
					return true;
				}
			}
			if (Utility.equal(path, servlet.getPath())) {
				if (Utility.equal(location, servlet.getLocation())) {
					if (status == null) {
						return servlet.getStatus() != HTTPStatus.MOVED_PERMANENTLY;
					}
					return servlet.getStatus().code() != status;
				}
			}
			return true;
		}
		return true;
	}

	public void reset() {
		if (type != null) {
			if (FileResourceServlet.NAME.equalsIgnoreCase(type)) {
				service = new FileResourceServlet(path, content, cache, error, //
					defaults(), compresses(), caches(), //
					browsable(), editable(), weak());
			} else //
			if (FileWEBDAVServlet.NAME.equalsIgnoreCase(type)) {
				service = new FileWEBDAVServlet(path, content, allProperty());
			} else //
			if (Location.NAME.equalsIgnoreCase(type)) {
				service = new Location(path, location, status());
			} else //
			if (SettingServlet.NAME.equalsIgnoreCase(type)) {
				service = new SettingServlet();
			} else //
			if (RosterServlet.NAME.equalsIgnoreCase(type)) {
				service = new RosterServlet();
			} else //
			if (UserServlet.NAME.equalsIgnoreCase(type)) {
				service = new UserServlet();
			} else //
			if (LogServlet.NAME.equalsIgnoreCase(type)) {
				service = new LogServlet(path);
			} else //
			if (EmojiServlet.NAME.equalsIgnoreCase(type)) {
				service = new EmojiServlet();
			}
		} else {
			service = null;
		}
	}

	public boolean allProperty() {
		return allProperty == null ? false : allProperty.booleanValue();
	}

	public HTTPStatus status() {
		return status == null ? null : HTTPStatus.fromCode(status);
	}

	public String[] defaults() {
		return defaults == null ? null : defaults.toArray(new String[defaults.size()]);
	}

	public String[] compresses() {
		return compresses == null ? null : compresses.toArray(new String[compresses.size()]);
	}

	public String[] caches() {
		return caches == null ? null : caches.toArray(new String[caches.size()]);
	}

	public boolean browsable() {
		return browsable == null ? false : browsable.booleanValue();
	}

	public boolean editable() {
		return editable == null ? false : editable.booleanValue();
	}

	public boolean weak() {
		return weak == null ? false : weak.booleanValue();
	}

	/**
	 * 获取服务类型
	 */
	public String getType() {
		return type;
	}

	/**
	 * 设置服务类型
	 */
	public void setType(String value) {
		type = value;
	}

	/**
	 * 获取资源路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置资源路径
	 */
	public void setPath(String value) {
		path = value;
	}

	/**
	 * 获取附加标头，附加标头将添加到每个响应
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * 设置附加标头，附加标头将添加到每个响应
	 */
	public void setHeaders(Map<String, String> values) {
		if (values != headers) {
			headers.clear();
			headers.putAll(values);
		}
	}

	/**
	 * 获取默认文件名
	 */
	public List<String> getDefaults() {
		return defaults;
	}

	/**
	 * 设置默认文件名
	 */
	public void setDefaults(List<String> values) {
		defaults = values;
	}

	/**
	 * 获取应压缩文件的扩展名（同时缓存）
	 */
	public List<String> getCompresses() {
		return compresses;
	}

	/**
	 * 设置应压缩文件的扩展名（同时缓存）
	 */
	public void setCompresses(List<String> values) {
		compresses = values;
	}

	/**
	 * 获取应缓存文件的扩展名
	 */
	public List<String> getCaches() {
		return caches;
	}

	/**
	 * 设置应缓存文件的扩展名
	 */
	public void setCaches(List<String> values) {
		caches = values;
	}

	/**
	 * 获取内容目录
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 设置内容目录
	 */
	public void setContent(String value) {
		content = value;
	}

	/**
	 * 获取缓存目录
	 */
	public String getCache() {
		return cache;
	}

	/**
	 * 设置缓存目录
	 */
	public void setCache(String value) {
		cache = value;
	}

	/**
	 * 获取错误页面目录
	 */
	public String getError() {
		return error;
	}

	/**
	 * 设置错误页面目录
	 */
	public void setError(String value) {
		error = value;
	}

	/**
	 * 获取是否使用弱验证器
	 */
	public Boolean isWeak() {
		return weak;
	}

	/**
	 * 设置是否使用弱验证器
	 */
	public void setWeak(Boolean value) {
		weak = value;
	}

	/**
	 * 获取是否可浏览目录
	 */
	public Boolean isBrowsable() {
		return browsable;
	}

	/**
	 * 设置是否可浏览目录
	 */
	public void setBrowsable(Boolean value) {
		browsable = value;
	}

	/**
	 * 获取是否可创建资源
	 */
	public Boolean isEditable() {
		return editable;
	}

	/**
	 * 设置是否可创建资源
	 */
	public void setEditable(Boolean value) {
		editable = value;
	}

	/**
	 * 获取重定向目标位置
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * 设置重定向目标位置
	 */
	public void setLocation(String value) {
		location = value;
	}

	/**
	 * 获取重定向状态码
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置重定向状态码
	 */
	public void setStatus(Integer value) {
		status = value;
	}

	public Boolean isAllProperty() {
		return allProperty;
	}

	public void setAllProperty(Boolean value) {
		allProperty = value;
	}

	@Override
	public String toString() {
		return getType() + " " + getPath();
	}
}
package com.joyzl.webserver.webdav;

import com.joyzl.network.StringSeeker;

/**
 * http://www.webdav.org/
 * 
 * @author ZhangXi 2025年2月12日
 */
public class WEBDAV {

	// 元素

	final static String ACTIVELOCK = "activelock";
	final static String ALLPROP = "allprop";
	final static String COLLECTION = "collection";
	final static String DEPTH = "depth";
	final static String ERROR = "error";
	final static String EXCLUSIVE = "exclusive";
	final static String HREF = "href";
	final static String INCLUDE = "include";
	final static String LOCATION = "location";
	final static String LOCKENTRY = "lockentry";
	final static String LOCKINFO = "lockinfo";
	final static String LOCKROOT = "lockroot";
	final static String LOCKSCOPE = "lockscope";
	final static String LOCKTOKEN = "locktoken";
	final static String LOCKTYPE = "locktype";
	final static String MULTISTATUS = "multistatus";
	final static String OWNER = "owner";
	final static String PROP = "prop";
	final static String PROPERTYUPDATE = "propertyupdate";
	final static String PROPFIND = "propfind";
	final static String PROPNAME = "propname";
	final static String PROPSTAT = "propstat";
	final static String REMOVE = "remove";
	final static String RESPONSE = "response";
	final static String RESPONSEDESCRIPTION = "responsedescription";
	final static String SET = "set";
	final static String SHARED = "shared";
	final static String STATUS = "status";
	final static String TIMEOUT = "timeout";
	final static String WRITE = "write";

	// 属性

	final static String CREATION_DATE = "creationdate";
	final static String DISPLAY_NAME = "displayname";
	final static String GET_CONTENT_LANGUAGE = "getcontentlanguage";
	final static String GET_CONTENT_LENGTH = "getcontentlength";
	final static String GET_CONTENT_TYPE = "getcontenttype";
	final static String GET_ETAG = "getetag";
	final static String GET_LAST_MODIFIED = "getlastmodified";
	final static String LOCK_DISCOVERY = "lockdiscovery";
	final static String RESOURCE_TYPE = "resourcetype";
	final static String SUPPORTED_LOCK = "supportedlock";

	final static String INFINITY = "infinity";

	public final static StringSeeker NAMES = new StringSeeker(new String[] { //
			ACTIVELOCK, //
			ALLPROP, //
			COLLECTION, //
			DEPTH, //
			ERROR, //
			EXCLUSIVE, //
			HREF, //
			INCLUDE, //
			LOCATION, //
			LOCKENTRY, //
			LOCKINFO, //
			LOCKROOT, //
			LOCKSCOPE, //
			LOCKTOKEN, //
			LOCKTYPE, //
			MULTISTATUS, //
			OWNER, //
			PROP, //
			PROPERTYUPDATE, //
			PROPFIND, //
			PROPNAME, //
			PROPSTAT, //
			REMOVE, //
			RESPONSE, //
			RESPONSEDESCRIPTION, //
			SET, //
			SHARED, //
			STATUS, //
			TIMEOUT, //
			WRITE, //
			CREATION_DATE, //
			DISPLAY_NAME, //
			GET_CONTENT_LANGUAGE, //
			GET_CONTENT_LENGTH, //
			GET_CONTENT_TYPE, //
			GET_ETAG, //
			GET_LAST_MODIFIED, //
			LOCK_DISCOVERY, //
			RESOURCE_TYPE, //
			SUPPORTED_LOCK, //
			INFINITY });
}
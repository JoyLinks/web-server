/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
module com.joyzl.webserver {
	requires transitive com.joyzl.network;
	requires transitive com.joyzl.logger;

	exports com.joyzl.webserver.webdav.elements to com.joyzl.odbs;
	exports com.joyzl.webserver.entities to com.joyzl.odbs;
	exports com.joyzl.webserver.service to com.joyzl.odbs;

	exports com.joyzl.webserver.web;
	exports com.joyzl.webserver.webdav;
	exports com.joyzl.webserver.servlet;
	exports com.joyzl.webserver.authenticate;
}
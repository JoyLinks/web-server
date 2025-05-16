module com.joyzl.webserver {
	requires transitive com.joyzl.network;
	requires transitive com.joyzl.logger;

	exports com.joyzl.webserver.entities to com.joyzl.odbs;
	exports com.joyzl.webserver.manage to com.joyzl.odbs;
	exports com.joyzl.webserver.web;
}
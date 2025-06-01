package com.joyzl.webserver;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TestFileWatch {

	@Test
	void test() throws IOException {
		final Path dir = Path.of("").toAbsolutePath();
		final Path file = dir.resolve("test.txt");
		System.out.println(file);
		FileSystems.getDefault().newWatchService();
		file.register(SERVICE, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

	}

	final WatchService SERVICE = new WatchService() {

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public WatchKey poll() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WatchKey take() throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}
	};
}
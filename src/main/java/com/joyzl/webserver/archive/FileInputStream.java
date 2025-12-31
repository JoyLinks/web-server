/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.webserver.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * FileChannel InputStream
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2025年7月30日
 */
class FileInputStream extends InputStream {

	private final FileChannel channel;
	private final ByteBuffer buffer;
	private long position = 0;
	private long mark = -1;

	public FileInputStream(Path file) throws IOException {
		channel = FileChannel.open(file, StandardOpenOption.READ);
		buffer = ByteBuffer.allocateDirect(1024 * 8);
		buffer.flip();
	}

	@Override
	public int read() throws IOException {
		if (buffer.hasRemaining()) {
			return buffer.get() & 0xFF;
		} else {
			buffer.clear();
			int size = channel.read(buffer, position);
			if (size <= 0) {
				return -1;
			}
			position += size;
			buffer.flip();
			return buffer.get() & 0xFF;
		}
	}

	@Override
	public int read(byte bytes[], int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		if (bytes == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || len > bytes.length - off) {
			throw new IndexOutOfBoundsException();
		}
		if (len <= buffer.remaining()) {
			buffer.get(bytes, off, len);
			return len;
		} else {
			int size = buffer.remaining();
			if (size > 0) {
				buffer.get(bytes, off, size);
				off += size;
				len -= size;
			} else if (position >= channel.size()) {
				return -1;
			}

			int s = channel.read(ByteBuffer.wrap(bytes, off, len), position);
			if (s > 0) {
				position += s;
				size += s;
			}
			return size;
		}
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		if (n <= buffer.remaining()) {
			buffer.position((int) (buffer.position() + n));
			return n;
		} else {
			long s = n - buffer.remaining();
			buffer.position(buffer.limit());

			if (s <= channel.size() - position) {
				channel.position(position += s);
				return n;
			} else {
				s -= channel.size() - position;
				channel.position(position += channel.size());
				return n - s;
			}
		}
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readlimit) {
		mark = position();
	}

	@Override
	public void reset() throws IOException {
		if (mark == -1) {
			throw new IOException("Mark not set");
		}
		position = mark;
		buffer.position(buffer.limit());
	}

	@Override
	public int available() throws IOException {
		long size = buffer.remaining() + channel.size() - position;
		if (size > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else {
			return (int) size;
		}
	}

	@Override
	public void close() throws IOException {
		if (channel.isOpen()) {
			channel.close();
		}
	}

	public long position() {
		return position - buffer.remaining();
	}

	public int read2Byte() throws IOException {
		return read() << 8 | read();
	}

	public int read4Byte() throws IOException {
		return read() << 24 | read() << 16 | read() << 8 | read();
	}

	public long read8Byte() throws IOException {
		return ((long) read()) << 56 | ((long) read()) << 48 | ((long) read()) << 40 | ((long) read()) << 32 | ((long) read()) << 24 | ((long) read()) << 16 | ((long) read()) << 8 | (long) read();
	}

	public InputStream range(final long start, final long length) throws IOException {
		if (start < 0 || start > channel.size()) {
			throw new IndexOutOfBoundsException(start);
		}
		if (length < 0 || start + length > channel.size()) {
			throw new IndexOutOfBoundsException(length);
		}
		if (start != position()) {
			buffer.position(buffer.limit());
			position = start;
		}

		return new InputStream() {
			private final long end = start + length;

			@Override
			public int available() throws IOException {
				long size = buffer.remaining() + (end - position);
				if (size > Integer.MAX_VALUE) {
					return Integer.MAX_VALUE;
				} else {
					return (int) size;
				}
			}

			@Override
			public int read() throws IOException {
				if (position() >= end) {
					return -1;
				}
				if (buffer.hasRemaining()) {
					return buffer.get() & 0xFF;
				} else {
					buffer.clear();
					if (end - position < buffer.capacity()) {
						buffer.limit((int) (end - position));
					}

					int size = channel.read(buffer, position);
					if (size <= 0) {
						return -1;
					}
					position += size;
					buffer.flip();

					return buffer.get() & 0xFF;
				}
			}

			@Override
			public int read(byte bytes[], int off, int len) throws IOException {
				if (len == 0) {
					return 0;
				}
				if (bytes == null) {
					throw new NullPointerException();
				}
				if (off < 0 || len < 0 || len > bytes.length - off) {
					throw new IndexOutOfBoundsException();
				}

				int size = available();
				if (size <= 0) {
					return -1;
				}
				if (len > size) {
					len = size;
				}

				if (len <= buffer.remaining()) {
					buffer.get(bytes, off, len);
					return len;
				} else {
					size = buffer.remaining();
					if (size > 0) {
						buffer.get(bytes, off, size);
						off += size;
						len -= size;
					}

					int s = channel.read(ByteBuffer.wrap(bytes, off, len), position);
					if (s > 0) {
						position += s;
						size += s;
					}
					return size;
				}
			}

			@Override
			public long skip(long n) throws IOException {
				if (n <= 0) {
					return 0;
				}
				// available
				long size = buffer.remaining() + (end - position);
				if (size <= 0) {
					return 0;
				}
				if (n > size) {
					n = size;
				}

				if (n <= buffer.remaining()) {
					buffer.position((int) (buffer.position() + n));
					return n;
				} else {
					long s = n - buffer.remaining();
					buffer.position(buffer.limit());

					if (s <= end - position) {
						channel.position(position += s);
						return n;
					} else {
						s -= end - position;
						channel.position(end);
						return n - s;
					}
				}
			}

			@Override
			public boolean markSupported() {
				return true;
			}

			@Override
			public void mark(int readlimit) {
				mark = position();
			}

			@Override
			public void reset() throws IOException {
				if (mark == -1) {
					throw new IOException("Mark not set");
				}
				position = mark;
				buffer.position(buffer.limit());
			}

			@Override
			public void close() throws IOException {
				channel.close();
			}
		};
	}
}
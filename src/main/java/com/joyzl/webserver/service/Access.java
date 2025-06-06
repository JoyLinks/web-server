package com.joyzl.webserver.service;

import java.io.File;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.joyzl.logger.clf.CLFRecord;
import com.joyzl.logger.clf.CommonLogger;
import com.joyzl.logger.clf.OptionalField;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 访问日志
 * 
 * @author ZhangXi 2024年12月10日
 */
public class Access {

	protected final AtomicLong counter = new AtomicLong();

	public Access() {
	}

	public void record(HTTPSlave chain, Request request) {
		counter.incrementAndGet();
	}

	public void record(HTTPSlave chain, Response response) {
		counter.incrementAndGet();
	}

	public long count() {
		return counter.get();
	}

	final static class AccessCommonLogger extends Access {
		private final CommonLogger logger;

		public AccessCommonLogger(String file) {
			logger = new CommonLogger(new File(file));
		}

		@Override
		public void record(HTTPSlave chain, Request request) {
			logger.record(new CLFRecord() {
				@Override
				public long getTimestamp() {
					return System.currentTimeMillis();
				}

				@Override
				public char getType() {
					return REQUEST;
				}

				@Override
				public char getRetransmission() {
					return SERVER;
				}

				@Override
				public char getDirection() {
					return RECEIVED;
				}

				@Override
				public char getTransport() {
					return TCP;
				}

				@Override
				public char getEncryption() {
					return UNENCRYPTED;
				}

				@Override
				public String getSource() {
					final SocketAddress a = chain.getRemoteAddress();
					return a == null ? null : a.toString();
				}

				@Override
				public String getDestination() {
					final SocketAddress a = chain.getLocalAddress();
					return a == null ? null : a.toString();
				}

				@Override
				public String getFrom() {
					return request.getHeader(HTTP1.Referer);
				}

				@Override
				public String getFromTag() {
					return null;
				}

				@Override
				public String getTo() {
					return request.getPath();
				}

				@Override
				public String getToTag() {
					return null;
				}

				@Override
				public long getCSeqNumber() {
					return counter.incrementAndGet();
				}

				@Override
				public String getCSeqMethod() {
					return request.getMethod();
				}

				@Override
				public String getCallId() {
					return null;
				}

				@Override
				public String getRURI() {
					return request.getURL();
				}

				@Override
				public int getStatus() {
					return 0;
				}

				@Override
				public String getServerTxn() {
					return null;
				}

				@Override
				public String getClientTxn() {
					return null;
				}

				@Override
				public List<OptionalField> getOptionalFields() {
					return null;
				}
			});
		}

		@Override
		public void record(HTTPSlave chain, Response response) {
			logger.record(new CLFRecord() {
				@Override
				public long getTimestamp() {
					return System.currentTimeMillis();
				}

				@Override
				public char getType() {
					return RESPONSE;
				}

				@Override
				public char getRetransmission() {
					return SERVER;
				}

				@Override
				public char getDirection() {
					return SENT;
				}

				@Override
				public char getTransport() {
					return TCP;
				}

				@Override
				public char getEncryption() {
					return UNENCRYPTED;
				}

				@Override
				public String getSource() {
					final SocketAddress a = chain.getRemoteAddress();
					return a == null ? null : a.toString();
				}

				@Override
				public String getDestination() {
					final SocketAddress a = chain.getLocalAddress();
					return a == null ? null : a.toString();
				}

				@Override
				public String getFrom() {
					return response.getHeader(HTTP1.Referer);
				}

				@Override
				public String getFromTag() {
					return null;
				}

				@Override
				public String getTo() {
					return null;
				}

				@Override
				public String getToTag() {
					return null;
				}

				@Override
				public long getCSeqNumber() {
					return counter.get();
				}

				@Override
				public String getCSeqMethod() {
					return null;
				}

				@Override
				public String getCallId() {
					return null;
				}

				@Override
				public String getRURI() {
					return null;
				}

				@Override
				public int getStatus() {
					return response.getStatus();
				}

				@Override
				public String getServerTxn() {
					return null;
				}

				@Override
				public String getClientTxn() {
					return null;
				}

				@Override
				public List<OptionalField> getOptionalFields() {
					return null;
				}
			});
		}
	}
}
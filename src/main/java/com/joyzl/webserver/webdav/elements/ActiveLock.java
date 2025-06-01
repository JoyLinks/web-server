package com.joyzl.webserver.webdav.elements;

import java.nio.file.Path;

/**
 * 资源上的锁
 * 
 * <pre>
 * <!ELEMENT activelock (lockscope, locktype, depth, owner?, timeout?, locktoken?, lockroot)>
 * <!ELEMENT lockroot (href) >
 * <!ELEMENT locktoken (href) >
 * </pre>
 * 
 * @author ZhangXi 2025年2月9日
 */
public class ActiveLock extends LockInfo implements Timeout {

	private int depth;
	private long timeout;
	private String locktoken;
	private String lockroot;
	private long timestamp;
	private Path path;

	public ActiveLock() {
	}

	public ActiveLock(LockInfo lockInfo) {
		setLockScope(lockInfo.getLockScope());
		setLockType(lockInfo.getLockType());
		setOwner(lockInfo.getOwner());
	}

	public String getLockRoot() {
		return lockroot;
	}

	public void setLockRoot(String value) {
		lockroot = value;
	}

	public String getLockToken() {
		return locktoken;
	}

	public void setLockToken(String value) {
		locktoken = value;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int value) {
		depth = value;
	}

	public Path path() {
		return path;
	}

	public void path(Path value) {
		path = value;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(long value) {
		timestamp = System.currentTimeMillis();
		timeout = value;
	}

	@Override
	public boolean valid() {
		return System.currentTimeMillis() - this.timestamp < timeout * 1000;
	}

	@Override
	public String toString() {
		return lockroot + "," + locktoken + ",depth:" + depth;
	}
}
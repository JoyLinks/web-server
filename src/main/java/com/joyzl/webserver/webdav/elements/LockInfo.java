package com.joyzl.webserver.webdav.elements;

/**
 * 指定客户端希望创建的锁的类型
 * 
 * @author ZhangXi 2025年2月9日
 */
public class LockInfo extends Element {
	/*-
	 * <!ELEMENT lockinfo (lockscope, locktype, owner?) >
	 */

	private LockScope lockScope;
	private LockType lockType;
	private Owner owner;

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner value) {
		owner = value;
	}

	public LockType getLockType() {
		return lockType;
	}

	public void setLockType(LockType value) {
		lockType = value;
	}

	public LockScope getLockScope() {
		return lockScope;
	}

	public void setLockScope(LockScope value) {
		lockScope = value;
	}
}
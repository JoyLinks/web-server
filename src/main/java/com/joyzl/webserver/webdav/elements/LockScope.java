package com.joyzl.webserver.webdav.elements;

/**
 * 排它锁还是共享锁
 * 
 * @author ZhangXi 2025年2月9日
 */
public enum LockScope {
	/*-
	 * <!ELEMENT lockscope (exclusive | shared) >
	 * <!ELEMENT exclusive EMPTY >
	 * <!ELEMENT shared EMPTY >
	 */
	/** 共享锁 */
	SHARED,
	/** 排他锁 */
	EXCLUSIVE;
}
/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
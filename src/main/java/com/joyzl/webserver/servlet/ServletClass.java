/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.webserver.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Servlet 注解，指示重载的服务程序类
 * 
 * @author ZhangXi
 * @date 2020年11月10日
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServletClass {

	/**
	 * 重载工具关联的服务程序类
	 */
	Class<? extends Servlet> servlet();
}
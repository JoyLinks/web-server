/*
 * Copyright © 2024-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
/**
 * 支持远程管理配置服务的服务程序（接口），实体格式为JSON，采用UTF-8字符编码，支持CROS。
 * 开发者可通过管理接口利用前端技术实现自己的服务管理工具，支持热配置，更改可立即生效而无需重启服务。
 * 由于管理接口具有危险性，可能导致服务意外停止，当开启这些接口时应配置必要的安全策略。
 * <ul>
 * <li>SettingServlet 服务配置接口，服务名SETTING，默认路径/manage/setting</li>
 * <li>RosterServlet 黑白名单接口，服务名ROSTER，默认路径/manage/roster</li>
 * <li>UserServlet 用户管理接口，服务名USER，默认路径/manage/user</li>
 * <li>LogServlet 日志接口，服务名LOG，默认路径/manage/log</li>
 * </ul>
 */
package com.joyzl.webserver.manage;

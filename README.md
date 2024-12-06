# JOYZL web-server
JOYZL WEB Server 是一个 WEB HTTP Server (通过HTTP协议提供WEB服务的服务器软件)；
纯粹中国智造的国产化软件，支持高性能高吞吐量的 WEB 服务，
内容自动缓存（支持常规文件缓存，以及高性能内存缓存），
内容压缩（GZip），反向代理、群集与负载均衡。

## 安装与使用

可独立部署，作为WEB内容服务器;

可作为组件集成到您的项目中，支持 Servlet (不是 Jakarta Servlet，也不支持 JSP) 扩展功能接口；
项目即可作为独立应用启动，无须额外部署 WEB HTTP Server 与 SpringBoot 启动方式类似。

更多内容及文档请参考官网
[JOYZL WEB Server](http://web.joyzl.net)

## 特性

Windows系统文件扩展名不区分大小写，而路径和文件名区分大小写。
不再支持 CGI、JSP、PHP、ASP 等过时的功能。

## 开发计划

内存缓存

HTTPS

HEADERS

反向代理与负载均衡

攻击与漏洞封堵
	IP黑名单
	防暴力破解
	权限
session

103 Early Hints
SPDY -> HTTP/2

RFC 6872 The Common Log Format (CLF) for the Session Initiation Protocol (SIP): Framework and Information Model 
access.log Server/Host
error.log 不在区分

计数器 总计数（Server/Host/Resource）


### 语言代码（Language Codes）

由 [www.iso639-3.sil.org](http://www.iso639-3.sil.org/) 提供 ISO 639-3 语言代码集

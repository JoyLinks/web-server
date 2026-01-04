# JOYZL Server

JOYZL Server 是纯粹中国智造，完全开源的国产化 HTTP WEB Server 服务器软件。
主要功能是基于超文本传送协议(HTTP)提供安全可靠且性能卓越的资源和数据访问服务。

JOYZL Server 的核心组件为 JOYZL network ，没有任何第三方依赖。
JOYZL network 是历经十年以上持续开发和不断优化且完全开源的以太网通信组件；
最初为物联网项目而开发，其中包含对 HTTP 多个版本的协议实现，随后逐步完善并形成 JOYZL Server 独立软件。

主要特性：

* 支持 HTTP 1.0、HTTP 1.1、HTTP 2 超文本传输协议；
* 支持 TLS 1.3、TLS 1.2 传输层安全协议；
* 支持 WEB Site 资源发布服务；
* 支持 WEBDAV Class 1、2、3 分布式编辑与版本管理服务；
* 支持请求地址黑名单或白名单过滤；
* 支持请求身份验证 Basic Digest ；
* 支持热配置，设置更改无须重启；
* 支持界面化(GUI)配置。

## WEB Site 网站发布(FileResourceServlet,FileResourceReload)

配置静态网站资源发布，支持多域名和虚拟目录，支持资源压缩和内存缓存。

## WEBDAV 文件资源(FileWEBDAVServlet,FileWEBDAVReload)

配置 WEBDAV 访问服务器文件资源，支持 PROPFIND PROPPATCH MKCOL DELETE GET PUT COPY MOVE LOCK UNLOCK 请求方法。
资源锁(Resource Lock)仅在服务运行期间有效，重启后丢失；单个资源最多可同时创建64个共享锁。
支持XML和JSON实体格式，通过请求时指定 Content-Type:application/xml 或 Content-Type:application/json 以指定格式实体执行请求和响应。

## ARCHIVE 文件归集(ArchiveServlet,ArchiveReload)

配置 ARCHIVE 服务，提供文件上传归档功能，支持 multipart/form-data 方式上传文件。
文件按编码归集，每个编码可归集多个文件，可按编码查询或提取文件。

## Authorization 身份验证

配置任意资源路径要求客户端身份验证，已集成 Basic 和 Digest 身份验证方式。
提供两个特殊身份验证方式：None 和 Deny 既无条件通过和拒绝。

## 对象层级结构

```
Application 1
├Server 1
│├Service 1
│├HostService 1
│├Authentication 1
│├Authentication *
│├Servlet 1
│├Servlet *
│├Host 1
││├HostService 1
││├Authentication 1
││├Authentication *
││├Servlet 1
││└Servlet *
│└Host *
└Server *
```

## 优先级

按以下优先级执行并应用规则：
1. 黑名单阻止；
2. 白名单允许；
3. 身份验证；
4. 资源定位；

### 语言代码（Language Codes）

由 [www.iso639-3.sil.org](http://www.iso639-3.sil.org/) 提供 ISO 639-3 语言代码集

### 致谢

[MDN WEB Docs](https://developer.mozilla.org)

[WebDAV test suite litmus](https://github.com/tolsen/litmus)

[h2spec](https://github.com/summerwind/h2spec)

[WebServer Tester](https://github.com/ibnesayeed/webserver-tester)

---
[www.joyzl.com](http://www.joyzl.com)

Copyright © 2024-2025 重庆骄智科技有限公司.
本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
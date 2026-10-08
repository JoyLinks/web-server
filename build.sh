#!/bin/bash

java --version
echo
mvn --version
echo

VERSION=2.2.12
ARCH=$(arch)
rm -rf publish

# 编译
echo Maven Package
mvn -f pom.xml clean package -P publish -Dmaven.test.skip=true
echo

# 构建运行环境
echo Build executable JOYZL WEB Server
rm -rf publish/joyzl-web-server
# https://docs.oracle.com/en/java/javase/17/docs/specs/man/jlink.html
jlink \
	--add-modules jdk.charsets\
	--add-modules jdk.localedata\
	--module-path publish\lib\
	--add-modules com.joyzl.webserver\
	--output publish\joyzl-web-server\
	--ignore-signing-information\
	--include-locales=zh-cn\
	--no-header-files\
	--no-man-pages\
	--bind-services\
	--compress=2\
	--strip-debug

# 复制附属文件
mv publish/*.json publish/joyzl-web-server/
mv publish/server-linux.properties publish/joyzl-web-server/server.properties
mv publish/server.sh publish/joyzl-web-server/server.sh
mv publish/install.sh publish/joyzl-web-server/install.sh
mv publish/uninstall.sh publish/joyzl-web-server/uninstall.sh
mv publish/server.service publish/joyzl-web-server/server.service
rm -f publish/*

# 可执行文件
chmod +x publish/joyzl-web-server/*.sh

# 创建压缩包
tar -czf "publish/joyzl-web-server_linux-${ARCH}_${VERSION}.tar.gz" -C publish joyzl-web-server

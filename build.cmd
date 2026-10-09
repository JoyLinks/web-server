@ECHO OFF

java -version
ECHO=
CALL mvn --version
ECHO=

SET VERSION=2.2.13
SET ARCH=%PROCESSOR_ARCHITECTURE%

IF EXIST publish RD /S /Q publish

REM 编译
ECHO Maven Package
call mvn -f pom.xml clean package -P publish -Dmaven.test.skip=true

ECHO=
REM 构建运行环境
ECHO Build executable JOYZL WEB Server
IF EXIST publish\joyzl-web-server RD /S /Q publish\joyzl-web-server
REM https://docs.oracle.com/en/java/javase/17/docs/specs/man/jlink.html
jlink ^
	--add-modules jdk.charsets^
	--add-modules jdk.localedata^
	--module-path publish\lib^
	--add-modules com.joyzl.webserver^
	--output publish\joyzl-web-server^
	--ignore-signing-information^
	--include-locales=zh-cn^
	--no-header-files^
	--no-man-pages^
	--bind-services^
	--compress=2^
	--strip-debug

REM 复制附属文件
MOVE /Y publish\*.json publish\joyzl-web-server\
MOVE /Y publish\server_windows.properties publish\joyzl-web-server\server.properties
MOVE /Y publish\service_windows-%ARCH%.exe publish\joyzl-web-server\service.exe
MOVE /Y publish\uninstall.cmd publish\joyzl-web-server\uninstall.cmd
MOVE /Y publish\install.cmd publish\joyzl-web-server\install.cmd
MOVE /Y publish\server.cmd publish\joyzl-web-server\server.cmd
MOVE /Y publish\www publish\joyzl-web-server\www
DEL /F /Q publish\*

REM 构建压缩包
jar cfM publish\joyzl-web-server_windows-%ARCH%_%VERSION%.zip -C publish joyzl-web-server

ECHO=
PAUSE
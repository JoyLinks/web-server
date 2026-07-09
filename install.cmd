@ECHO OFF

SET DIR=C:\Program Files
SET VAR=%ProgramData%

:: 检查安装目录
IF DEFINED ProgramFiles (
	SET DIR=%ProgramFiles%
) ELSE (
	ECHO 请确保操作系统为64位
	PAUSE
	EXIT /b 1
)

:: 检查管理员权限
NET SESSION >nul 2>&1
IF %errorLevel% NEQ 0 (
	ECHO 请使用管理员权限运行此脚本！
	PAUSE
	EXIT /b 1
)

SET DIR=%DIR%\joyzl
MKDIR "%DIR%"
SET VAR=%VAR%\joyzl
MKDIR "%VAR%"
SET DIR=%DIR%\web-server
MKDIR "%DIR%"
SET VAR=%VAR%\web-server
MKDIR "%VAR%"
MKDIR "%VAR%\log"

IF EXIST "%DIR%" (
	XCOPY "%~dp0*.*" "%DIR%\" /F /E /I /H /Y /C
	IF EXIST "%DIR%\server.properties" MOVE "%DIR%\server.properties" "%VAR%\server.properties"
	MOVE "%DIR%\manage" "%VAR%\"
	MOVE "%DIR%\*.json" "%VAR%\"

	ECHO 注册服务

	CD %DIR%
	REM https://commons.apache.org/proper/commons-daemon
	service.exe //IS//JOYZL-WEB-Server ^
	--DisplayName="JOYZL WEB Server 网页文件资源服务器" ^
	--Description "HTTP WEB Server 网页文件资源服务器，提供 WEB、WEBDAV、Archive 等服务支持。" ^
	--JavaHome="%DIR%" ^
	--Startup=auto ^
	--StartMode=jvm ^
	--StartPath="%VAR%" ^
	--StartMethod=start ^
	--StartClass=com.joyzl.webserver.Application ^
	++JvmOptions=-Xms256m ^
	++JvmOptions=-Xmx2048m ^
	++JvmOptions=-Dfile.encoding=UTF-8 ^
	++JvmOptions=-Duser.timezone=GMT+08 ^
	++JvmOptions=-Duser.dir="%VAR%" ^
	--StopMode=jvm ^
	--StopPath="%VAR%" ^
	--StopClass=com.joyzl.webserver.Application ^
	--StopMethod=stop ^
 	--StopTimeout=30 ^
	--StdOutput="%VAR%\log\out.log" ^
	--StdError="%VAR%\log\err.log" ^
	--LogPath="%VAR%\log" ^
	--LogPrefix=daemon ^
	--PidFile=pid

	ECHO 安装完成!
	ECHO 程序位于 %DIR%
	ECHO 配置位于 %VAR%
) ELSE (
	ECHO 错误: 无法创建目录，可能是权限不足
	ECHO 请以管理员身份运行此脚本
)

PAUSE
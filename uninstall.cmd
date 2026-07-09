@ECHO OFF
CD /d "%~dp0"

:: 检查管理员权限
NET SESSION >nul 2>&1
IF %errorLevel% NEQ 0 (
	ECHO 请使用管理员权限运行此脚本！
	PAUSE
	EXIT /b 1
)

ECHO JOYZL WEB Server uninstall

ECHO 停止服务
service.exe stop JOYZL-WEB-Server
TIMEOUT /t 12 /nobreak > nul

ECHO 移除服务
service.exe delete JOYZL-WEB-Server
SC DELETE JOYZL-WEB-Server > nul

SET DIR=%ProgramFiles%\joyzl\web-server
IF EXIST "%DIR%" (
	ECHO %DIR%
	RMDIR /s /q "%DIR%"
)

SET DIR=%ProgramData%\joyzl\web-server
IF EXIST "%DIR%" (
	IF "%1"=="all" (
		ECHO %DIR%
		RMDIR /s /q "%DIR%"
	)
)

PAUSE
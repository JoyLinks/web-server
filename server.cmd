@ECHO OFF

::SWITCH DIR TO WORK
CD /D %~DP0

SET WORK_HOME=%CD%
SET JAVA_HOME=%CD%
SET JAVA_OPTIONS=-Xms256m -Xmx2048m -Duser.dir=%WORK_HOME% -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08
SET JAVA_EXECUTE=--module com.joyzl.webserver/com.joyzl.webserver.Application
SET JAVA_COMMAND=%JAVA_HOME%\bin\java.exe -server %JAVA_OPTIONS% %JAVA_EXECUTE%

ECHO JOYZL WEB HTTP Server starting
REM ECHO %JAVA_COMMAND%
%JAVA_COMMAND%

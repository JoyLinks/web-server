@ECHO OFF

java -version

set dir=%cd%\publish
IF EXIST %dir% RD /S /Q %dir%

ECHO Package
call mvn -f pom.xml clean package -DoutputDirectory=%dir% -Dmaven.test.skip=true

ECHO BUILD Runtime
jlink ^
	--module-path %dir%\lib^
	--output %dir%\server^
	--compress=2^
	--no-header-files^
	--no-man-pages^
	--strip-debug^
	--bind-services^
	--include-locales=zh-cn^
	--add-modules com.joyzl.webserver^
	--ignore-signing-information

%dir%\server\bin\java --list-modules

MOVE %dir%\*.* %dir%\server\

PAUSE
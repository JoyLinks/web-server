#!/bin/bash

dir=$(pwd)/publish
rm -rf $dir

echo package
mvn -f pom.xml clean package -DoutputDirectory=$dir -Dmaven.test.skip=true

echo BUILD Runtime
jlink \
	--module-path $dir/lib\
	--output $dir/server\
	--compress=2\
	--no-header-files\
	--no-man-pages\
	--bind-services\
	--include-locales=zh-cn\
	--add-modules com.joyzl.webserver

$dir/server/bin/java --list-modules

mv $dir/*.* $dir/server/

chmod +x $dir/server/server.sh
chmod +x $dir/server/debug.sh
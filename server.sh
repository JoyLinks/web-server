#!/bin/bash

# chkconfig: 2345 80 90
# description: JOYZL WEB Server
# processname: joyzl-web-server

SERVER=joyzl-web-server
JAVA_HOME="/opt/joyzl/web-server"

JAVA_SERVICE="com.joyzl.webserver/com.joyzl.webserver.Application"
JAVA_OPTIONS="-Xms256m -Xmx2048m -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08"
JAVA_COMMAND="authbind --deep $JAVA_HOME/bin/java -server $JAVA_OPTIONS --module $JAVA_SERVICE"

# 当前目录
CURRENT_DIR=$(pwd)
# 命令行参数
AUGMENT2=$2

usage()
{
    echo "Usage: $0 {start|start -debug|stop|restart|status|stop|command -f}"
    echo "Example: $0 start"
    exit 1
}

start()
{
    count=$(ps -ef |grep java|grep $JAVA_SERVICE|wc -l)
    if [ $count != 0 ]
	then
        echo "$SERVER 已在运行中"
    else
        echo "$SERVER 正在启动 ..."
        if [ "$AUGMENT2" = "-debug" ]
        then
        	echo $JAVA_COMMAND
        	echo
        	$JAVA_COMMAND
        else
        	nohup $JAVA_COMMAND >> /var/log/joyzl/web-server/console.log 2>&1 &
        fi
    fi
}

stop()
{
    PID=$(ps -ef |grep java|grep $JAVA_SERVICE|awk '{print $2}')
    if [ -z $PID ]
    then
        echo "$SERVER 未在运行中"
    else
        echo -n "$SERVER 正在停止 ..."
        if [ "$AUGMENT2" = "-f" ]
        then
        	# 强制终止 SIGKILL(9)
            echo "by force"
            kill -9 $PID
        else
        	# 执行退出 SIGTERM(15)
            echo
            kill $PID
        fi
    fi
}

status()
{
    PID=$(ps -ef |grep java|grep $JAVA_SERVICE|awk '{print $2}')
    if [ -z $PID ]
    then
        echo -e "\033[31m $SERVER 未运行 \033[0m"
    else
        echo -e "\033[32m $SERVER 在运行 [$PID] \033[0m"
    fi
}

restart()
{
    stop
    for i in 3 2 1
    do
        echo -n "$i "
        sleep 1
    done
    echo 0
    start
}

command()
{
    echo $JAVA_COMMAND
    exit 1
}

case $1 in
    start)
    start;;

    stop)
    stop;;

    restart)
    restart;;

    status)
    status;;
    
    command)
    command;;

    *)
    usage;;
esac

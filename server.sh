#!/bin/bash

# chkconfig: 2345 80 90
# description: JOYZL SCADA Server
# processname: scada-server

WORK_HOME=$(cd $(dirname "$0") && pwd)
JAVA_HOME="$WORK_HOME"
JAVA_SERVICE="com.joyzl.scada.servo/com.joyzl.scada.servo.Application"
JAVA_OPTIONS="-Xms256m -Xmx1024m -Duser.dir=$WORK_HOME -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08"
JAVA_EXECUTE="--module-path $WORK_HOME/lib/service --module $JAVA_SERVICE"
JAVA_COMMAND="$JAVA_HOME/bin/java -server $JAVA_OPTIONS $JAVA_EXECUTE"

SERVER=scada-servo
# command augment -debug/-f
AUGMENT2=$2

usage()
{
    echo "Usage: $0 {start -debug|stop|restart|status|stop|command -f}"
    echo "Example: $0 start"
    exit 1
}

start()
{
    count=`ps -ef |grep java|grep $SERVER|wc -l`
    if [ $count != 0 ]
	then
        echo "Maybe $SERVER is running, please check it..."
    else
        echo "The $SERVER is starting..."
        if [ "$AUGMENT2" == "-debug" ]
        then
        	$JAVA_COMMAND
        else
        	cd $WORK_HOME
        	sudo nohup $JAVA_COMMAND >> $WORK_HOME/console.log 2>&1 &
        fi
    fi
}

stop()
{
    PID=`ps -ef |grep java|grep $JAVA_SERVICE|awk '{print $2}'`
    if [ -z $PID ]
    then
        echo "Maybe $SERVER not running, please check it..."
    else
        echo -n "The $SERVER is stopping..."
        if [ "$AUGMENT2" == "-f" ]
        then
            echo "by force"
            kill -9 $PID
        else
            echo
            kill $PID
        fi
    fi
}

status()
{
    PID=`ps -ef |grep java|grep $JAVA_SERVICE|awk '{print $2}'`
    if [ -z $PID ]
    then
        echo -e "\033[31m $SERVER Not running \033[0m"
    else
        echo -e "\033[32m $SERVER Running [$PID] \033[0m"
    fi
}

restart()
{
    stop
    for i in {3..1}
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

# CentOS 8
# chkconfig --add scada-server	添加服务
# chkconfig --del scada-server	删除服务
# chkconfig scada-server on	开机启动
# chkconfig scada-server off	开机不启动
# service scada-server start	启动服务
# service scada-server stop	停止服务
# service scada-server restart	重启服务
# Debian 12

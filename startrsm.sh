#!/bin/bash

###################必要的配置 start###################
#应用标识
export appname=AP1
#监控地址
export monurl=10.240.35.50:9898
###################必要的配置 end#####################

###################采集应用服务器的配置 start############
#in容器监听地址
export in_url=http://127.0.0.1:8070/esb_monitor
#router容器监听地址
export router_url=http://127.0.0.1:8071/esb_monitor
#out容器监听地址
export out_url=http://127.0.0.1:8072/esb_monitor
#流水应用监听地址
export journal_url=http://127.0.0.1:8073/esb_monitor
#主流控监听地址
export flow_url=http://10.240.35.49:8074/esb_monitor
#备流控监听地址
export subFlow_url=http://10.240.35.49:8074/esb_monitor
#mom监听地址
export mom_url=127.0.0.1:7799
###################采集应用服务器的配置 end###############

###################采集数据库服务器的配置 start############
#数据用户名
export username="esbimon"
#数据库密码
export password="abcd1234"
#数据库服务器的ip
export ip="192.168.43.37"
#数据库端口
export port="1521"
#数据库实例名
export instance="esbimon"
###################采集数据库服务器的配置 end###############

#运行环境
export CLASSPATH=./lib/esb_rsm.jar:./lib/ESBRSM.jar:./lib/log4j-1.2.8.jar:./lib/message-server-1.0.jar:$CLASSPATH

#打印基础信息
echo "APPNAME="$appname
echo "MONURL="$monurl
echo "JAVA_HOME="$JAVA_HOME
echo "CLASSPATH="$CLASSPATH

#停止资源采集器
echo "stop esbrsm process"
sh stoprsm.sh

#运行采集器并挂起
echo "start esbrsm process"
nohup sh runesbmon.sh 2>&1 &

#运行清理进程并挂起
echo "start clear process"
nohup sh clear.sh 2>&1 &

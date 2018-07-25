ps -ef |grep "sh runesbmon.sh"|grep -v grep |awk '{print "kill -9 "$2}'|sh
ps -ef |grep "sh clear.sh"|grep -v grep |awk '{print "kill -9 "$2}'|sh
echo "资源采集器已停止!"

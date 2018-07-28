procnum=`ps -ef |grep "sh runesbmon.sh"|grep -v grep |wc -l`

if [ $procnum -gt 0 ]; then
	ps -ef |grep "sh runesbmon.sh"|grep -v grep |awk '{print "kill -9 "$2}'|sh
	ps -ef |grep "sh clear.sh"|grep -v grep |awk '{print "kill -9 "$2}'|sh
	echo "资源采集器关闭成功!"
else
	echo "资源采集器已关闭!"
fi
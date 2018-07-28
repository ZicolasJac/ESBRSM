#一分钟采集一次

while true
do
  echo "start collect:"`date +"%Y%m%d %H:%M:%S"`
  start_tm=`date +%s`
  #采集数据并发送到监控
  sh esbmon.sh
  stop_tm=`date +%s`
  #计算本次采集时间
  stime=`expr $stop_tm - $start_tm`
  #计算睡眠时间
  interval=`expr 60 - $stime`
  echo "finish collect:"`date +"%Y%m%d %H:%M:%S"`
  echo "start sleep,sleep time is "$interval"s"
  echo ""
  sleep $interval
done






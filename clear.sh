export mpath=`pwd`/monms
targetHour=23

echo "this clear target dir is"$mpath
while true
do
	hour=`date +%H`
	if [ "$hour" = "$targetHour" ]; then
	    echo "[`date`] - start clear monms data!"
	    cd $mpath
	    #$?--指上一条命令的执行状态
	    if [ $? -eq 0 ]; then
	    	#清理非当天的数据
	        ls |grep -v `date +%Y%m%d`|xargs rm -rf $1
	        echo "[`date`] - clear ok!"
	    else 
	        echo "[`date`] - clear fail!"
	    fi
	fi
	#每半小时循环一次
	sleep 1800s
done

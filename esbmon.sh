export spath=`pwd`/monsh
export tpath=`pwd`/tmp
export mpath=`pwd`/monms

#创建结果存放目录
ms_time=`date +%Y%m%d%H%M`
ms_date=`date +%Y%m%d`
mkdir $mpath/$ms_date  1>/dev/null 2>/dev/null
ms_file=$mpath/$ms_date/esb_ms_$ms_time.xml

aptype=`echo $appname |cut -b1-2`

#执行采集脚本
sh $spath/nmon.sh $tpath
sh $spath/file.sh $tpath
sh $spath/proc.sh $tpath
sh $spath/cpu.sh $tpath
sh $spath/net.sh $tpath
sh $spath/mem.sh $tpath
sh $spath/disk.sh $tpath
if [ "$aptype" = "AP" ]; then
  echo "appType is APP"
  sh $spath/mom.sh $tpath
  sh $spath/esbPool.sh $appname $tpath/pool_ms.tmp $in_url $router_url $out_url $journal_url $flow_url $subFlow_url
fi
if [ "$aptype" = "DB" ]; then
  echo "appType is DB"
  sh $spath/db.sh
fi

#输出结果信息到文件
echo '<esb_rsm servName="'$appname'" monTime="'$ms_time'">' > $ms_file
cat $tpath/cpu_ms.tmp >> $ms_file
cat $tpath/mem_ms.tmp >> $ms_file
cat $tpath/disk_ms.tmp >> $ms_file
cat $tpath/net_ms.tmp >> $ms_file
cat $tpath/file.tmp >> $ms_file
cat $tpath/proc.tmp >> $ms_file
if [ "$aptype" = "AP" ]; then
  cat $tpath/mom.tmp>>$ms_file
  cat $tpath/pool_ms.tmp>>$ms_file
fi
echo '</esb_rsm>' >> $ms_file

#发送数据到监控
sh $spath/send.sh $monurl $ms_file

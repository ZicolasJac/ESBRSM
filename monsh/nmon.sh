tpath=`pwd`
if [ $# -eq 1 ]
then
  tpath=$1
else
  tpath=`pwd`
fi

rm -f $tpath/mynmon*

./monsh/nmon -s 1 -c 2 -F $tpath/mynmon.tmp
sleep 10

while true
do
  flag=`ps -ef |grep "mynmon"|grep -v grep|wc -l`
  if [ $flag -eq 0 ]; then
    break
  else
    sleep 1
  fi
done

tfile=$tpath/mymon.tmp

cat $tpath/mynmon* |sort  >$tfile
grep ^CPU_ALL $tfile >$tpath/cpu.tmp
grep ^DISK $tfile >$tpath/disk.tmp
grep ^MEM $tfile >$tpath/mem.tmp
grep ^NET $tfile >$tpath/net.tmp


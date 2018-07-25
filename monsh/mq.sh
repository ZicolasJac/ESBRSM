tpath=`pwd`
if [ $# -eq 1 ]
then
tpath=$1
fi

doone()
{
flag=`dspmq |grep $qmname|grep Running|wc -l`

if [ $flag -eq 0 ]
then
echo "$qmname,-,0,0,0,1">>$tmpfile
return
fi

echo "display ql(*)"|runmqsc $qmname|grep QUEUE|grep -v AMQ|grep -v SYSTEM>$tmpfile1
awk '{print "display "$1" curdepth ipprocs opprocs"}' $tmpfile1>$tmpfile2
data=`cat $tmpfile2 |runmqsc $qmname|grep -v ":"|grep "("|sed 's/QUEUE/%/g'|sed 's/[(|)|]//g'`

echo $data| awk -F"%" -vqn=$qmname '{
i=2;
while(i<=NF)
{
print qn","$i",0"
i++
}
}'|sed 's/TYPEQLOCAL CURDEPTH/,/g'|sed 's/IPPROCS/,/g'|sed 's/ OPPROCS/,/g'|sed 's/ //g'>>$tmpfile
}

tmpfile1=$tpath/mq1.txt
tmpfile2=$tpath/mq2.txt
tmpfile=$tpath/mq.tmp

echo ">>>>MQ">$tmpfile
echo "qmn,qn,depth,inp,opp,flag">>$tmpfile
qmname=esb.consumer.qm
doone
qmname=esb.esb.qm
doone
qmname=esb.provider.qm
doone
qmname=esb.trans_logger.qm
doone

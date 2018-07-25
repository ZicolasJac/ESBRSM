tpath=`pwd`
if [ $# -eq 1 ]
then
	tpath=$1
fi

input=$tpath/cpu.tmp
output=$tpath/cpu_ms.tmp
echo "<cpu>" > $output

grep "CPU_ALL,T" $input|awk -F, '
BEGIN{
    user=0;
    sys=0;
    wait=0;
    idle=0;
    count=0;
    cpunum=0;
}
{
	user+=$3;
	sys+=$4;
	wait+=$5;
	idle+=$6;
	cpunum=$8
	count+=1;
}
END{
	user=user/count;
	sys=sys/count;
	wait=wait/count;
	idle=idle/count;
	print "\t<item user=\""user"\" sys=\""sys"\" wait=\""wait"\" idle=\""idle"\" cpunum=\""cpunum"\"></item>";
}'>>$output

echo "</cpu>" >> $output
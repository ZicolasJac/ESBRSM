tpath=`pwd`
if [ $# -eq 1 ]
then
	tpath=$1
fi

output=$tpath/mem_ms.tmp
echo "<mem>" > $output

mfree=`free -m |grep "Mem"|awk '{print $4}'`
mbuffers=`free -m |grep "Mem"|awk '{print $6}'`
mcached=`free -m |grep "Mem"|awk '{print $7}'`

rfmb=`expr $mfree + $mbuffers + $mcached`
vfmb=`free -m |grep "Swap"|awk '{print $4}'`
rtmb=`free -m |grep "Mem"|awk '{print $2}'`
vtmb=`free -m |grep "Swap"|awk '{print $2}'`
rfree=$(printf "%.2f" `echo "scale=4;$rfmb/$rtmb*100" |bc`)
vfree=$(printf "%.2f" `echo "scale=4;$vfmb/$vtmb*100" |bc`)
echo -e "\t<item rfree=\"$rfree\" vfree=\"$vfree\" rfmb=\"$rfmb\" vfmb=\"$vfmb\" rtmb=\"$rtmb\" vtmb=\"$vtmb\"></item>" >> $output
echo "</mem>" >> $output

tpath=`pwd`
if [ $# -eq 1 ]
then
tpath=$1
fi

filename=$tpath/file.tmp

echo "<file>" > $filename

df -mP |grep '^/'|grep '%'|awk '{
	print "\t<item filename=\""$6"\" totsize=\""$2"\" freesize=\""$4"\" usedpct=\""$5"\" iusedsize=\"0\" iusedpct=\"0\"></item>"
}' >> $filename

echo "</file>" >> $filename
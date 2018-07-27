tpath=`pwd`
if [ $# -eq 1 ]; then
	tpath=$1
fi

doonenetms(){
echo "
    awk -F',' '
        BEGIN{
          value=0;value1=0;type=\"\";str=\"\"
        }
	{
	  if(count!=0 && type!=\$1){
	    value=value/count;
	    value1=value1/count
	    str=str\",\"value\",\"value1
	    value=0;
	    count=0;
	  }
	  type=\$1;
	  value+=\$$index
	  value1+=\$$index1
	  count+=1;
	}
	END{
	  if(count!=0){
	    value=value/count;
	    value1=value1/count
	  }
          str=str\" $1=\\\"\"value\"\\\" $2=\\\"\"value1\"\\\"\";
	  print str
        }
	' $tmpf
"|sh
}

donenet(){
	index=`expr $netn + 3`
	index1=`expr $index + $netnum`
	ntmpf=$tpath/ntmp.tmp
	echo "echo $colvalue |awk -F',' '{print \$$index}'"|sh > $ntmpf
	netname=`cat $ntmpf|awk -F'-' '{print $1}'`

	grep -v "read" $input|grep ^NET,>$tmpf
	netrd="netname=\""$netname"\" "`doonenetms nrkps nwkps`

	grep -v "read" $input|grep ^NETERROR,>$tmpf
	netrd=$netrd`doonenetms nrerr nwerr`
	
	grep -v "read" $input|grep ^NETPACKET,>$tmpf
	netrd=$netrd`doonenetms nprs npws`
	
	grep -v "read" $input|grep ^NETSIZE,>$tmpf
	netrd=$netrd`doonenetms nrsize nwsize`
	
	echo -e "\t<item "$netrd"></item>"
}

input=$tpath/net.tmp
tmpf=$tpath/tmp.tmp
output=$tpath/net_ms.tmp

colvalue=`sed -n '1p' $input`
colnum=`sed -n '1p' $input|awk -F, '{print NF-2}'`
netnum=`expr $colnum / 2`

grep -v "read" $input > $tmpf

echo "<net>" > $output
netn=0
while true
do
	if [ $netn -lt $netnum ]; then
		donenet >> $output
		netn=`expr $netn + 1`
	else
		break;
	fi
done
echo "</net>" >> $output 

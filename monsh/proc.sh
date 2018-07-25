tpath=`pwd`
if [ $# -eq 1 ]; then
	tpath=$1
fi

printproc(){ 
	if [ "$procms" = "" ]; then
		ms="\t<item pname=\""$procnm"\" puser=\"-\" pid=\"-\" stime=\"-\" rtime=\"-\" cpu=\"0\" mem=\"0\" sz=\"0\" rss=\"0\" flag=\"1\"></item>"
	else
		ms="\t<item pname=\""$procnm"\""`echo $procms|cat |awk '{
			print " puser=\""$1"\" pid=\""$2"\" stime=\""$9"\" rtime=\""$10"\" cpu=\""$3"\" mem=\""$4"\" sz=\""$5"\" rss=\""$6"\" flag=\"0\"></item>"
		}'`
	fi
	echo -e $ms >> $tpath/proc.tmp
}

ps aux> $tpath/myproc.tmp 

export myproc=$tpath/myproc.tmp

echo "<proc>" > $tpath/proc.tmp

procnm=接入容器
procms=`grep "com.dc.esb.startup.standalone.ESBStartup IN OUT ROUTER" $myproc`
printproc

procnm=控制台
procms=`grep "com.dc.esb.startup.standalone.ESBStartup CONSOLE" $myproc`
printproc

procnm=接出容器
procms=`grep "com.dc.esb.startup.standalone.ESBStartup IN OUT ROUTER" $myproc`
printproc

procnm=流水服务
procms=`grep "com.dc.esb.startup.standalone.ESBStartup JOURNAL" $myproc`
printproc

procnm=流控服务
procms=`grep "com.dc.esb.startup.standalone.ESBStartup FLOW" $myproc`
printproc

procnm=核心路由
procms=`grep "com.dc.esb.startup.standalone.ESBStartup IN OUT ROUTER" $myproc`
printproc

echo "</proc>" >> $tpath/proc.tmp

echo "<pool>" > $2
$JAVA_HOME/bin/java com.dcfs.rsm.esbpool.CollectESBPool $1 $2 $3 $4 $5 $6 $7 $8
echo "</pool>" >> $2
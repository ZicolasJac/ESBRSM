#! /bin/bash

tpath=`pwd`
#检查传入参数个数
if [ $# -eq 1 ]; then
  tpath=$1
fi

#查询数据库结果暂存文件
queryTmpDir=db_query.tmp
#最终处理结果存放文件
finalRes=$tpath/db.tmp

fun_query(){
#连接数据库,标准输出到$queryTmpDir,从EOF之间获取输入
sqlplus -S $username/$password@$ip:$port/$instance >$queryTmpDir << EOF
    set heading on
    set headsep','
    set line 5000
    set wrap off
    set tab off
    set serveroutput off
    set verify off
    set pages 0
    set colsep'^'
    set termout off
    set trimout on
    set trimspool on
    set newp none
    set echo off
    set feedback off
    $1
    exit
EOF
}

echo "<db>" > $finalRes

echo -e "\t<tablespace>" >> $finalRes
#查询数据库表空间(MB)
fun_query "select b.tablespace_name,
                  b.bytes/1024/1024 as total_size,
                  (b.bytes-sum(nvl(a.bytes,0)))/1024/1024 used_size,
                  sum(nvl(a.bytes,0))/1024/1024 free_size,
                  trunc(sum(nvl(a.bytes,0))/(b.bytes),6) free_rate 
           from dba_free_space a, dba_data_files b 
           where a.file_id=b.file_id 
           group by b.tablespace_name,b.file_id,b.bytes;"
cat $queryTmpDir |gawk -F^ -v resPath=$finalRes '{
  gsub(/^ +| +$/,"",$1);
  gsub(/^ +| +$/,"",$2);
  gsub(/^ +| +$/,"",$3);
  gsub(/^ +| +$/,"",$4);
  gsub(/^ +| +$/,"",$5);
  print "\t\t<item name=\""$1"\" total_size=\""$2"\" used_size=\""$3"\" free_size=\""$4"\" free_rate=\""$5"\"/>" >> resPath;
}'
echo -e "\t</tablespace>" >> $finalRes

echo -e "\t<sql>" >> $finalRes
#查询执行过的sql语句的文本、执行次数、无效次数、查询成本、执行总耗时(ms)、平均执行耗时(ms)、初次载入时间、最后加载时间、最后执行时间
fun_query "select substr(trim(b.sql_text),0,150) as sql_text,
	          b.executions,
	          b.invalidations,
	          b.optimizer_cost,
              round(b.ELAPSED_TIME/1000, 4) as total_spend_time,
              round(b.ELAPSED_TIME/1000/(decode(b.executions，0，1，b.executions）), 4) as avg_spend_time,
              b.first_load_time,
	          b.last_load_time,
              to_char(b.last_active_time,'yyyy-mm-dd hh24:mi:ss.SS') as last_active_time
           from v\$sql b
           where b.parsing_schema_name='ESBIMON' and b.sql_fulltext not like '/*+%' and b.module is not null
           order by b.last_active_time desc;"
cat $queryTmpDir |gawk -F^ -v resPath=$finalRes '{
  gsub(/\n\r/," ",$1);
  gsub(/\n/," ",$1);
  gsub(/\r/," ",$1);
  gsub(/"/,"",$1);
  gsub(/^ +| +$/,"",$1);
  gsub(/^ +| +$/,"",$2);
  gsub(/^ +| +$/,"",$3);
  gsub(/^ +| +$/,"",$4);
  gsub(/^ +| +$/,"",$5);
  gsub(/^ +| +$/,"",$6);
  gsub(/^ +| +$/,"",$7);
  gsub(/^ +| +$/,"",$8);
  gsub(/^ +| +$/,"",$9);
  print "\t\t<item text=\""$1"\" exec_num=\""$2"\" fail_num=\""$3"\" opti_cost=\""$4"\" total_spend_time=\""$5"\" avg_spend_time=\""$6"\" first_load_time=\""$7"\" last_load_time=\""$8"\" last_active_time=\""$9"\"/>" >> resPath;
}'
echo -e "\t</sql>" >> $finalRes

echo -e "\t<connNum>" >> $finalRes
#查询数据库允许的最大连接数、当前的数据库连接数
fun_query "select m.value as total_num,n.active_num 
           from v\$parameter m,(select count(*) as active_num from v\$process) n 
           where m.name='processes';"
cat $queryTmpDir |gawk -F^ -v resPath=$finalRes '{
  gsub(/^ +| +$/,"",$1);
  gsub(/^ +| +$/,"",$2);
  print "\t\t<item total_num=\""$1"\" active_num=\""$2"\"/>" >> resPath;
}'
echo -e "\t</connNum>" >> $finalRes

echo -e "\t<lock>" >> $finalRes
#查询数据库锁,被锁对象类型、被锁对象名、被锁对象拥有者、锁模式、造成者用户名、造成者登录的机器名、造成锁的进程号、造成锁的会话号
fun_query "select o.object_type,
                  o.object_name,
                  o.owner,
                  l.locked_mode,
                  s.username,
                  s.machine,
                  s.sid,
                  s.serial#
           from v\$locked_object l, dba_objects o, v\$session s
           where l.object_id = o.object_id and l.session_id = s.sid;"
cat $queryTmpDir |gawk -F^ -v resPath=$finalRes '{
  gsub(/^ +| +$/,"",$1);
  gsub(/^ +| +$/,"",$2);
  gsub(/^ +| +$/,"",$3);
  gsub(/^ +| +$/,"",$4);
  gsub(/^ +| +$/,"",$5);
  gsub(/^ +| +$/,"",$6);
  gsub(/^ +| +$/,"",$7);
  gsub(/^ +| +$/,"",$8);
  print "\t\t<item object_type=\""$1"\" object_name=\""$2"\" owner=\""$3"\" locked_mode=\""$4"\" username=\""$5"\" machine=\""$6"\" sid=\""$7"\" serial=\""$8"\"/>" >> resPath;
}'
echo -e "\t</lock>" >> $finalRes

echo -e "\t<tableSize>" >> $finalRes
#查询当前数据库表占用的空间大小(MB)
fun_query "select segment_name, tablespace_name, bytes/1024/1024 as occur_size
           from user_segments
           where segment_type = 'TABLE'
           order by bytes desc;"
cat $queryTmpDir |gawk -F^ -v resPath=$finalRes '{
  gsub(/^ +| +$/,"",$1);
  gsub(/^ +| +$/,"",$2);
  gsub(/^ +| +$/,"",$3);
  print "\t\t<item segment_name=\""$1"\" tablespace_name=\""$2"\" occur_size=\""$3"\"/>" >> resPath;
}'
echo -e "\t</tableSize>" >> $finalRes

echo "</db>" >> $finalRes

echo off
cls

::设置编译环境
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%

::调用编译脚本
call ant -f build_esb_rsm.xml

pause

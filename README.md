工程简要说明:

	startrsm.sh--启动脚本
	stoprsm.sh--关闭脚本
	clear.sh--清理脚本
	runesbmon.sh--定时控制
	esbmon.sh--采集核心调度
	/monsh--所有采集脚本存放目录
	/src--部分需要JAVA语言实现的功能代码
	
	build_esb_rsm.xml--ant打包脚本
	/dest--部署包存放目录
	
	/patch--SmartESB和流水应用的适配补丁
	
	/monms采集结果存放目录
	/tmp临时文件存放目录
	
待完成内容:
	修改net.sh打印格式
	采集内存队列
	采集数据库信息
	
注:本版本依据上海银行ESB架构模式修改,使用前需进行本地化
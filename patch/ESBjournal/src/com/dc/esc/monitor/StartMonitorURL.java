package com.dc.esc.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dc.esc.ESCConfig;

public class StartMonitorURL  {

	private static Log log = LogFactory.getLog(StartMonitorURL.class);
	private static String moinitorport = ESCConfig.getConfig().getProperty("esb.monitorport");
	
	public static void start(){
		if(log.isInfoEnabled()){
			log.info("开始启动监控获取数据的url");
		}
		try{
			//流水容器采集入口
			HttpServer ss = HttpServer.getInstance();
			ESBMonitorServlet esbs = new ESBMonitorServlet();
			ss.addServletMapping("http://127.0.0.1:" +moinitorport+ "/esb_monitor",esbs);
			
		}catch(Exception e){
			System.out.println("启动监控URL失败");
			if(log.isErrorEnabled()){
				log.error("启动监控URL失败",e);
			}
		}
	}
	
	public static void stop(){
		HttpServer ss = HttpServer.getInstance();
		ss.stop();
	}
}
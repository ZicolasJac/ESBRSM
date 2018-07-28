package com.dc.esb.startup.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dc.esb.container.protocol.http.server.HttpServer;
import com.dcfs.impls.esb.ESBConfig;
import com.dcits.esb.monitor.ESBMonitorServlet;

/**
 * 监控需要需要ESB提供协议线程池参数、数据库连接池参数和ESB各应用的线程池参数
 * 在ESB系统启动一个url，供监控调用
 * @author chenzyn
 *
 */
public class StartMonitorURL  {

	private static Log log = LogFactory.getLog(StartMonitorURL.class);

	public static void start(){
		if(log.isInfoEnabled()){
			log.info("开始启动监控获取数据的监听");
		}
		String locationType = ESBConfig.getConfig().getProperty("com.dcfs.esb.client.location");
		String monitorPort = ESBConfig.getConfig().getProperty("esb.monitorport");
		if(log.isDebugEnabled()){
			log.debug("获取到的容器类型["+locationType+"],管理端口为[" + monitorPort + "]");
		}
		try{
			HttpServer.getInstance().addServletMapping("http://127.0.0.1:"+ monitorPort +"/esb_monitor",new ESBMonitorServlet());
		}catch(Exception e){
			if(log.isErrorEnabled()){
				log.error("启动监控URL失败",e);
			}
		}
	}
}

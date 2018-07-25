package com.dcits.esb.monitor;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dc.esb.container.adaptor.resource.ThreadPoolConfig;
import com.dcfs.impls.esb.ESBConfig;
import com.dcits.esb.monitor.DBPoolMonitor;
import com.dcits.esb.monitor.service.CollectPoolService;

public class ESBMonitorServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ESBMonitorServlet.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		
		if (log.isDebugEnabled()) {
			log.debug("采集器正在采集ESB线程池信息...");
		}
		
		String appID = request.getParameter("app");
		// 预留可以单独获取某容器中某线程池的入口
		String reqType = request.getParameter("type");
		String value = null;
		
		if (log.isDebugEnabled()) {
			log.debug("获取到的appid=["+appID+"]");
		}
		if(null == reqType){
			value = CollectPoolService.getAllPool(appID);
		} else {
			if("in_protocol_poolSize".equals(reqType)){
				// in容器接入协议线程池
				value = ThreadPoolConfig.getInstance().getProxysAttributes(appID);
			} else if("flow_token".equals(reqType)){
				// 令牌使用情况
				value = FlowControlMonitor.getFlowInfoForMonitor(appID);
			} else if("journal_db_poolSize".equals(reqType)){
				// 流水应用数据库连接池
				value = DBPoolMonitor.getDBPoolMonitorInfo(appID);
			} else if("in_thread_poolSize".equals(reqType)){
				// IN容器线程池
				value = ESBConfig.getPoolAttributes(appID);
			} else if("core_thread_poolSize".equals(reqType)){
				// router容器线程池
				value = ESBConfig.getPoolAttributes(appID);
			} else if("out_thread_poolSize".equals(reqType)){
				// out容器线程池
				value = ESBConfig.getPoolAttributes(appID);
			} else if("flow_thread_poolSize".equals(reqType)){
				// 流控容器线程池
				value = ESBConfig.getPoolAttributes(appID);
			} else if("journal_thread_poolSize".equals(reqType)){
				// 流水应用容器线程池
				value = ESBConfig.getPoolAttributes(appID);
			}
		}	
		if(log.isDebugEnabled()){
			log.debug("采集到的ESB信息=["+value+"]");
		}
		OutputStream out = response.getOutputStream(); 
		out.write(value.getBytes());
		out.flush();
	}
}

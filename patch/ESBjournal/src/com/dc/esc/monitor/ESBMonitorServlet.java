package com.dc.esc.monitor;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ESBMonitorServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ESBMonitorServlet.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		
		if (log.isDebugEnabled()) {
			log.debug("监控系统采集数据库连接池信息");
		}
		String appID = request.getParameter("app");
		String value = null;
		if (log.isDebugEnabled()) {
			log.debug("获取到的appid=[" + appID + "]");
		}
		value = DBPoolMonitor.getDBPoolMonitorInfo(appID);
		if(log.isDebugEnabled()){
			log.debug("采集到的数据库连接池信息=[" + value + "]");
		}
		OutputStream out = response.getOutputStream(); 
		out.write(value.getBytes());
		out.flush();
	}
}

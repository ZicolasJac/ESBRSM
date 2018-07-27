package com.dcits.esb.monitor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

/**
 * 数据库连接池监控
 *
 */
public class DBPoolMonitor {
	
	private static Log log = LogFactory.getLog(DBPoolMonitor.class);
	
	public static String getDBPoolMonitorInfo(String appID){
		String result = "";
		try {
			SnapshotIF snapshot = ProxoolFacade.getSnapshot("proxool_journal", true);
			StringBuffer buff = new StringBuffer();
			buff.append("\t<db>").append("\n\t\t");
			buff.append("<dbpool maxSize=\"").append(snapshot.getMaximumConnectionCount()).append("\"");
			buff.append(" useSize=\"").append(snapshot.getActiveConnectionCount()).append("\"");
			buff.append(" app=\"").append(appID).append("\"");
			buff.append(" appliaction=\"记录流水\"");
			buff.append("/>\n");
			buff.append("\t</db>").append("\n");
			result = buff.toString(); 
		} catch (ProxoolException e) {
			log.error("获取监控数据库连接池信息出现异常:"+e);
		}
		return result;
		
	}
}

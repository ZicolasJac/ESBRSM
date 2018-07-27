package com.dc.esc.monitor;


import java.util.Calendar;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;

import com.dc.esc.journallog.db.DBManager;

/**
 * 
 * 数据库连接池监控
 *
 */
public class DBPoolMonitor {
	
	// 当天最大连接数
	private static int maxnum_day = 0;
	// 操作日
	private static int opday = 0;
	
	public static String getDBPoolMonitorInfo(String appID){
		String result = "";
		int maxSize = 0;
		int useSize = 0;
		List<DataSource> dslist = DBManager.getInstance().getDsList();
		StringBuffer buff = new StringBuffer();
		for(DataSource ds:dslist){
			// 应用程序借用的连接数
			useSize = useSize + ds.getActive();
			// 该池允许的最大连接
			maxSize = maxSize + ds.getMaxActive();
		}
		
		/* 切换日期 */
		int nowday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		// 进入了下一天
		if(opday != nowday){
			maxnum_day = 0;
			opday = nowday;
		}else{
			maxnum_day = maxnum_day < useSize ? useSize : maxnum_day;
		}
		
		/* 拼接响应报文 */
		buff.append("\t<db>").append("\n\t");
		buff.append("<dbpool maxSize=\"").append(maxSize).append("\"");
		buff.append(" useSize=\"").append(useSize).append("\"");
		buff.append(" app=\"").append(appID).append("\"");
		buff.append(" appliaction=\"流水入库\"");
		buff.append(" maxnumDay=\"").append(maxnum_day).append("\"");
		buff.append(" />");
		buff.append("</db>").append("\n");
		result = buff.toString();
		
		return result;
	}
}

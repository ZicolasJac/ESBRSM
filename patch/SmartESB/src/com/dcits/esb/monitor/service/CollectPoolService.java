package com.dcits.esb.monitor.service;

import com.dc.esb.container.adaptor.resource.ThreadPoolConfig;
import com.dcfs.impls.esb.ESBConfig;
import com.dcits.esb.monitor.DBPoolMonitor;
import com.dcits.esb.monitor.FlowControlMonitor;
import com.dcits.esb.monitor.MemQueueMonitor;

public class CollectPoolService {

	/**
	 * 获取所有线程池信息
	 * @param appID
	 * @return
	 */
	public static String getAllPool(String appID) {
		StringBuffer sb = new StringBuffer();
		// 获取容器类型
		String locationType = ESBConfig.getConfig().getProperty("com.dcfs.esb.client.location");
		if ("journalSrv".equals(locationType)) {
			// 获取流水模块的数据库连接池信息
			sb.append(getJournalDBPoolSize(appID));
		} else if (locationType.contains("flow")) {
			// 获取令牌信息
			sb.append(getFlowToken(appID));
		} else if(locationType.contains("local_in")){
			// 获取内存队列信息
			sb.append(getMemQueueInfo(appID));
		}
		
		// 获取线程池信息
		sb.append(getThreadPoolSize(appID));
		
		return sb.toString();
	}

	public static String getInProtocolPoolSize(String appID) {
		return ThreadPoolConfig.getInstance().getProxysAttributes(appID);
	}

	public static String getFlowToken(String appID) {
		return FlowControlMonitor.getFlowInfoForMonitor(appID);
	}

	public static String getJournalDBPoolSize(String appID) {
		return DBPoolMonitor.getDBPoolMonitorInfo(appID);
	}

	public static String getThreadPoolSize(String appID) {
		return ESBConfig.getPoolAttributes(appID);
	}
	
	public static String getMemQueueInfo(String appID) {
		return MemQueueMonitor.getMemQueueInfoForMonitor(appID);
	}
}

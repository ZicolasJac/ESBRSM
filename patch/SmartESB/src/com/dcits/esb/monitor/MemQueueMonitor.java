package com.dcits.esb.monitor;

import com.dc.esb.container.router.noroutermode.CircleLinked;
import com.dc.esb.container.router.noroutermode.InnerMessage;
import com.dc.esb.container.router.noroutermode.SharedQueueManager;
import com.dc.esb.container.router.noroutermode.console.BlockingQueueAgent;

/**
 * 监控获取内存队列信息
 * @author ZhangJinchuang
 *
 */
public class MemQueueMonitor {

	private static boolean shortCircurt = false;
	
	static {
		try {
			shortCircurt = "true".equals(System.getProperty("com.dc.inout.shortcircurt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getMemQueueInfoForMonitor(String appID) {
		StringBuilder res = new StringBuilder();
		res.append("\t<memq>").append("\n");
		if(shortCircurt){
			CircleLinked<BlockingQueueAgent<InnerMessage>> reqCircleLinked = SharedQueueManager.getInstance().getReqCircleLinked();
			assembData(res, appID, reqCircleLinked);
			CircleLinked<BlockingQueueAgent<InnerMessage>> respCircleLinked = SharedQueueManager.getInstance().getRespCircleLinked();
			assembData(res, appID, respCircleLinked);
		}else{
			CircleLinked<BlockingQueueAgent<InnerMessage>> mailBoxCircleLinked = SharedQueueManager.getInstance().getMailBoxCircleLinked();
			assembData(res, appID, mailBoxCircleLinked);
		}
		res.append("\t</memq>").append("\n");
		return res.toString();
	}
	
	/**
	 * 组装报文
	 * @param res
	 * @param sourceData
	 */
	private static void assembData(StringBuilder res, String appID, CircleLinked<BlockingQueueAgent<InnerMessage>> sourceData){
		for(int i=0; i<sourceData.getSize(); i++){
			res.append("\t\t").append("<item");
			BlockingQueueAgent<InnerMessage> blockQueue = sourceData.next();
			res.append(" app=\"").append(appID).append("\"");
			res.append(" qn=\"").append(blockQueue.getID()).append("\"");
			res.append(" max_depth=\"").append(blockQueue.getMaxCount()).append("\"");
			res.append(" depth=\"").append(blockQueue.getQueue().size()).append("\"");
			res.append("></item>").append("\n");
		}
	}
}

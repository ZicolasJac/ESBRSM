package com.dcits.esb.monitor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dc.esb.flowcontrol.impl.TokenServerContainerFactory;
import com.dc.esb.flowcontrol.interfaces.ITokenServerContainer;
import com.dc.esb.flowcontrol.interfaces.ITokenServerList;
import com.dcfs.impls.esb.ESBConfig;

@SuppressWarnings("unchecked")
public class FlowControlMonitor {

	private static Log log = LogFactory.getLog(FlowControlMonitor.class);
	// 操作日
	private static int opday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	// 每个令牌池的当天峰值的集合
	private static Map<String, Integer> dayMaxTokenNums = new HashMap<String, Integer>();

	public static String getFlowInfoForMonitor(String appID) {
		ITokenServerContainer container = TokenServerContainerFactory.getInstance().getTokenServerContainer();
		Map<String, ITokenServerList> tokenMap = container.getTokenServerMap();
		StringBuffer tokens = new StringBuffer();
		tokens.append("<flow app=\"").append(appID).append("\">").append("\n");
		for (String key : tokenMap.keySet()) {
			String[] keys = key.split("&");
			String channelName = keys[0];
			String serviceName = keys[1];
			String systemName = keys[2];
			String type = "默认";
			boolean has_channel = channelName!=null && !"".equals(channelName) && !"ALL".equalsIgnoreCase(channelName);
			boolean has_service = serviceName!=null && !"".equals(serviceName) && !"ALL".equalsIgnoreCase(serviceName);
			boolean has_system = systemName!=null && !"".equals(systemName) && !"ALL".equalsIgnoreCase(systemName);
			if (has_channel && has_service && has_system) {
				type = "渠道+服务+系统";
			} else if (has_channel && has_service) {
				type = "渠道+服务";
			} else if (has_channel && has_system) {
				type = "渠道+系统";
			} else if (has_service && has_system) {
				type = "服务+系统";
			} else if (has_channel) {
				type = "渠道";
			} else if (has_service) {
				type = "服务";
			} else if (has_system) {
				type = "系统";
			}
			ITokenServerList tokenList = tokenMap.get(key);
			int count = tokenList.getCount();
			int used = tokenList.getCurrentlyUsedSiz();
			int dayMaxTokenNum = getDayMaxnumOfOneTokenPool(key, used);
			tokens.append("\t");
			tokens.append("<token type=\"").append(type).append("\"");
			tokens.append(" app=\"").append(ESBConfig.getConfig().getProperty("com.dcfs.esb.client.location")).append("\"");
			tokens.append(" channelName=\"").append(channelName).append("\"");
			tokens.append(" serviceName=\"").append(serviceName).append("\"");
			tokens.append(" systemName=\"").append(systemName).append("\"");
			tokens.append(" htotal=\"").append(dayMaxTokenNum).append("\"");
			tokens.append(" total=\"").append(count).append("\"");
			tokens.append(" used=\"").append(used).append("\"").append("/>");
			tokens.append("\n");
		}
		tokens.append("</flow>").append("\n");
		if (log.isDebugEnabled()) {
			log.debug("流控信息：" + tokens.toString());
		}
		return tokens.toString();
	}
	
	/**
	 * 获取某个令牌池的当天令牌使用数峰值
	 * @param tokenPoolName
	 * @return
	 */
	private static Integer getDayMaxnumOfOneTokenPool(String tokenPoolName, int usedNum){
		// 当天最大已用令牌数
		int currMaxNum = 0;
		int nowday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		// 判断是否进入了下一天
		if (opday != nowday) {
			// 若进入了下一天，重置当天最大已用令牌数与操作日
			currMaxNum = 0;
			opday = nowday;
		} else {
			if(null != dayMaxTokenNums.get(tokenPoolName)){
				int lastMaxNum = dayMaxTokenNums.get(tokenPoolName);
				currMaxNum = lastMaxNum < usedNum ? usedNum : lastMaxNum;
			}else{
				currMaxNum = usedNum;
			}
		}
		dayMaxTokenNums.put(tokenPoolName, currMaxNum);
		return currMaxNum;
	}
}

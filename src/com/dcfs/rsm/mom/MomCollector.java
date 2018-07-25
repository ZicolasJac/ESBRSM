package com.dcfs.rsm.mom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.dcfs.rsm.mom.messageserver.RMIClient;

public class MomCollector {
	private static String tmpFilePath = "/home/esb/esb_rsm/tmp";
	private static String tmpFileName = "/mom.tmp";
	private static String serverHost = "127.0.0.1";
	private static int serverPort = 7799;

	public static void main(String[] paramArrayOfString) {
		if (paramArrayOfString.length < 1) {
			System.err.println("missing params,right params is:port filename");
			return;
		}
		
		tmpFilePath = paramArrayOfString[0].trim();
		if (paramArrayOfString.length > 1) {
			String[] arrayOfString = paramArrayOfString[1].split(":");
			serverHost = arrayOfString[0] == null ? serverHost : arrayOfString[0];
			serverPort = arrayOfString[1] == null ? serverPort : Integer.parseInt(arrayOfString[1]);
		}
		try {
			new MomCollector().exe();
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void exe() throws MalformedObjectNameException, NullPointerException, IOException,
			InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
		RMIClient localRMIClient = RMIClient.getInstance(serverHost, serverPort);
		LinkedList<?> localLinkedList = localRMIClient.getMBeanNameList();
		StringBuffer localStringBuffer = new StringBuffer();
		localStringBuffer.append("<mom>").append("\n");
		for (int i = 0; i < localLinkedList.size(); i++) {
			ObjectName localObjectName = (ObjectName) localLinkedList.get(i);
			AttributeList localAttributeList = localRMIClient.getAttributes(localObjectName);
			String str1 = null;
			int j = -1;
			long l1 = -1L;
			for (int k = 0; k < localAttributeList.size(); k++) {
				Attribute localAttribute = (Attribute) localAttributeList.get(k);
				String str2 = localAttribute.getName();
				if ("QueueName".equals(str2)) {
					str1 = (String) localAttribute.getValue();
				} else if ("QueueDepeth".equals(str2)) {
					l1 = ((Long) localAttribute.getValue()).longValue();
				} else {
					if (!"QueuePort".equals(str2))
						continue;
					j = ((Integer) localAttribute.getValue()).intValue();
				}
			}
			if (null == str1)
				continue;
			long l2 = localRMIClient.getMessageCount(str1);
			long l3 = localRMIClient.getQueueReceiverCount(str1).longValue();
			localStringBuffer.append("\t");
			localStringBuffer.append("<item qmn=\"").append(j).append("\"");
			localStringBuffer.append(" qn=\"").append(str1).append("\"");
			localStringBuffer.append(" depth=\"").append(l1).append("\"");
			localStringBuffer.append(" inp=\"").append(l2).append("\"");
			localStringBuffer.append(" opp=\"").append(l3).append("\"");
			localStringBuffer.append(" flag=\"").append(0).append("\"></item>");
			localStringBuffer.append("\n");
		}
		localStringBuffer.append("</mom>").append("\n");
		writeToFile(localStringBuffer.toString());
	}

	private void writeToFile(String paramString) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(tmpFilePath + tmpFileName));
			fos.write(paramString.getBytes("UTF-8"));
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
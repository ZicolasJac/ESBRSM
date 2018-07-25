package com.dcfs.rsm.mom.messageserver;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class RMIClient {
	private static RMIClient rmiClient = null;
	private JMXConnector client;
	private MBeanServerConnection connection;
	private String domainName = "com.dc.messageserver";
	private String profix;
	private ObjectName queueManagerObjName;
	private ObjectName topicManagerObjName;
	private ObjectName authenticateObjName;
	private int rmiServerPort;
	private String serverConnectorPath = "/jmxrmi";
//	private String serverHost = "localhost";
//	private int serverPort = 1099;

	public static synchronized RMIClient getInstance(String serverHost, int serverPort)
			throws IOException, MalformedObjectNameException, NullPointerException {
		if (rmiClient == null)
			rmiClient = new RMIClient(serverHost, serverPort);
		return rmiClient;
	}

	public static RMIClient getRMIClient(String serverHost, int serverPort)
			throws IOException, MalformedObjectNameException, NullPointerException {
		return new RMIClient(serverHost, serverPort);
	}

	private RMIClient(String serverHost, int serverPort)
			throws IOException, MalformedObjectNameException, NullPointerException {
//		if ((serverHost != null) && (!serverHost.equalsIgnoreCase(""))) {
//			this.serverHost = serverHost;
//		}
//		if ((serverPort > 0) && (serverPort < 65536)) {
//			this.serverPort = serverPort;
//		}
		this.profix = (this.domainName + ":name=");
		this.queueManagerObjName = new ObjectName(this.profix + "queueManager");
		this.topicManagerObjName = new ObjectName(this.profix + "topicManager");
		this.authenticateObjName = new ObjectName(this.profix + "authenticate");
		String rmiServer = "";
		if (this.rmiServerPort != 0) {
			rmiServer = "localhost:" + this.rmiServerPort;
		}
		String serviceURL = "service:jmx:rmi://" + rmiServer + "/jndi/rmi://" + serverHost + ":" + serverPort
				+ this.serverConnectorPath;

		JMXServiceURL url = new JMXServiceURL(serviceURL);
		this.client = JMXConnectorFactory.connect(url);
		this.connection = this.client.getMBeanServerConnection();
	}

	public boolean addQueue(String queueName, long queueDepeth, long maxMessageLength, int transferThreadPoolSize,
			boolean isPersistent, String queueProtocal, String queueHost, int queuePort, String connectorName,
			String persistentName, long timeOut, String timeoutProcessName) throws InstanceNotFoundException,
			MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
		Object[] params = { queueName, Long.valueOf(queueDepeth), Long.valueOf(maxMessageLength),
				Integer.valueOf(transferThreadPoolSize), Boolean.valueOf(isPersistent), queueProtocal, queueHost,
				Integer.valueOf(queuePort), connectorName, persistentName, Long.valueOf(timeOut), timeoutProcessName };

		String[] signature = { "java.lang.String", "long", "long", "int", "boolean", "java.lang.String",
				"java.lang.String", "int", "java.lang.String", "java.lang.String", "long", "java.lang.String" };

		Boolean result = (Boolean) this.connection.invoke(this.queueManagerObjName, "addQueue", params, signature);
		if (result.booleanValue())
			System.out.println("add queue " + queueName + " successfully...");
		else
			System.out.println("add queue " + queueName + " failed...");
		return result.booleanValue();
	}

	public boolean deleteQueue(String queueName)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		Object[] params = { queueName };
		String[] signature = { "java.lang.String" };
		Boolean result = (Boolean) this.connection.invoke(this.queueManagerObjName, "deleteQueue", params, signature);
		if (result.booleanValue())
			System.out.println("delete queue " + queueName + " successfully...");
		else
			System.out.println("delete queue " + queueName + " failed...");
		return result.booleanValue();
	}

	public boolean deleteMessage(String queueName, int[] indexs)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		boolean result = false;
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		Object[] params = { indexs };
		String[] signature = { indexs.getClass().getName() };

		result = ((Boolean) this.connection.invoke(objectName, "deleteMessage", params, signature)).booleanValue();
		return result;
	}

	public AttributeList getAttributes(ObjectName objectName) throws MalformedObjectNameException, NullPointerException,
			InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		AttributeList list = null;
		MBeanInfo beanInfo = this.connection.getMBeanInfo(objectName);
		MBeanAttributeInfo[] attributeInfo = beanInfo.getAttributes();
		int length = attributeInfo.length;
		String[] attributes = new String[length];
		for (int i = 0; i < length; i++) {
			attributes[i] = attributeInfo[i].getName();
		}
		list = this.connection.getAttributes(objectName, attributes);
		return list;
	}

	public LinkedList<ObjectName> getMBeanNameList() throws IOException {
		LinkedList<ObjectName> nameList = new LinkedList<ObjectName>();
		Set<?> nameSet = this.connection.queryNames(null, null);
		for (Iterator<?> iterator = nameSet.iterator(); iterator.hasNext();) {
			ObjectName object = (ObjectName) iterator.next();
			nameList.addLast(object);
		}
		nameSet.clear();

		Collections.sort(nameList, new ComparatorObjectName());
		return nameList;
	}

	public long getMessageCount(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		long count = 0L;
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		count = ((Long) this.connection.invoke(objectName, "reloadMessageCount", null, null)).longValue();
		return count;
	}

	public boolean startBroker() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException,
			MalformedObjectNameException, NullPointerException {
		System.out.println("begin to start broker...");
		ObjectName brokerService = new ObjectName(this.profix + "brokerService");
		Boolean result = (Boolean) this.connection.invoke(brokerService, "start", null, null);
		if (result.booleanValue())
			System.out.println("start broker successfully...");
		else
			System.out.println("start broker failed...");
		return result.booleanValue();
	}

	public Map<?, ?> getPersistences() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException,
			MBeanException, ReflectionException, IOException {
		Map<?, ?> persistenceMap = null;
		ObjectName persistence = new ObjectName(this.profix + "persistence");
		persistenceMap = (Map<?, ?>) this.connection.invoke(persistence, "queryBeans", null, null);
		return persistenceMap;
	}

	public boolean stopBroker() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException,
			MBeanException, ReflectionException, IOException {
			System.out.println("begin to stop broker...");
		ObjectName brokerService = new ObjectName(this.profix + "brokerService");
		Boolean result = (Boolean) this.connection.invoke(brokerService, "stop", null, null);
		if (result.booleanValue())
			System.out.println("stop broker successfully...");
		else
			System.out.println("stop broker failed...");
		return result.booleanValue();
	}

	public Long getQueueReceiverCount(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		Long count = (Long) this.connection.invoke(objectName, "reloadQueueReceiverCount", null, null);

		return count;
	}

	public String[] getQueueMessageList(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		String[] messageArray = (String[]) (String[]) this.connection.invoke(objectName, "reloadQueueMessageList", null,
				null);

		return messageArray;
	}

	public Map<String, Integer> getReceiverCountWithSelector(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		@SuppressWarnings("unchecked")
		Map<String, Integer> receiverCounts = (Map<String, Integer>) this.connection.invoke(objectName, "reloadReceiverCountWithSelector", null, null);

		return receiverCounts;
	}

	@SuppressWarnings("unchecked")
	public Set<String> getAllTimeOutProcessors()
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		Set<String> processors = null;
		ObjectName processorsObj = new ObjectName(this.profix + "timeOutProcessors");
		processors = (Set<String>) this.connection.invoke(processorsObj, "loadAllTimeOutProcessors", null, null);

		return processors;
	}

	public boolean updateQueueTimeOut(String queueName, long timeOut)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		this.connection.getDomains();
		boolean result = false;
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		Object[] params = { Long.valueOf(timeOut) };
		String[] signature = { "long" };
		result = ((Boolean) this.connection.invoke(objectName, "updateTimeOut", params, signature)).booleanValue();
		return result;
	}

	public long reloadTimeOut(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		long result = 0L;
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		result = ((Long) this.connection.invoke(objectName, "reloadTimeOut", null, null)).longValue();
		return result;
	}

	public String reloadTProcessorName(String queueName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		String result = "";
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		result = (String) this.connection.invoke(objectName, "reloadTProcessorName", null, null);
		return result;
	}

	public boolean updateTProcessorName(String queueName, String processorName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		boolean result = true;
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(queueName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + queueName + " in jmx agent");
		}
		Object[] params = { processorName };
		String[] signature = { "java.lang.String" };
		result = ((Boolean) this.connection.invoke(objectName, "updateTProcessorName", params, signature))
				.booleanValue();
		return result;
	}

	public static void main(String[] args) throws Exception {
		RMIClient client = new RMIClient("127.0.0.1", 7799);
		client.updateQueueTimeOut("inbox", 1000L);
	}

	static void test(int[] indexs) {
		for (int i = 0; i < indexs.length; i++)
			System.out.println(i);
	}

	public Map<?, ?> getSubscriberCount(String topicName)
			throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException,
			NullPointerException, ReflectionException, MBeanException {
		ObjectName objectName = null;
		LinkedList<?> list = getMBeanNameList();
		for (int i = 0; i < list.size(); i++) {
			ObjectName temp = (ObjectName) list.get(i);
			String keyProperty = temp.getKeyProperty("name");
			if ((keyProperty != null) && (keyProperty.endsWith(topicName))) {
				objectName = temp;
				break;
			}
		}
		if (objectName == null) {
			System.err.println("There is no mbean with name " + topicName + " in jmx agent");
		}
		Map<?, ?> count = (Map<?, ?>) this.connection.invoke(objectName, "reloadTopicSubscriberCount", null, null);
		return count;
	}

	public boolean addTopic(String topicName, String connector, long maxdepth, long maxlength, int transferThreadPool,
			int durableQueueDepth, long readMaxIndex, String topicProtocal, String topicHost, int topicPort)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException,
			MalformedObjectNameException, NullPointerException {
		System.out.println("begin to add queue " + topicName);
		Object[] params = { topicName, connector, Long.valueOf(maxdepth), Long.valueOf(maxlength),
				Integer.valueOf(transferThreadPool), Integer.valueOf(durableQueueDepth), Long.valueOf(readMaxIndex),
				topicProtocal, topicHost, Integer.valueOf(topicPort) };

		String[] signature = { "java.lang.String", "java.lang.String", "long", "long", "int", "int", "long",
				"java.lang.String", "java.lang.String", "int" };

		Boolean result = (Boolean) this.connection.invoke(this.topicManagerObjName, "addTopic", params, signature);
		if (result.booleanValue())
			System.out.println("add topic " + topicName + " successfully...");
		else
			System.out.println("add topic " + topicName + " failed...");
		return result.booleanValue();
	}

	public boolean deleteTopic(String topicName)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		System.out.println("begin to delete topic " + topicName);
		Object[] params = { topicName };
		String[] signature = { "java.lang.String" };
		Boolean result = (Boolean) this.connection.invoke(this.topicManagerObjName, "deleteTopic", params, signature);
		if (result.booleanValue())
			System.out.println("delete topic " + topicName + " successfully...");
		else
			System.out.println("delete topic " + topicName + " failed...");
		return result.booleanValue();
	}

	public String[] getTopicMessageList(String topicName) {
		return null;
	}

	public void close() throws IOException {
		if (this.client != null)
			this.client.close();
		this.client = null;
		this.connection = null;
	}

	public boolean checking(String userName, String password)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		System.out.println("begin to checking user info ");
		Object[] params = { userName, password };
		String[] signature = { "java.lang.String", "java.lang.String" };
		Boolean result = (Boolean) this.connection.invoke(this.authenticateObjName, "checking", params, signature);
		return result.booleanValue();
	}

	public boolean resetPassword(String userName, String oldPW, String newPW)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		System.out.println("begin to reset password ");
		Object[] params = { userName, oldPW, newPW };
		String[] signature = { "java.lang.String", "java.lang.String", "java.lang.String" };
		Boolean result = (Boolean) this.connection.invoke(this.authenticateObjName, "resetPassword", params, signature);
		return result.booleanValue();
	}

	class ComparatorObjectName implements Comparator<Object> {
		ComparatorObjectName() {
		}

		public int compare(Object object1, Object object2) {
			ObjectName name1 = (ObjectName) object1;
			ObjectName name2 = (ObjectName) object2;
			String queue1 = getQueueName(name1);
			String queue2 = getQueueName(name2);
			return queue1.compareTo(queue2);
		}

		private String getQueueName(ObjectName name1) {
			String MBName = "";
			Hashtable<?, ?> map1 = name1.getKeyPropertyList();
			if (map1.containsKey("name")) {
				String name = (String) name1.getKeyPropertyList().get("name");
				if (name.startsWith("queue_")) {
					MBName = name.substring(name.indexOf("queue_"));
				}
			}
			return MBName;
		}
	}
}
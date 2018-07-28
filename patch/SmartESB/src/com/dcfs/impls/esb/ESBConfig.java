package com.dcfs.impls.esb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.dcfs.interfaces.esb.IESBConfig;

public class ESBConfig implements IESBConfig {
	private static Log log = LogFactory.getLog(ESBConfig.class);
	private static ESBConfig esbCOnfig;
	private HashMap<String, String> props = new HashMap<String, String>();
	private PropertiesConfiguration propConfiguration = new  PropertiesConfiguration();
	
	private static HashMap<String,Executor> threadPools = new HashMap<String, Executor>();

	private static String appRoot;
	private static String instroot = null;

	public static void setRoot(String path) {
		appRoot = path;
		if (!isAbsolutePath(path)) {
			appRoot = new File(path).getAbsolutePath();
		}
		// 同一个jvm可以变换不同的容器加载目录。
		instroot = appRoot;
	}

	private ESBConfig() throws IOException, SAXException, ParserConfigurationException {
		init();
	}

	private void init() {
		Properties properties = new Properties();
		if (instroot == null) {
			instroot = System.getProperty("com.dc.install_path");
		}
		if (!isAbsolutePath(instroot)) {
			instroot = new File(instroot).getAbsolutePath();
		}
		FileInputStream fin = null;
		FileInputStream fin1 = null;
		if (instroot == null) {
			instroot = appRoot;
			if (log.isDebugEnabled()) {
				log.debug("Undefine  the parameter [-Dcom.bis.install_path],using specifid:"
						+ appRoot);
			}
		}
		try {
			fin = new FileInputStream(new File(instroot + File.separator + "conf", "mxsd_process.properties"));
			properties.load(fin);
			fin1 = new FileInputStream(new File(instroot + File.separator + "conf", "mxsd_process.properties"));
			propConfiguration.load(fin1);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Load system config error.", e);
			}
		} finally {
			if(fin != null)
				try {
					fin.close();
				} catch (Exception e) {
				}
		}
		properties.setProperty(INSTALL_ROOT, instroot);
		Enumeration<?> en = properties.keys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			props.put(key, properties.getProperty(key));
		}
	}

	/**
	 * the configurations can be modified by the Monitor, then the
	 * configurations should be reload.
	 * 
	 */
	public void reload() {
		esbCOnfig._reload();
	}

	private void _reload() {
		if (props == null) {
			props = new HashMap<String, String>();
			propConfiguration = new PropertiesConfiguration();
		} else {
			props.clear();
			propConfiguration.clear();
			init();
		}

	}

	/**
	 * 
	 * you can specify an executor yourself in the configuration file
	 * mxsd_process.properties as follows:
	 * <p>
	 * thread_pool_impl=com.dcfs.demo.MyExecutor
	 * <p>
	 * which implements the interface java.util.concurrent.Executor. you should
	 * initialize all your executor properties(e.g. pool size 銆?? policy) with
	 * the default constructor. <BR>
	 * Otherwise,an default executor will be created,and which size is 20.
	 * Also,you can specify the size by property "thread_pool_size"(Attention
	 * please : Default Executor Only);
	 * 
	 * @see java.util.concurrent.Executor;
	 * @return
	 */
	public static final Executor createThreadPool(String poolName) {
		Executor pool =null;
		if (getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL) != null
				&& !"".equalsIgnoreCase(getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL))) {
			String clazzExecutor = getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL);
			try {
				if (log.isInfoEnabled()) {
					log.info("specified Excutor.[" + clazzExecutor + "]");
				}
				Class.forName(clazzExecutor);
				pool=(Executor) Class.forName(clazzExecutor).newInstance();
				threadPools.put(poolName, pool);
				return pool;
			} catch (Throwable th) {
				if (log.isWarnEnabled()) {
					log.warn("Unable to initialize the specified Excutor.[" + clazzExecutor + "]");
				}
			}
		}
		pool=getDefaultExecutor(poolName);
		threadPools.put(poolName, pool);
		return pool;

	}
	
	public static final Executor createThreadPool() {
        if (getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL) != null
                && !"".equalsIgnoreCase(getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL))) {
            String clazzExecutor = getConfig().getProperty(IESBConfig.THREAD_POOL_IMPL);
            try {
                if (log.isInfoEnabled()) {
                    log.info("specified Excutor.[" + clazzExecutor + "]");
                }
                Class.forName(clazzExecutor);
                return (Executor) Class.forName(clazzExecutor).newInstance();
            } catch (Throwable th) {
                if (log.isWarnEnabled()) {
                    log.warn("Unable to initialize the specified Excutor.[" + clazzExecutor + "]");
                }
            }
        }
        return getDefaultExecutor();

    }

	private static Executor getDefaultExecutor(final String poolName) {
		if (getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE) != null
				&& !"".equalsIgnoreCase(getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE))) {
			try {
				if (log.isInfoEnabled()) {
					log.info("Executor size : "
							+ getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE));
				}
				return Executors.newFixedThreadPool(Integer.parseInt((String) getConfig()
						.getProperty(IESBConfig.THREAD_POOL_SIZE)), new ThreadFactory(){
			        ThreadGroup tg = new ThreadGroup(poolName);
			        int count = 0;
					public Thread newThread(Runnable r)
					{
						return new Thread(tg,r, poolName + count++);
					}
					
				});
			} catch (Throwable th) {
				if (log.isWarnEnabled()) {
					log.warn("Create default executor error:" + th);
				}
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Executor size : 20 [default]");
		}
		return Executors.newFixedThreadPool(20);
	}
	
    private static Executor getDefaultExecutor() {
        if (getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE) != null
                && !"".equalsIgnoreCase(getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE))) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("Executor size : "
                            + getConfig().getProperty(IESBConfig.THREAD_POOL_SIZE));
                }
                return Executors.newFixedThreadPool(Integer.parseInt((String) getConfig()
                        .getProperty(IESBConfig.THREAD_POOL_SIZE)));
            } catch (Throwable th) {
                if (log.isWarnEnabled()) {
                    log.warn("Create default executor error:" + th);
                }
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Executor size : 20 [default]");
        }
        return Executors.newFixedThreadPool(20);
    }

	public static final IESBConfig getConfig() {
		if (esbCOnfig == null)
			synchronized (ESBConfig.class) {
				if (esbCOnfig == null)
					try {
						esbCOnfig = new ESBConfig();
					} catch (Exception ex) {
						if (log.isErrorEnabled()) {
							log.error("Load ESB config error.", ex);
						}
					}
			}
		return esbCOnfig;
	}

	public String getProperty(String name) {
		return props.get(name);
	}

	public String getInstallRoot() {
		return props.get(INSTALL_ROOT);
	}

	private static boolean isAbsolutePath(String url) {
		if (url == null || url.length() < 1) {
			return true;
		}
		if (url.startsWith(".")) {
			return false;
		}
		return true;

	}

	public boolean isEncrypt() {
		return getBooleanProperty(IS_ENCRYPT, false);
	}

	private boolean getBooleanProperty(String name, boolean defaultValue) {
		String prop = getProperty(name);
		boolean bRet = defaultValue;
		if (prop != null) {
			try {
				bRet = new Boolean(prop).booleanValue();
			} catch (Exception e) {
			}
		}
		return bRet;
	}

	public int getMaxSessions() {
		String sessionSize = props.get(MAX_SESSIONS);
		if (sessionSize == null || "".equals(sessionSize.trim())) {
			return 0;
		} else {
			return Integer.parseInt(sessionSize);
		}
	}

	public int getSleepTime() {
		String sleepTime = props.get(SLEEP_TIME);
		if (sleepTime == null || "".equals(sleepTime.trim())) {
			return 0;
		} else {
			return Integer.parseInt(sleepTime);
		}
	}

	public long getFlowControlTimeOut() {
		String flowcontroltimeout = props.get(FLOWCONTROL_TIMEOUT);
		if (flowcontroltimeout == null || "".equals(flowcontroltimeout.trim())) {
			return 20000;
		} else {
			return Long.parseLong(flowcontroltimeout);
		}
	}

	public int getNodeType() {
		String type = props.get(NODE_TYPE);
		if (type == null || "".equals(type.trim())) {
			return 0;
		} else {
			return Integer.parseInt(type);
		}
	}

	/**
	 * 
	 * @return LogInvoker is enable or not
	 */
	public boolean isLogInvokerEnable() {
		return (props.get(LOGINVOKER_SWITCH) != null 
				&& props.get(LOGINVOKER_SWITCH).equalsIgnoreCase("on")) ? true : false;
	}
	
	public String getDeploymentPatterns(){
		String patterns = "distributed";//默认分布式部署
		String pat = this.getProperty("deployment_patterns");
		if(pat!=null && "aio".equalsIgnoreCase(pat)){
			patterns = "aio";
		}
		return patterns;
	}
	public boolean needSet(){
		boolean set = false;
		String pat = this.getProperty("deployment_patterns");
		if(pat == null || "".equals(pat) || (!"distributed".equalsIgnoreCase(pat) && !"aio".equalsIgnoreCase(pat))){
			set = true;
		}
		return set;
	}
	public boolean isAIO(){
		String pat = this.getProperty("deployment_patterns");
		if(pat!=null && pat.equals("aio")){
			return true;
		}
		return false;
	}
	public void updateConfig(){
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(instroot + File.separator + "conf","mxsd_process.properties"));
			Properties properties = new Properties();
			Iterator<String> en = props.keySet().iterator();
			while (en.hasNext()) {
				String key = (String) en.next();
				properties.put(key, props.get(key));
			}
			properties.store(fout, "update at " + new Date().getTime());
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fout != null){
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fout = null;
			}
		}
	}
	
	public void setPoperValue(String key,String value){
		props.put(key, value);
		propConfiguration.setProperty(key, value);
	}
	public void updateConfigWithComment(){
		File file = new File(instroot + File.separator + "conf","mxsd_process.properties");
		try {
			propConfiguration.save(file);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void delPoperValue(String key){
		props.remove(key);
		propConfiguration.clearProperty(key);
	}
	
	public static String getPoolAttributes(String appID) {
		StringBuffer returnAttributes = new StringBuffer();
		returnAttributes.append("\t<appThreadPool>").append("\n");
		Set<Map.Entry<String, Executor>> entrySet = threadPools.entrySet();
		for (Map.Entry<String, Executor> entry : entrySet) {
			ThreadPoolExecutor threadPool = (ThreadPoolExecutor) entry.getValue();
			if (threadPool != null) {
				returnAttributes.append("\t\t");
				returnAttributes.append("<ThreadPool");
				returnAttributes.append(" app=\"").append(appID).append("\"");
				returnAttributes.append(" container=\"").append(getConfig().getProperty("com.dcfs.esb.client.location")).append("\"");
				String poolName = (String) entry.getKey();
				returnAttributes.append(" poolName=\"").append(poolName + "\"");
				int maxSize = threadPool.getMaximumPoolSize();
				returnAttributes.append(" maxSize=\"").append(maxSize + "\"");
				int activeSize = threadPool.getActiveCount();
				returnAttributes.append(" activeSize=\"").append(activeSize + "\"");
				returnAttributes.append(" coreSize=\"").append(threadPool.getCorePoolSize() + "\"");
				int poolSize = threadPool.getPoolSize();
				int unusedSize = poolSize - activeSize;
				returnAttributes.append(" unusedSize=\"").append(unusedSize + "\"");
				returnAttributes.append(" queueMaxSize=\"2147483647\"");
				int queueSize = threadPool.getQueue().size();
				returnAttributes.append(" queueDepth=\"").append(queueSize + "\"/>");
				returnAttributes.append("\n");
			}
		}
		returnAttributes.append("\t</appThreadPool>").append("\n");
		if (log.isDebugEnabled()) {
			log.debug(returnAttributes.toString());
		}
		return returnAttributes.toString();
	}
}

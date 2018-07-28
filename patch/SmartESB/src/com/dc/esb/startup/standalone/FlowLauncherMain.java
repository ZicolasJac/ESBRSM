package com.dc.esb.startup.standalone;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bis.impls.license.DataFactory;
import com.dc.esb.ESBConstants;
import com.dc.esb.flowcontrol.impl.FlowControlManager;
import com.dc.esb.startup.standalone.guardian.LauncherGuardian;
import com.dcfs.impls.esb.ESBConfig;
import com.dcfs.impls.esb.admin.ManagerFactory;
import com.dcfs.interfaces.esb.admin.IManageComponent;

public class FlowLauncherMain {

	private String esb_management;
	private static Log log = LogFactory.getLog(FlowLauncherMain.class);
	
	public void start(){
		// 获取文件锁
		int ret = DataFactory.check();
		if (ret == DataFactory.INVALID_LICENSE) {
			System.err.println("--------------------License验证失败--------------------");
			System.exit(1);
		}else{
			System.out.println("--------------------License验证成功--------------------");
		}
		String lockedFile = ESBConfig.getConfig().getInstallRoot() + File.separator + "ESBlock";
		boolean getLock = LauncherGuardian.getInstance().getFileLock(lockedFile);
		if (!getLock) {
			System.err.println("Can not achieve file lock of " + lockedFile + ". Another application may be running!");
			System.exit(1);
		}
		
		//读取配置参数
		esb_management = ESBConfig.getConfig().getProperty(
				ESBConstants.ESB_MANAGEMENT);
		if (log.isDebugEnabled()) {
			log.debug("esb_management :" + esb_management);
		}
		
		FlowControlManager manager;
		manager = FlowControlManager.getInstance();
	    Thread thread = new Thread(manager);
	    thread.start();
	    
		if ("true".equalsIgnoreCase(esb_management)) {
			//如果设置了从管理监控获取配置，则加载invoke.xml文件
			IManageComponent managerTmp = ManagerFactory.getManager();
			managerTmp.setReady(true);
		}
		
		// 监控获取ESB各容器线程池等参数的入口
		StartMonitorURL.start();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FlowLauncherMain fcm = new FlowLauncherMain();
		fcm.start();
	}
}

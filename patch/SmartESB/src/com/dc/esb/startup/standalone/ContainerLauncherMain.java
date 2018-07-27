package com.dc.esb.startup.standalone;

import java.io.File;

import com.bis.impls.license.DataFactory;
import com.dc.esb.container.databinding.channel.ChannelAppLauncherException;
import com.dc.esb.container.databinding.channel.ChannelAppLauncherMain;
import com.dc.esb.container.launcher.ContainerLauncher;
import com.dc.esb.container.protocol.ws.server.http.JettyServer;
import com.dc.esb.startup.standalone.guardian.LauncherGuardian;
import com.dc.esb.startup.standalone.guardian.StopProtocolTask;
import com.dcfs.impls.esb.ESBConfig;

public class ContainerLauncherMain {

	private static int containerType = 0;
	private ContainerLauncher launcher;

	private void setInstallRoot(String installRoot) {
		ESBConfig.setRoot(installRoot);
		System.out.println("t-->Application installRoot is [" + installRoot + "]");
	}

	public void start() {
		// 获取文件锁
		int ret = DataFactory.check();
		if (ret == DataFactory.INVALID_LICENSE) {
			System.err.println("--------------------License验证失败--------------------");
			System.exit(1);
		} else {
			System.out.println("--------------------License验证成功--------------------");
		}
		String lockedFile = ESBConfig.getConfig().getInstallRoot() + File.separator + "ESBlock";
		boolean getLock = LauncherGuardian.getInstance().getFileLock(lockedFile);
		if (!getLock) {
			System.err.println("Can not achieve file lock of " + lockedFile
					+ ". Another application may be running!");
			System.exit(1);
		}
		// 注册停止协议的钩子任务
		LauncherGuardian.getInstance().registerTask(new StopProtocolTask());

		String startChannelApp = System.getProperty("startChannelApp", "false");
		if (startChannelApp.equals("true")) {
			try {
				ChannelAppLauncherMain.main(null);
			} catch (ChannelAppLauncherException e) {
				e.printStackTrace();
				System.out.println("**************start channelApp failed!****************");
			}
		}
		containerType = ESBConfig.getConfig().getNodeType();

		if (containerType <= 0) {
			throw new IllegalArgumentException("Illegal node type: " + containerType);
		}

		launcher = new ContainerLauncher(containerType);
		launcher.start();

		if (containerType == 1) {
			// 如果是接入容器, 启动JettyServer, 用于WebService Server的监听
			int port = 9000;
			String p = System.getProperty("com.dc.esb.app_port");
			if (p != null)
				port = Integer.parseInt(p.trim());
			JettyServer.getInstance().addServletMapping(
					"http://localhost:" + port + "/esb_launcher/esbwebservice/*");

		}
		
		// 监控获取ESB各容器线程池等参数的入口
		StartMonitorURL.start();
		
		ret = DataFactory.check();
		if (ret == DataFactory.INVALID_LICENSE) {
			System.err.println("--------------------License验证失败--------------------");
			System.exit(1);
		} else {
			System.out.println("--------------------License验证成功--------------------");
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ContainerLauncherMain clm = new ContainerLauncherMain();
		String installRoot = "";
		if (args != null && args.length > 0) {
			// 标识应用类型,IN/OUT
			installRoot = args[0];
		}
		if (installRoot.trim().length() > 0)
			clm.setInstallRoot(installRoot);
		clm.start();
	}

}

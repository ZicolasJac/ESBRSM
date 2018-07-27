package com.dc.esb.startup.standalone;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletException;

import com.bis.impls.license.DataFactory;
import com.dc.esb.container.router.noroutermode.CircleLinked;
import com.dc.esb.container.router.noroutermode.InnerMessage;
import com.dc.esb.container.router.noroutermode.NoRouterConsumerListener;
import com.dc.esb.container.router.noroutermode.SharedQueueManager;
import com.dc.esb.container.router.noroutermode.console.BlockingQueueAgent;
import com.dc.esb.startup.standalone.guardian.LauncherGuardian;
import com.dcfs.impls.esb.ESBConfig;
import com.dcfs.impls.esb.router.RouterLauncher;
import com.dcfs.interfaces.esb.client.MemoryRouterIServiceHandler;

public class RouterLauncherMain {

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

		RouterLauncher launcher = new RouterLauncher();
		try {
			launcher.init();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean integrated = "true".equals(System.getProperty("com.dc.inout.integrated"));
		boolean shortCircurt = "true".equals(System.getProperty("com.dc.inout.shortcircurt"));
		if (integrated && !shortCircurt) {
			// ALL IN ONE 并且不短路模式下启动。
			startMemeoryQueueListener();
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

	private void startMemeoryQueueListener() {

		System.out.println("-->\tRouter integrated mode is open using shortcircurt mode.");

		int poolSize = 10;
		String _poolSize = ESBConfig.getConfig().getProperty("thread_pool_size");
		if (_poolSize != null) {
			poolSize = Integer.parseInt(_poolSize);
		}
		final String location = "Router";
		ExecutorService executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize,
				new ThreadFactory() {
					ThreadGroup tg = new ThreadGroup("pool-BlockingQueueHandler-" + location);

					public Thread newThread(Runnable r) {
						return new Thread(tg, r, "pool-BlockingQueueHandler-" + location);
					}

				});
		MemoryRouterIServiceHandler dispatcher = null;
		String _dispatcher = ESBConfig.getConfig().getProperty("com.dcfs.esb.memoryrouter.invoker");
		try {
			dispatcher = (MemoryRouterIServiceHandler) Class.forName(_dispatcher).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ("true".equals(System.getProperty("com.dc.inout.blocking"))) {
			CircleLinked<BlockingQueueAgent<InnerMessage>> circle = null;
			circle = SharedQueueManager.getInstance().getMailBoxCircleLinked();
			for (int i = 0; i < circle.getSize(); i++) {
				new NoRouterConsumerListener(circle.next(), executor, dispatcher).start();
			}
		}
		System.out.println("-->\tRouter Listener on memeory queue is established");
	}

	public static void main(String[] args) {
		RouterLauncherMain rlm = new RouterLauncherMain();
		String installRoot = "";
		if (args != null && args.length > 0) {
			// 标识应用类型,IN/OUT
			installRoot = args[0];
		}
		if (installRoot.trim().length() > 0)
			rlm.setInstallRoot(installRoot);
		rlm.start();
	}
}

package com.dc.esc;

import java.util.concurrent.CountDownLatch;

import com.dc.esc.journallog.ActiveJournallogProcess;
import com.dc.esc.journallog.JournallogProcess;
import com.dc.esc.journallog.SingleJournallogProcess;
import com.dc.esc.journallog.StandbyJournallogProcess;
import com.dc.esc.journallog.db.ActiveDBInsert;
import com.dc.esc.journallog.db.DBInsert;
import com.dc.esc.journallog.db.StandbyDBInsert;
import com.dc.esc.monitor.StartMonitorURL;

/**
 * 流水服务器启动入口
 * @author Administrator
 * @Created on 2016-07-26
 */
public class ESBJournal {
	// 双写开关
	private static boolean isDouble = ESCConfig.getConfig().getPropertyBoolean(ESCConfig.DB_DOUBLEINSERT);
	// 非双写入库线程数量
	private static int singleInsertThreadNum = ESCConfig.getConfig().getPropertyInt("journallog.singleInsertThread.number");
	// 双写-主库入库线程数量
	private static int activeInsertThreadNum = ESCConfig.getConfig().getPropertyInt("journallog.activeInsertThread.number");
	// 双写-备库入库线程数量
	private static int standbyInsertThreadNum = ESCConfig.getConfig().getPropertyInt("journallog.standbyInsertThread.number");
	
	// 闭锁
	private static int count = isDouble ? (activeInsertThreadNum + 1 + standbyInsertThreadNum + 1) : (singleInsertThreadNum + 1);
	public static CountDownLatch latch = new CountDownLatch(count);
	
	public static void start() {
		//加载参数
		ESCConfig.getConfig();
		
		if (!isDouble) {
			for (int i = 0; i < singleInsertThreadNum; i++) {
			    if (i == 0) {
			        Thread watchDogForSingle = new Thread(new DBInsert());
			        watchDogForSingle.setPriority(Thread.MAX_PRIORITY);
			        watchDogForSingle.start();
			        continue;
			    }
				new Thread(new DBInsert()).start();
			}
			new SingleJournallogProcess().start();
		} else {
			for (int i = 0; i < activeInsertThreadNum; i++) {
			    if (i == 0) {
                    Thread watchDogForActive = new Thread(new ActiveDBInsert());
                    watchDogForActive.setPriority(Thread.MAX_PRIORITY);
                    watchDogForActive.start();
                    continue;
                }
				new Thread(new ActiveDBInsert()).start();
			}
			new ActiveJournallogProcess().start();
			
			for (int i = 0; i < standbyInsertThreadNum; i++) {
			    if (i == 0) {
			        Thread watchDogForStandby = new Thread(new StandbyDBInsert());
			        watchDogForStandby.setPriority(Thread.MAX_PRIORITY);
			        watchDogForStandby.start();
                    continue;
			    }
				new Thread(new StandbyDBInsert()).start();
			}
			new StandbyJournallogProcess().start();
		}
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// 启动异步线程处理流水文件
		new JournallogProcess().start();
		
		System.out.println("ESB Application <JOURNAL> startup");
	}
	
	public static void stop() {
		try {
			Thread.sleep(5000);
			System.out.println("ESB Application <JOURNAL> stop Success");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("ESB Application <JOURNAL> stop Failed"+e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		if("stop".equals(args[0])){
			StartMonitorURL.stop();
			stop();
		}else if("start".equals(args[0])){
			StartMonitorURL.start();
			start();
		}

	}

}

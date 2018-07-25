package com.dcfs.rsm.client;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.dcfs.rsm.utils.RsmConnector;

public class SendMonData {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("missing params,right params is:port filename");
			return;
		}

		String[] addr = args[0].split(":");
		String ip = addr[0];
		int port = Integer.parseInt(addr[1]);
		String fileName = args[1];
		
		File file = new File(fileName);
		if(file.exists()) {
			RsmConnector conn = null;
			try {
				byte[] data = readFileByByte(file);
				conn = new RsmConnector(ip, port);
				conn.writeCont(data);
				String retData;
				try {
					retData = conn.readCont();
					System.out.println("Send file [" + fileName + "] to [" + args[0] + "] is " + retData);
				} catch (EOFException e) {
					System.out.println("Send file [" + fileName + "] to [" + args[0] + "] completed,not receive response!");
				}
			} catch (Exception e) {
				System.out.println("error:" + e);
				if (conn != null)
					try {
						conn.close();
					} catch (Exception e1) {
						System.out.println("close socket error.");
					}
			} finally {			
				if (conn != null)
					try {
						conn.close();
					} catch (Exception e) {
						System.out.println("close socket error.");
					}
			}
		}else {
			System.err.println("文件[" + fileName + "]不存在!");
		}
	}
	
	/**
	 * 以字节为单位读取文件,块读取,直接将数据存入缓冲区中
	 * @param file
	 */
	public static byte[] readFileByByte(File file) { 
		FileInputStream fis = null;
		byte[] resByte = null;
		try {
			fis = new FileInputStream(file);
			resByte = new byte[fis.available()];
			byte[] buf = new byte[10240];
			// 结果集中的元素下标
			int btIndex = 0;
			// 每次读入的字节数
			int oneReadNum = -1; 
			while ((oneReadNum = fis.read(buf)) != -1) {
				for (int i = 0; i < oneReadNum; i++) {
					resByte[btIndex] = buf[i];
					btIndex++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return resByte;
	}
}
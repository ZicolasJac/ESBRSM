package com.dcfs.rsm.esbpool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

public class CollectESBPool {
	private static String appID = "AP1";
	private static String path = null;
	private static String in_url = "http://127.0.0.1:8070/esb_monitor";
	private static String router_url = "http://127.0.0.1:8071/esb_monitor";
	private static String out_url = "http://127.0.0.1:8072/esb_monitor";
	private static String journal_url = "http://127.0.0.1:8073/esb_monitor";
	private static String flow_url = "http://127.0.0.1:8074/esb_monitor";
	private static String subFlow_url = "http://127.0.0.1:8074/esb_monitor";

	private void execute() {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		collect(in_url, localByteArrayOutputStream, appID);
		collect(router_url, localByteArrayOutputStream, appID);
		collect(out_url, localByteArrayOutputStream, appID);
		collect(journal_url, localByteArrayOutputStream, appID);
		collectFlow(flow_url, subFlow_url, localByteArrayOutputStream, appID);
		writeToFile(path, localByteArrayOutputStream.toByteArray());
	}

	private void collect(String paramString1, ByteArrayOutputStream paramByteArrayOutputStream, String paramString2) {
		URL localURL = null;
		URLConnection localURLConnection = null;
		InputStream localInputStream = null;
		paramString1 = paramString1 + "?app=" + paramString2;
		try {
			localURL = new URL(null, paramString1, ClientTimeoutHandler.getInstance(5000, 5000));
			localURLConnection = localURL.openConnection();
			localURLConnection.setDoOutput(false);
			localInputStream = localURLConnection.getInputStream();
			byte[] arrayOfByte = new byte[1024];
			int i = 0;
			while ((i = localInputStream.read(arrayOfByte)) != -1)
				paramByteArrayOutputStream.write(arrayOfByte, 0, i);
		} catch (Exception e) {
			System.out.println("url=[" + paramString1 + "]" + e);
		} finally {
			if (localInputStream != null)
				try {
					localInputStream.close();
				} catch (Exception localException4) {
					localException4.printStackTrace();
				}
		}
	}

	private void collectFlow(String paramString1, String paramString2, ByteArrayOutputStream paramByteArrayOutputStream,
			String paramString3) {
		URL localURL = null;
		URLConnection localURLConnection = null;
		InputStream localInputStream = null;
		paramString1 = paramString1 + "?app=" + paramString3;
		try {
			localURL = new URL(null, paramString1, ClientTimeoutHandler.getInstance(5000, 5000));
			localURLConnection = localURL.openConnection();
			localURLConnection.setDoOutput(false);
			localInputStream = localURLConnection.getInputStream();
			byte[] arrayOfByte = new byte[1024];
			int i = 0;
			while ((i = localInputStream.read(arrayOfByte)) != -1)
				paramByteArrayOutputStream.write(arrayOfByte, 0, i);
		} catch (Exception e) {
			System.out.println("url=[" + paramString1 + "]" + e);
			if (paramString2 != null) {
				System.out.println("主流控连接失败,使用备流控");
				collectFlow(paramString2, null, paramByteArrayOutputStream, paramString3);
			} else {
				System.out.println("备流控连接失败,不采集本地流控信息");
			}
		} finally {
			if (localInputStream != null)
				try {
					localInputStream.close();
				} catch (Exception localException4) {
					localException4.printStackTrace();
				}
		}
	}
	
	private static void writeToFile(String path, byte[] value){
		if (path == null) {
			System.err.println("文件名不能为空!");
			return;
		}
		
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
			fos.write(value);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fos != null)
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 *  参数:应用标识,结果输出路径,in容器监听地址,router容器监听地址,out容器监听地址,流水应用监听地址,主流控地址,备流控地址
	 * @param paramArrayOfString
	 */
	public static void main(String[] paramArrayOfString) {
		if (paramArrayOfString != null) {
			if (paramArrayOfString.length < 8) {
				System.err.println("missing params,right params is:port filename");
				return;
			}
			appID = paramArrayOfString[0];
			path = paramArrayOfString[1];
			in_url = paramArrayOfString[2];
			router_url = paramArrayOfString[3];
			out_url = paramArrayOfString[4];
			journal_url = paramArrayOfString[5];
			flow_url = paramArrayOfString[6];
			subFlow_url = paramArrayOfString[7];
		}
		new CollectESBPool().execute();
	}
}
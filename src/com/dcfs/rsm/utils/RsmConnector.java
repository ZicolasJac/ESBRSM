package com.dcfs.rsm.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class RsmConnector {
	private Socket socket = null;
	private String clientIp = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;

	public RsmConnector(String ip, int port) throws Exception {
		this.socket = new Socket(ip, port);
		this.in = new DataInputStream(this.socket.getInputStream());
		this.out = new DataOutputStream(this.socket.getOutputStream());
	}

	public RsmConnector(Socket socket) throws Exception {
		this.socket = socket;
		this.clientIp = socket.getInetAddress().toString();
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public void close() throws Exception {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
		if (this.out != null) {
			this.out.close();
			this.out = null;
		}
		if (this.socket != null) {
			this.socket.close();
			this.socket = null;
		}
	}

	public DataInputStream getIn() throws Exception {
		return this.in;
	}

	public DataOutputStream getOut() throws Exception {
		return this.out;
	}

	public Socket getSocket() throws Exception {
		return this.socket;
	}

	public void writeCont(byte[] bean) throws Exception {
		int len = bean.length;
		this.out.writeInt(len);
		this.out.write(bean, 0, len);
		this.out.flush();
	}

	public String readCont() throws Exception {
		int len = readInt();
		System.out.println("step4--len:" + len);
		byte[] data = readNum(len);
		System.out.println("step5");
		String retStr = new String(data, "UTF-8");
		System.out.println("step6--str:"+retStr);
		return retStr;
	}

	private int readInt() throws Exception {
		int len = -1;
		while (len < 0)
			len = this.in.readInt();
		return len;
	}

	private byte[] readNum(int num) throws Exception {
		byte[] data = new byte[num];
		int offset = 0;
		int count = -1;
		while (-1 != (count = this.in.read(data, offset, num - offset))) {
			offset += count;
			if (offset == num)
				break;
		}
		return data;
	}

	public String getClientIp() {
		return this.clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
}

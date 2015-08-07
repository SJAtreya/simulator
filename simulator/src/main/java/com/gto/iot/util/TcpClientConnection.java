package com.gto.iot.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClientConnection {

	private static Socket socket = null;
	private static TcpClientConnection clientSocketConnection = new TcpClientConnection();
	private static final String TCP_SERVER = "localhost";
	private static final int PORT = 9090;
	
	private TcpClientConnection() {
		connect();
	}
	
	public static TcpClientConnection getInstance() {
		return clientSocketConnection;
	}
	
	public boolean sendMessage(String msg) throws IOException {
		int tryAttemptCounter = 0;
//		System.out.println("Trying to send message:"+msg);
		if (socket== null || !socket.isConnected()) {
//			System.out.println("Trying to get a socket connection!");
			do {
				if (tryAttemptCounter>5) {
					break;
				}
				connect();
				tryAttemptCounter++;
			} while (!socket.isConnected());
		}
		
//		System.out.println("Tried connecting to socket, status:"+socket.isConnected());
		if (socket == null || !socket.isConnected()) {
			return false;
		}
//		System.out.println("Trying to send a message");
		DataOutputStream dataoutputstream = new DataOutputStream(socket.getOutputStream());
		dataoutputstream.writeBytes(msg);
		dataoutputstream.writeByte('\r');
		dataoutputstream.writeByte('\n');
		dataoutputstream.flush();
//		dataoutputstream.close();
//		System.out.println("Sent message");
		return true;
	}
	
	public void connect () {
		try {
			socket = new Socket(TCP_SERVER, PORT);
			socket.setKeepAlive(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
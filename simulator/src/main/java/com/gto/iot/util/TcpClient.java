package com.gto.iot.util;

import java.io.IOException;

public class TcpClient {

	public static synchronized void sendMessage(String msg) throws IOException {
		TcpClientConnection.getInstance().sendMessage(msg);
	}
}

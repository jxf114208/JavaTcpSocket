package com.jzj.socket;

import ru.d_shap.hex.HexHelper;

public class ClsMainServer {

	public static void main(String[] args) {
		int port = 6017;
		TcpServer server = new TcpServer( port ) {

			@Override
			public void onConnect(SocketTransceiver client) {
				printInfo( client, "Connect" );
			}

			@Override
			public void onConnectFailed() {
				System.out.println( "Client Connect Failed" );
			}

			@Override
			public void onReceive(SocketTransceiver client, byte[] b) {
				printInfo( client, "Send Data: " + b );
				client.send( HexHelper.toBytes( "0000000000100006313338303335313030303100013D" ) );
			}

			@Override
			public void onDisconnect(SocketTransceiver client) {
				printInfo( client, "Disconnect" );
			}

			@Override
			public void onServerStop() {
				System.out.println( "--------Server Stopped--------" );
			}
		};
		System.out.println( "--------Server Started--------" );
		server.start();
	}

	static void printInfo(SocketTransceiver st, String msg) {
		System.out.println( "Client " + st.getInetAddress().getHostAddress() );
		System.out.println( "  " + msg );
	}
}

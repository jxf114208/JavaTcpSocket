package com.jzj.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 *
 * @author jzj1993
 * @since 2015-2-22
 */
public abstract class SocketTransceiver implements Runnable {

	protected Socket socket;
	protected InetAddress addr;
	protected DataInputStream in;
	protected DataOutputStream out;
	private boolean runFlag;

	/**
	 * 实例化
	 *
	 * @param socket
	 *          已经建立连接的socket
	 */
	public SocketTransceiver(Socket socket) {
		this.socket = socket;
		this.addr = socket.getInetAddress();
	}

	/**
	 * 获取连接到的Socket地址
	 *
	 * @return InetAddress对象
	 */
	public InetAddress getInetAddress() {
		return this.addr;
	}

	/**
	 * 开启Socket收发
	 * <p>
	 * 如果开启失败，会断开连接并回调{@code onDisconnect()}
	 */
	public void start() {
		this.runFlag = true;
		new Thread( this ).start();
	}

	/**
	 * 断开连接(主动)
	 * <p>
	 * 连接断开后，会回调{@code onDisconnect()}
	 */
	public void stop() {
		this.runFlag = false;
		try {
			this.socket.shutdownInput();
			this.in.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送字符串
	 *
	 * @param s
	 *          字符串
	 * @return 发送成功返回true
	 */
	public boolean send(byte[] b) {
		if ( this.out != null ) {
			try {
				this.out.write( b );
				this.out.flush();
				return true;
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 监听Socket接收的数据(新线程中运行)
	 */
	@Override
	public void run() {
		try {
			this.in = new DataInputStream( this.socket.getInputStream() );
			this.out = new DataOutputStream( this.socket.getOutputStream() );
		}
		catch ( IOException e ) {
			e.printStackTrace();
			this.runFlag = false;
		}
		while ( this.runFlag ) {
			// 此处需要增加超时设置，防止socket客户端已关闭，服务端没有及时关闭的问题。如：客户端长期没有发送数据
			try {
				if ( this.in.available() > 0 ) {
					final byte[] buffer = new byte[this.in.available()];
					this.in.read( buffer );
					this.onReceive( this.addr, buffer );
				}
				else {
					// 等待100毫秒
					try {
						Thread.sleep( 100 );
					}
					catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				}
			}
			catch ( IOException e ) {
				// 连接被断开(被动)
				this.runFlag = false;
			}
		}
		// 断开连接
		try

		{
			this.in.close();
			this.out.close();
			this.socket.close();
			this.in = null;
			this.out = null;
			this.socket = null;
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		this.onDisconnect( this.addr );
	}

	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 *
	 * @param addr
	 *          连接到的Socket地址
	 * @param s
	 *          收到的字符串
	 */
	public abstract void onReceive(InetAddress addr, byte[] s);

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 *
	 * @param addr
	 *          连接到的Socket地址
	 */
	public abstract void onDisconnect(InetAddress addr);
}

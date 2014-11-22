package com.naruto.tpms.app.comm;

/**
 * 命令消息常量类
 * 
 * @author Thinkman 415071574@qq.com
 */
public final class Commend {
	/**
	 * 发送的命令
	 * 
	 * @author Thinkman 415071574@qq.com
	 */
	public static final class TX {
		/**
		 * 查询接收器ID(固定命令)
		 */
		public static final byte[] QUERY_TIRE_ID = new byte[] { (byte) 0x055, (byte) 0xaa, (byte) 0x06, (byte) 0x02, (byte) 0x00, (byte) 0xfb };
		/**
		 * 退出配对学习(固定命令)
		 */
		public static final byte[] QUIT_STUDY = new byte[] { (byte) 0x055, (byte) 0xaa, (byte) 0x06, (byte) 0x06, (byte) 0x00, (byte) 0xff };
		/**
		 * 设为静音
		 */
		public static final byte[] SET_MUTE_ON = new byte[] { (byte) 0x055, (byte) 0xaa, (byte) 0x06, (byte) 0x04, (byte) 0x00, (byte) 0xff };
		/**
		 * 取消静音
		 */
		public static final byte[] SET_MUTE_OFF = new byte[] { (byte) 0x055, (byte) 0xaa, (byte) 0x06, (byte) 0x04, (byte) 0x01, (byte) 0xff };
	}

	/**
	 * 接收的命令
	 * 
	 * @author Thinkman 415071574@qq.com
	 */
	public static final class RX {

	}

}

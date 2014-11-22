package com.naruto.tpms.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import com.naruto.tpms.app.comm.util.CommUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//@SuppressLint("NewApi")
public class BTService {
	private final static String TAG = "BTService";
	public static final int MSG_TOAST = 10;
	public static final int MSG_READ = 11;
	public static final int MSG_CONNECTED = 12;
	public static final int MSG_CONNECTING = 13;
	public static final int MSG_WRITE = 14;
	public static final int MSG_CONNECTED_FAIL = 15;
	public static final int MSG_CONNECTED_LOST = 16;
	// 状态
	public final static int STATE_NONE = 0;
	public final static int STATE_LISTEN = 1;
	public final static int STATE_CONNECTING = 2;
	public final static int STATE_CONNECTED = 3;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private Handler handler;
	private BluetoothAdapter mAdapter;
	private int mState;
	private ConnectThread mConnectThread;

	private ConnectedThread mConnectedThread;

	public BTService(Handler handler) {
		this.handler = handler;
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public synchronized int getState() {
		return mState;
	}

	public synchronized void setState(int state) {
		Log.v(TAG, "state:" + this.mState + "->" + state);
		this.mState = state;
	}

	public void connect(BluetoothDevice device) {
		Log.v(TAG, "start connect:" + device.getAddress());
		if (getState() == STATE_CONNECTING && mConnectThread != null) {
			mConnectThread.cancel();
			mConnectedThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
		// 通知正在连接
		Message msg = handler.obtainMessage(MSG_CONNECTING);
		msg.obj = device;
		handler.sendMessage(msg);
	}

	public synchronized void connected(BluetoothDevice device, BluetoothSocket socket) {
		Log.v(TAG, "connected");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectedThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectedThread = new ConnectedThread(device, socket);
		mConnectedThread.start();
		setState(STATE_CONNECTED);
		// 通知已连上
		Message msg = handler.obtainMessage(MSG_CONNECTED);
		msg.obj = device;
		handler.sendMessage(msg);
	}

	public synchronized void start() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		setState(STATE_LISTEN);
	}

	public synchronized void stop() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectedThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		// Log.d("debug","关闭蓝牙连接完成");
		setState(STATE_NONE);
	}

	public synchronized boolean sendMsg(byte[] out) {
		ConnectedThread r;
		synchronized (this) {
			if (getState() != STATE_CONNECTED)
				return false;
			r = mConnectedThread;
		}
		return r.sendMsg(out);
	}

	private void connectionFailed() {
		if (getState() == STATE_NONE) {
			return;
		}
		Message msg = handler.obtainMessage(MSG_CONNECTED_FAIL);
		handler.sendMessage(msg);
		BTService.this.start();
	}

	private void connectionLost() {
		if (getState() == STATE_NONE) {
			return;
		}
		Message msg = handler.obtainMessage(MSG_CONNECTED_LOST);
		handler.sendMessage(msg);
		BTService.this.start();
	}

	/**
	 * 客户端模式线程
	 * 
	 * @author Thinkman 415071574@qq.com
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private OutputStream mOutputStream;
		private InputStream mInputStream;

		public ConnectedThread(BluetoothDevice mDevice, BluetoothSocket mSocket) {
			BluetoothSocket tempSocket = mSocket;
			this.mSocket = tempSocket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpOut = mSocket.getOutputStream();
				tmpIn = mSocket.getInputStream();
			} catch (IOException e) {
				Log.e(TAG, "get inputstream fail", e);
				e.printStackTrace();
			}
			mOutputStream = tmpOut;
			mInputStream = tmpIn;
		}

		@Override
		public void run() {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int msgLength = -1;// -1表示还没读到下条消息长度
			Byte curByte = null, lastByte = null, lastByte2 = null;// 最新读到的字节,上一个读到的字节,上上个读到的字节
			while (BTService.this.getState() == BTService.STATE_CONNECTED) {
				try {
					// 值的迁移
					lastByte2 = lastByte;//倒数第二位
					lastByte = curByte;//倒数第一位
					curByte = Byte.valueOf((byte) mInputStream.read());//当前位
					//
					bos.write(curByte.byteValue());
//					 Log.d(TAG,"读取字节:"+curByte.byteValue());
					// 获取消息长度
					if (lastByte != null && lastByte2 != null && lastByte2.byteValue() == 0x55 && lastByte.byteValue() == (byte) 0xAA) {
						msgLength = curByte.byteValue();//当前长度位
						// 判断消息是否结束
					} else if (bos.size() == msgLength) {
						 Log.v(TAG, " 已读取到的长度:" + bos.size()+",应读取的长度:"+msgLength);
						Message msg = handler.obtainMessage(MSG_READ);
						byte[] bytes = bos.toByteArray();
						// 消息交互时可能丢个把包，导致数据不完整，所以不能完全根据消息长度来分割消息
						if (bytes[0] == 0x55 || bytes[1] == 0xAA) {
							msg.obj = bytes;
							Log.v(TAG, "-正常返回的消息:" + CommUtil.byte2HexStr(bytes) + ",长度：" + bytes.length);
							handler.sendMessage(msg);
							bos.reset();
							msgLength = -1;
						}
						// 在bos中有数据，但有读到55 AA时,先返回之前的消息，再清空
					} else if (bos.size() > 3 && lastByte != null && curByte != null && lastByte.byteValue() == 0x55
							&& curByte.byteValue() == (byte) 0xAA) {
						byte[] result = bos.toByteArray();
						// 先去掉最后两位再返回
						Message msg = handler.obtainMessage(MSG_READ);
						byte[] byteMsg = new byte[result.length - 2];
						System.arraycopy(result, 0, byteMsg, 0, result.length - 2);
						msg.obj = byteMsg;
						Log.v(TAG, "*返回的消息:" + CommUtil.byte2HexStr(byteMsg) + ",长度：" + byteMsg.length);
						handler.sendMessage(msg);
						// 重置字节流数组流后再写入去掉的两位
						bos.reset();
						msgLength = -1;
						bos.write(result[result.length - 2]);
						bos.write(result[result.length - 1]);
					}
					// if (-1 != (len = mInputStream.read(buffer, 0, 512))) {
					// bos.write(buffer, 0, len);
					// // Log.v(TAG, " 新长度:" +
					// // len+",已收内容"+Arrays.toString(bos.toByteArray()));
					// // if (bos.size() >= 4) {
					// // break;
					// // }
					// }
				} catch (IOException e) {
					Log.e(TAG, "IOException", e);
					try {
						mInputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					mInputStream = null;
				} catch (Exception e) {
					Log.e(TAG, "read stream fail", e);
					connectionLost();
					break;
				}
				try {
					sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			Log.d(TAG, "while 循环结束"  );
			connectionLost();
		}

		public synchronized boolean sendMsg(byte[] out) {
			try {

//				Log.d(TAG, "马上写出消息:" + Arrays.toString(out) + "十六进制:" + CommUtil.byte2HexStr(out));
				mOutputStream.write(out);
				mOutputStream.flush();
				Thread.sleep(100);// 单片机消息处理不过来,所以每次发送指令间隔一段时间
				Message msg = handler.obtainMessage(MSG_WRITE);
				handler.sendMessage(msg);
				return true;
			} catch (IOException e) {
				Log.e(TAG, "write stream fail", e);
				try {
					mOutputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				mOutputStream = null;
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false;
		}

		public void cancel() {
			try {
				if (mOutputStream != null) {
					mOutputStream.close();
					mOutputStream = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "close connect fail", e);
			}
			try {
				if (mInputStream != null) {
					mInputStream.close();
					mInputStream = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "close connect fail", e);
			}
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close connect fail", e);
			}
		}
	}

	/**
	 * 连接线程
	 * 
	 * @author Thinkman 415071574@qq.com
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;

		public ConnectThread(BluetoothDevice device) {
			mDevice = device;
			BluetoothSocket temp = null;
			try {
				// temp =
				// device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
				temp = device.createRfcommSocketToServiceRecord(MY_UUID);
				// temp.connect();
			} catch (IOException e) {
				Log.e(TAG, "create connect  fail", e);
				e.printStackTrace();
			}
			mSocket = temp;
		}

		@Override
		public void run() {
			mAdapter.cancelDiscovery();
			try {
				mSocket.connect();
			} catch (IOException e) {
				try {
					mSocket.close();
				} catch (IOException e1) {
					Log.e(TAG, "close connect  fail", e);
					e1.printStackTrace();
				}
				Log.e(TAG, "connect fail", e);
				e.printStackTrace();
				// 通知失败
				connectionFailed();
				return;
			}
			synchronized (BTService.this) {
				mConnectThread = null;
			}
			// 连接上了则读取消息
			connected(mDevice, mSocket);
		}

		public void cancel() {
			try {
				if (mSocket != null) {
					mSocket.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "close socket fail", e);
			}
		}
	}
}

package com.naruto.tpms.app;

import java.util.Arrays;
import java.util.List;

import com.naruto.tpms.app.bean.TireData;
import com.naruto.tpms.app.comm.Commend;
import com.naruto.tpms.app.comm.Constants;
import com.naruto.tpms.app.comm.util.CommUtil;
import com.naruto.tpms.app.comm.util.MusicUtil;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 后台核心服务
 * 
 * @author Thinkman
 * 
 */
@SuppressLint("HandlerLeak")
public class CoreBtService extends Service {

	public static final String TAG = "CoreBtService";
	public static final int MSG_AUTO_REFRESH_VIEW = 123;
	// 广播定义
	public static final String BROADCAST_CONNECTED = "com.naruto.tpms.app.CONNECTED";
	public static final String BROADCAST_CONNECTING = "com.naruto.tpms.app.CONNECTING";
	public static final String BROADCAST_CONNECTFAIL = "com.naruto.tpms.app.CONNECTFAIL";
	public static final String BROADCAST_CONNECTLOST = "com.naruto.tpms.app.CONNECTLOST";
	public static final String BROADCAST_READ = "com.naruto.tpms.app.READ";
	public static final String BROADCAST_WRITE = "com.naruto.tpms.app.WRITE";
	public static final String BROADCAST_UPDATE_SETTINGS = "com.naruto.tpms.app.UPDATESETTINGS";
	public static final String BROADCAST_TOAST = "com.naruto.tpms.app.TOAST";
	// 以下是service应该接收的广播
	public static final String BROADCAST_RECONNECT = "com.naruto.tpms.app.RECONNECT";
	public static final String BROADCAST_DISCONNECT = "com.naruto.tpms.app.DISCONNECT";
	private static final int FOREGROUND_ID = R.drawable.icon;// 通知id

	// 数据信息
	private BluetoothAdapter bAdapter;
	private BluetoothDevice device;// 位置绑定的设备
	private SharedPreferences pref;
	private BTService mService;
	private final int MAX_RETRY_COUNT = 3;// 最多重试次数
	private int retryCount = 0;// 连接失败重试了的次数
	private Vibrator vibrator;
	private TireData[] tireDataArr = new TireData[4];// 4个轮胎(依次为左前、右前、左后、右后)
	public double highPressure, lowPressure, highTemprature;
	private PendingIntent contentIntent;
	private RemoteViews contentView;// 通知的自定义视图
	private Notification mNotification;
	private String[] tireNames;
	private String resRunning;
	private PowerManager.WakeLock mWakeLock;

	/**
	 * 告警是否开启
	 */
	private boolean isAlarmOn;

	private Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_AUTO_REFRESH_VIEW:
				mHandler.removeMessages(MSG_AUTO_REFRESH_VIEW);
				// refreshView();
				parseData();
				mHandler.sendEmptyMessageDelayed(MSG_AUTO_REFRESH_VIEW, 1000);
				break;
			case BTService.MSG_TOAST:
				CommUtil.showTips(R.drawable.tips_warning, String.valueOf(msg.obj));
				break;
			case BTService.MSG_CONNECTED: {
				// Log.d(TAG, "连接成功");
				retryCount = 0;
				device = (BluetoothDevice) msg.obj;
				notifyConnected(device);
				notification(resRunning, false);
				// 发送查询轮胎id的指令
				mService.sendMsg(Commend.TX.QUERY_TIRE_ID);
			}
				break;
			case BTService.MSG_CONNECTING: {
				// Log.d(TAG, "连接中...");
				notifyConnecting();
			}
				break;
			case BTService.MSG_CONNECTED_FAIL: {
//				 Log.d(TAG, "连接失败...");
				if (retryCount++ < MAX_RETRY_COUNT) {
					// Log.d(TAG, "连接失败,重试" + retryCount);
					doConnect();
				} else {
					retryCount = 0;
					CommUtil.showTips(R.drawable.tips_error, R.string.tips_connect_fail);
					mHandler.removeMessages(MSG_AUTO_REFRESH_VIEW);
					parseData();
					disConnect();
					notifyConnectFail();
				}
			}
				break;
			case BTService.MSG_CONNECTED_LOST: {
				 Log.d(TAG, "连接丢失");
				retryCount = 0;
				mHandler.removeMessages(MSG_AUTO_REFRESH_VIEW);
				// refreshView();
				disConnect();
				notifyConnectLost();
//				CommUtil.showTips(R.drawable.tips_error, R.string.tips_connect_lost);
			}
				break;
			case BTService.MSG_WRITE:
				break;
			case BTService.MSG_READ:
				byte[] report = (byte[]) msg.obj;
				Log.d(TAG, "接收到的消息:" + Arrays.toString(report) + "十六进制:" + CommUtil.byte2HexStr(report));

				// 验证消息长度
				if (report.length < 3) {
					return;
				}
				// 验证消息固定位
				if (report[0] != (byte) 0x55 || report[1] != (byte) 0xAA) {
					return;
				}
				// 计算校验位(轮胎id查询无检验位)
				if (report[2] != 0x07) {
					byte checkByte = CommUtil.getCheckByte(report, 0, report.length - 2);// 获得校验位
					if (checkByte != report[report.length - 1]) {
						// Log.w(TAG, "校验位不正确,当前校验位=" + report[report.length -
						// 1] + ",正确校验位应=" + checkByte);
						return;
					}
				}
				notifyRead(report);

				// 解析消息
				// 状态上报
				// 1.08表示状态上报
				if (report[2] == 0x08) {
					int index = CommUtil.getTireIndexByTireNum(report[3]);
					if (index != -1 && tireDataArr[index] != null) {
						tireDataArr[index].lastUpdateTime = System.currentTimeMillis();
						tireDataArr[index].pressure = Integer.parseInt(Integer.toHexString(report[4] & 0XFF), 16) * 3.44;
						tireDataArr[index].temperature = Integer.parseInt(Integer.toHexString(report[5] & 0XFF), 16) - 50;
						String status = CommUtil.byteToBinaryString(report[6]);
						tireDataArr[index].isSignalError = '1' == status.charAt(2);
						tireDataArr[index].isLowBattery = '1' == status.charAt(3);
						tireDataArr[index].isLeak = '1' == status.charAt(4);
					}
					mHandler.sendEmptyMessage(MSG_AUTO_REFRESH_VIEW);
					// 2.ID学习反馈
				} else if (report[2] == 0x06) {
					switch (report[3]) {
					case 0x10:
						// Log.d(TAG, "开始ID学习:" + report[4]);
						break;
					case 0x18:
						// Log.d(TAG, "ID学习成功:" + report[4]);
						int index = CommUtil.getTireIndexByTireNum(report[4]);
						if (index > -1) {
							String data = getResources().getString(R.string.msg_tire_study_success);
							CommUtil.showTips(R.drawable.tips_smile, String.format(data, Constants.TIRE_NAMES[index]));
						}
						break;
					case 0x30:
						// Log.d(TAG, "轮胎互换成功:" + report[4]);
						// CommUtil.showTips(R.drawable.tips_smile,
						// R.string.msg_tire_switch_success);
						break;
					case 0x40:
						break;
					}
					// 查询四个发射器的ID结果
				} else if (report[2] == 0x07) {
					byte[] idByte = new byte[] { report[4], report[5], report[6] };
					// Log.d(TAG, "ID查询结果:" + report[3] + "'id=" +
					// CommUtil.byte2HexStr(idByte));
					int index = CommUtil.getTireIndexByTireNum(report[3]);
					if (index != -1) {
						tireDataArr[index].tireId = idByte;
					}
					mHandler.sendEmptyMessage(MSG_AUTO_REFRESH_VIEW);
				}
				parseData();
				break;
			}
		};
	};

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// Log.d(TAG, "action=" + action);
			if (BROADCAST_WRITE.equals(action)) {
				byte[] data = intent.getByteArrayExtra("data");
				mService.sendMsg(data);
			} else if (BROADCAST_UPDATE_SETTINGS.equals(action)) {
				updateSettings();
			} else if (BROADCAST_RECONNECT.equals(action)) {
				doConnect();
			} else if (BROADCAST_DISCONNECT.equals(action)) {
				disConnect();
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		resRunning = getString(R.string.msg_tpms_running);
		pref = getSharedPreferences(Constants.PREF_NAME_SETTING, Context.MODE_PRIVATE);
		bAdapter = BluetoothAdapter.getDefaultAdapter();// 蓝牙用
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);// 震动服务

		mService = new BTService(mHandler);
		
		tireNames = getResources().getStringArray(com.naruto.tpms.app.R.array.tires_names);

		// 设置前台运行
		mNotification = new Notification();
		mNotification.tickerText = resRunning;
		mNotification.icon = R.drawable.notify_icon;
		mNotification.defaults = Notification.DEFAULT_SOUND;
		contentView = new RemoteViews(getPackageName(), R.layout.notification_view);
		contentView.setImageViewResource(R.id.image, mNotification.icon);
		contentView.setTextColor(R.id.text, mNotification.icon == R.drawable.notify_icon ? Color.GREEN : Color.RED);
		contentView.setTextViewText(R.id.text, resRunning);
		mNotification.contentView = contentView;
		// if (Build.VERSION.SDK_INT >= 11) {
		// Bitmap bm = BitmapFactory.decodeResource(getResources(),
		// mNotification.icon);
		// mNotification = new
		// Notification.Builder(this).setContentTitle(mNotification.tickerText).setLargeIcon(bm).build();
		// }
		// mNotification.vibrate = new long[] { 0, 100, 300, 400 };
		// PendingIntent pendingIntent =
		// PendingIntent.getActivity(getApplicationContext(), 0, new
		// Intent(getApplicationContext(),
		// MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage(this.getPackageName());
		List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
		ResolveInfo ri = apps.iterator().next();
		Intent intent1 = null;
		if (ri != null) {
			String className = ri.activityInfo.name;
			intent1 = new Intent(Intent.ACTION_MAIN);
			intent1.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(this.getPackageName(), className);
			intent1.setComponent(cn);
		}
		contentIntent = PendingIntent.getActivity(this, 600, intent1, PendingIntent.FLAG_CANCEL_CURRENT);

		mNotification.contentIntent = contentIntent;
		// mNotification.setLatestEventInfo(getApplicationContext(),
		// getString(R.string.app_name), getString(R.string.msg_running),
		// contentIntent);
		// mNotificationManager.notify(FOREGROUND_ID, mNotification);
		startForeground(FOREGROUND_ID, mNotification);

		updateSettings();
		IntentFilter inf = new IntentFilter();
		inf.addAction(BROADCAST_WRITE);
		inf.addAction(BROADCAST_UPDATE_SETTINGS);
		inf.addAction(BROADCAST_RECONNECT);
		inf.addAction(BROADCAST_DISCONNECT);
		registerReceiver(myReceiver, inf);
		// 初始化轮胎对象
		for (int i = 0; i < tireDataArr.length; i++) {
			tireDataArr[i] = new TireData();
			tireDataArr[i].tireNum = Constants.TIRE_NUMS[i];
		}
		doConnect();
		acquireWakeLock();
	}

	/**
	 * onCreate时,申请设备电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	 */
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "myService");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	/**
	 * onDestroy时，释放设备电源锁
	 */
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		disConnect();
		unregisterReceiver(myReceiver);
		mHandler.removeMessages(MSG_AUTO_REFRESH_VIEW);
		MusicUtil.stop(this);
		vibrator.cancel();
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.cancel(FOREGROUND_ID);
		releaseWakeLock();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private synchronized void doConnect() {
//		Toast.makeText(this, "连接中...", Toast.LENGTH_LONG).show();
		String mac = pref.getString(Constants.SETTING_MAC, null);
		if (mac == null) {
			notifyConnectFail();
			// Toast.makeText(this, "no bind", Toast.LENGTH_SHORT).show();
			notifyToast(getString(R.string.msg_device_not_bind));
		} else if (!bAdapter.isEnabled()) {
			notifyConnectFail();
			// CommUtil.showTips(R.drawable.tips_error, "蓝牙未打开");
			notifyToast(getString(R.string.msg_bluetooth_closed));
			// Toast.makeText(this, "蓝牙未打开", Toast.LENGTH_SHORT).show();
		} else {
			// 启动连接
			if (mService != null) {
				switch (mService.getState()) {
				case BTService.STATE_CONNECTING:
					notifyConnecting();
					return;
				case BTService.STATE_CONNECTED:
					notifyConnected(device);
					return;
				default:
					mService.stop();
//					mService = null;
					break;
				}
			}
			device = bAdapter.getRemoteDevice(mac);
			mService.connect(device);
		}
	}

	private void disConnect() {
		if (mService != null) {
			mService.stop();
//			mService = null;
		}
	}

	private void updateSettings() {
		isAlarmOn = pref.getBoolean(Constants.SETTING_IS_ALARM_ON, true);

		highPressure = pref.getInt(Constants.SETTING_HIGH_PRESSURE, 5) * Constants.HIGH_PRESSURE_STEUP + Constants.HIGH_PRESSURE_MIN;
		lowPressure = pref.getInt(Constants.SETTING_LOW_PRESSURE, 0) * Constants.LOW_PRESSURE_STEUP + Constants.LOW_PRESSURE_MIN;
		highTemprature = pref.getInt(Constants.SETTING_HIGH_TEMPRATURE, 5) * Constants.HIGH_TEMPRATURE_STEUP
				+ Constants.HIGH_TEMPRATURE_MIN;
		// parseData();
	}

	/**
	 * 解析数据，产生告警
	 */
	protected void parseData() {
//		 Log.d("debug", "刷新数据");
		// 告警处理
		boolean isAnyAlarm = false;// 是否有告警信息
		int alarmCount = 0;
		StringBuilder alarmSb = new StringBuilder();
		alarmSb.append("(");
		for (int i = 0; i < tireDataArr.length; i++) {
			TireData td = tireDataArr[i];
			String alarmStr = null;
			if (td == null) {
				continue;
			}
			// 上报时间超过5秒则不处理
			if (System.currentTimeMillis() - td.lastUpdateTime <= 10000) {
				StringBuilder sb = new StringBuilder();
				if (td.isLeak) {
					sb.append(getString(R.string.alarm_leak));
					sb.append(",");
				}
				if (td.isLowBattery) {
					sb.append(getString(R.string.alarm_low_battery));
					sb.append(",");
				}
				if (td.isSignalError) {
					sb.append(getString(R.string.alarm_signal_error));
					sb.append(",");
				}
				if (td.pressure > highPressure) {
					sb.append(getString(R.string.alarm_high_pressure));
					sb.append(",");
				}
				if (td.pressure < lowPressure) {
					sb.append(getString(R.string.alarm_low_pressure));
					sb.append(",");
				}
				if (td.temperature > highTemprature) {
					sb.append(getString(R.string.alarm_high_temprature));
					sb.append(",");
				}
				if (sb.length() > 0) {
					alarmStr = tireNames[i] + ":" + sb.substring(0, sb.length() - 1);
				}
			}
			td.haveAlarm = (alarmStr != null);
			isAnyAlarm = isAnyAlarm || td.haveAlarm;
			alarmCount += td.haveAlarm ? 1 : 0;
			if (td.haveAlarm) {
				alarmSb.append(alarmStr);
				alarmSb.append(";");
			}
		}
		alarmSb.append(")");
		if (alarmCount != 0) {
			alarmSb.insert(0, alarmCount + " " + getString(R.string.msg_n_tire_alarming));
		}
		if (alarmCount == 0) {
			// Log.d(TAG, " 告警文字:" + alarmSb.toString());
		} else {
			// Log.d(TAG, "无告警");
		}
		if (isAnyAlarm) {
			notification(alarmSb.toString(), true);
		} else {
			notification(resRunning, false);
		}
		// sendWarnNotification(isAnyAlarm ? alarmSb.toString() : resRunning);
		// 如果有了警告信息且开启了警告
		if (isAnyAlarm && isAlarmOn) {
			if (!MusicUtil.isPlaying()) {
//				 Log.d(TAG, "开始告警");
				vibrator.vibrate(new long[] { 500, 1000 }, 0);
				MusicUtil.play(this, R.raw.alarm1);
			}
		} else {
			// 停止告警
			// Log.d(TAG, "停止告警");
			MusicUtil.stop(this);
			vibrator.cancel();
		}
	}

	private void notification(String text, boolean isWarn) {
		contentView.setImageViewResource(R.id.image, isWarn ? R.drawable.notify_icon_warn : R.drawable.notify_icon);
		contentView.setTextColor(R.id.text, isWarn ? Color.RED : Color.GREEN);
		contentView.setTextViewText(R.id.text, text);
		mNotification.icon = isWarn ? R.drawable.notify_icon_warn : R.drawable.notify_icon;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (!mNotification.tickerText.equals(text)) {
			mNotification.tickerText = text;
			mNotificationManager.notify(FOREGROUND_ID, mNotification);
		}
	}

	/**
	 * 通知已连接
	 */
	protected void notifyConnected(BluetoothDevice device) {
		Intent intent = new Intent(BROADCAST_CONNECTED);
		intent.putExtra("data", device);
		sendBroadcast(intent);
		// Log.d(TAG,"通知已连接");
	}

	protected void notifyConnecting() {
		Intent intent = new Intent(BROADCAST_CONNECTING);
		sendBroadcast(intent);
	}

	protected void notifyConnectFail() {
//		Log.d(TAG, "连接失败");
		notification(getString(R.string.msg_connect_fail), true);

		Intent intent = new Intent(BROADCAST_CONNECTFAIL);
		sendBroadcast(intent);
		// !resRunning.equals(alarmText)
	}

	protected void notifyConnectLost() {
//		Log.d(TAG, "连接丢失");
		notification(getString(R.string.msg_connect_lost), true);

		Intent intent = new Intent(BROADCAST_CONNECTLOST);
		sendBroadcast(intent);
	}

	protected void notifyRead(byte[] bytes) {
		Intent intent = new Intent(BROADCAST_READ);
		intent.putExtra("data", bytes);
		sendBroadcast(intent);
	}

	protected void notifyToast(String data) {
		notification(data, !resRunning.equals(data));

		Intent intent = new Intent(BROADCAST_TOAST);
		intent.putExtra("data", data);
		sendBroadcast(intent);
	}

}

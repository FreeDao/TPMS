package com.naruto.tpms.app.activity;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.naruto.tpms.app.CoreBtService;
import com.naruto.tpms.app.R;
import com.naruto.tpms.app.BTService;
import com.naruto.tpms.app.adapter.BluetoothDeviceAdapter;
import com.naruto.tpms.app.bean.DeviceInfo;
import com.naruto.tpms.app.comm.Commend;
import com.naruto.tpms.app.comm.Commend.TX;
import com.naruto.tpms.app.comm.util.CommUtil;
import com.naruto.tpms.app.comm.Constants;

public class ScanActivity extends BaseActivity implements OnItemClickListener {

	private BluetoothAdapter bAdapter = null;
	private BluetoothDeviceAdapter deviceAdapter;
	private BTService mService;
	private String temp_bt_name;
	private SharedPreferences pref;

	private Button btn_refresh;
	private ProgressBar pb_loading;
	private ProgressDialog pDialog;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case BTService.MSG_TOAST:
				break;
			case BTService.MSG_CONNECTED: {
				pDialog.setMessage(temp_bt_name + getString(R.string.msg_state_connected));
				CommUtil.showTips(R.drawable.tips_smile, R.string.msg_connect_success);
				pDialog.hide();
			}
				break;
			case BTService.MSG_CONNECTING: {
				pDialog.setMessage(temp_bt_name + getString(R.string.msg_state_connecting));
				pDialog.show();
			}
				break;
			case BTService.MSG_CONNECTED_FAIL: {
				CommUtil.showTips(R.drawable.tips_error, temp_bt_name + getString(R.string.msg_state_connect_fail));
				pDialog.cancel();
			}
				break;
			case BTService.MSG_CONNECTED_LOST: {
				CommUtil.showTips(R.drawable.tips_error, temp_bt_name + getString(R.string.msg_state_connect_lost));
				pDialog.cancel();
			}
				break;
			case BTService.MSG_WRITE:
				// str = temp_bt_name + "(发送闪烁指令成功)";
				break;
			case BTService.MSG_READ:
				// byte[] receiveMsg = (byte[]) msg.obj;
				// try {
				// str = new String(receiveMsg, "GBK");
				// } catch (UnsupportedEncodingException e) {
				// e.printStackTrace();
				// }
				break;
			}
		};

	};
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//				Log.d("tag", "发现:" + device.getName());
				DeviceInfo deviceInfo = new DeviceInfo();
				deviceInfo.device = device;
				// 信号强度。
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				deviceInfo.rssi = rssi;
				deviceInfo.isDiscovery = true;
				deviceAdapter.add(deviceInfo);
				// }
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				btn_refresh.setVisibility(View.VISIBLE);
				pb_loading.setVisibility(View.GONE);

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		initView();
		initData();
	}

	private void initView() {
		setTitle(R.string.bind_bluetooth);
		getTitleBar().findViewById(R.id.iv_title_left).setVisibility(View.GONE);
		getTitleBar().findViewById(R.id.iv_title_right).setVisibility(View.GONE);
		Button btn_title_left = (Button) getTitleBar().findViewById(R.id.btn_title_left);
		btn_title_left.setVisibility(View.VISIBLE);
		// btn_title_left.setBackgroundResource(R.drawable.btn_ok_bg);
		btn_title_left.setText(R.string.comm_back);
		Button btn_title_right = (Button) getTitleBar().findViewById(R.id.btn_title_right);
		btn_title_right.setVisibility(View.VISIBLE);
		// btn_title_right.setBackgroundResource(R.drawable.btn_ok_bg);
		btn_title_right.setText(R.string.comm_ok);

		btn_refresh = (Button) findViewById(R.id.btn_refresh);
		btn_refresh.setOnClickListener(this);
		pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
		ListView devicesList = (ListView) findViewById(R.id.list_devices);
		deviceAdapter = new BluetoothDeviceAdapter(this);
		devicesList.setOnItemClickListener(this);
		devicesList.setAdapter(deviceAdapter);
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(true);
		// 以下方法不会执行了,注意：4.2以下版本的机器上连接中的蓝牙关闭会挂掉。
		pDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d("tag", "dialog cancel");
				try {
					if (mService != null) {
						mService.stop();
					}
				} catch (Exception e) {
					Log.e("tag", e.getMessage());
				}
			}
		});
	}

	private void initData() {
		// 停止后台service中的连接
		sendBroadcast(new Intent(CoreBtService.BROADCAST_DISCONNECT));

		bAdapter = BluetoothAdapter.getDefaultAdapter();
		pref = getSharedPreferences(Constants.PREF_NAME_SETTING, Context.MODE_PRIVATE);
		String mac = pref.getString(Constants.SETTING_MAC, null);
		if (mac != null) {
			BluetoothDevice bindedDevice = bAdapter.getRemoteDevice(mac);
			deviceAdapter.setSelectDevice(bindedDevice);
		} else {
			deviceAdapter.setSelectDevice(null);
		}
		// 注册广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		// 进入时扫描
		doDiscovery();
	}

	@Override
	public void onClick(View v) {
		// super.onClick(v);
		switch (v.getId()) {
		case R.id.btn_title_left:
			finish();
			break;
		case R.id.btn_refresh:
			doDiscovery();
			break;
		case R.id.btn_title_right:
			BluetoothDevice bluetoothDevice = deviceAdapter.getSelectDevice();
			Editor editor = pref.edit();
			if (bluetoothDevice != null) {
				editor.putString(Constants.SETTING_MAC, bluetoothDevice.getAddress());
				editor.putString(Constants.SETTING_NAME, bluetoothDevice.getName());
			} else {
				editor.remove(Constants.SETTING_MAC);
				editor.remove(Constants.SETTING_NAME);
			}
			editor.commit();
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("tag", "onDestroy");
		if (bAdapter != null) {
			bAdapter.cancelDiscovery();
		}
		if (mService != null) {
			mService.stop();
		}
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * 开始搜索
	 */
	private void doDiscovery() {
		// 启动蓝牙
		if (!bAdapter.isEnabled()) {
			Intent cwj = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(cwj, 55);
//			bAdapter.enable();
		} else {
			startDiscovery();
		}
	}

	private void startDiscovery() {
		if (!bAdapter.isEnabled()) {
			return;
		}
		btn_refresh.setVisibility(View.GONE);
		pb_loading.setVisibility(View.VISIBLE);
		if (bAdapter.isDiscovering()) {
			bAdapter.cancelDiscovery();
		}
		deviceAdapter.clear();// 清空列表
		deviceAdapter.add(null);
		// 添加已绑定的设备
		Set<BluetoothDevice> bondedDevices = bAdapter.getBondedDevices();
		for (BluetoothDevice device : bondedDevices) {
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.device = device;
			deviceAdapter.add(deviceInfo);
		}
		bAdapter.startDiscovery();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		DeviceInfo dInfo = deviceAdapter.get(position);
		// 停止之前的连接
		if (mService != null) {
			mService.stop();
		}
		if (dInfo == null) {
			deviceAdapter.setSelectDevice(null);
		} else {
			pDialog.setMessage(temp_bt_name + getResources().getString(R.string.msg_connecting));
			pDialog.show();
			// ！！蓝牙在连接设备前会自动停止正在进行的扫描
			deviceAdapter.setSelectDevice(dInfo.device);
			deviceAdapter.notifyDataSetChanged();
			// 连接蓝牙发起闪烁
			temp_bt_name = dInfo.device.getName();
			mService = new BTService(mHandler);
			mService.connect(dInfo.device);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("test", "scan act,requestCode=" + requestCode + ",resultCode=" + resultCode);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case 55:
			startDiscovery();
			break;
		}
	}

}

package com.naruto.tpms.app.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.naruto.tpms.app.BTService;
import com.naruto.tpms.app.CoreBtService;
import com.naruto.tpms.app.R;
import com.naruto.tpms.app.comm.Commend;
import com.naruto.tpms.app.comm.Constants;
import com.naruto.tpms.app.comm.util.CommUtil;
import com.naruto.tpms.app.weight.LoadingDialog;

/**
 * 轮胎id学习/轮胎调换
 * 
 * @author Thinkman
 * 
 */
@SuppressLint("HandlerLeak")
public class EditActivity extends BaseActivity implements OnItemClickListener, OnCancelListener {

	public static final String EXTRA_MODE = "mode";

	public static final int MODE_STUDY = 11;
	public static final int MODE_SWITCH = 12;
	private int mode = MODE_STUDY;

	private int checkedId = -1;// 上一个选中轮胎所在布局的资源id
	private int checkedWhich = -1;// 上一个选中的轮胎的序号,依次是 0，1，2，3

	private int studyingWitch = -1;// 正在学习的轮胎序号

	private String preTitle;// 标题前缀
	private ProgressDialog pDialog;// 连接中的进度框
	private LoadingDialog mLoadingDialog;// 加载中...
	private TextView[] tv_tireIdArr = new TextView[4];// 显示id的几个textView
	private TextView[] tv_tireStateArr = new TextView[4];// 状态文字
	private RelativeLayout[] rl_boxArr = new RelativeLayout[4];//

	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case BTService.MSG_TOAST:
				CommUtil.showTips(R.drawable.tips_warning, String.valueOf(msg.obj));
				break;
			case BTService.MSG_CONNECTED: {
				setTitle(preTitle + getResources().getString(R.string.msg_state_connected));
				mLoadingDialog.dismiss();
				// 发送查询轮胎id的指令
				queryTireId();
				if (mode == MODE_STUDY) {
					CommUtil.showTips(R.drawable.tips_smile, R.string.tips_click_tire_start_study);
				} else if (mode == MODE_SWITCH) {
					CommUtil.showTips(R.drawable.tips_smile, R.string.tips_click_tire_start_switch);
				}
			}
				break;
			case BTService.MSG_CONNECTING: {
				setTitle(preTitle + getResources().getString(R.string.msg_state_connecting));
				mLoadingDialog.show();
				mLoadingDialog.setText(getResources().getString(R.string.msg_connecting));
			}
				break;
			case BTService.MSG_CONNECTED_FAIL: {
				setTitle(preTitle + getResources().getString(R.string.msg_state_connect_fail));
				CommUtil.showTips(R.drawable.tips_error, R.string.tips_connect_fail);
				mLoadingDialog.dismiss();
			}
				break;
			case BTService.MSG_CONNECTED_LOST: {
				setTitle(preTitle + getResources().getString(R.string.msg_state_connect_lost));
				mLoadingDialog.dismiss();
				CommUtil.showTips(R.drawable.tips_error, R.string.tips_connect_lost);
			}
				break;
			case BTService.MSG_WRITE:
				break;
			case BTService.MSG_READ:
				byte[] report = (byte[]) msg.obj;
				// Log.d("tag", "receved msg:" + Arrays.toString(report));
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
					byte checkByte = CommUtil.getCheckByte(report, 0, report.length - 2);
					if (checkByte != report[report.length - 1]) {
						// Log.w("tag", "校验位不正确,当前校验位=" + report[report.length -
						// 1] + ",正确校验位应=" + checkByte);
						return;
					}
				}

				// 解析消息
				// 状态上报 不处理
				if (report[2] == 0x08) {
					// ID学习反馈
				} else if (report[2] == 0x06) {
					switch (report[3]) {
					case 0x10:
						// Log.d("tag", "开始ID学习:" + report[4]);
						break;
					case 0x18:
						// Log.d("tag", "ID学习成功:" + report[4]);
						studyingWitch = -1;
						pDialog.dismiss();
						queryTireId();
						int index = CommUtil.getTireIndexByTireNum(report[4]);
						if (index > -1) {
							tv_tireStateArr[index].setText(R.string.msg_study_success);
							rl_boxArr[index].setBackgroundResource(R.drawable.edit_item_bg);
						}
						break;
					case 0x30:
						// Log.d("tag", "轮胎互换成功:" + report[4]);
						pDialog.dismiss();
						queryTireId();
						checkedId = -1;
						checkedWhich = -1;
						for (View v : rl_boxArr) {
							v.setBackgroundResource(R.drawable.edit_item_bg);
						}
						new AlertDialog.Builder(EditActivity.this).setMessage(R.string.msg_switch_success)
								.setNegativeButton(R.string.comm_ok, null).show();
						break;
					}
					// 查询四个发射器的ID结果
				} else if (report[2] == 0x07) {
					byte[] idByte = new byte[] { report[4], report[5], report[6] };
					int index = CommUtil.getTireIndexByTireNum(report[3]);
					// Log.d("tag", index + "   ID查询结果:" + report[3] + "'id=" +
					// CommUtil.byte2HexStr(idByte));
					index = report[3] - 1;
					if (index > -1 && index < 4) {
						tv_tireIdArr[index].setText(CommUtil.byte2HexStr(idByte));
					}
				}
				break;
			}

		};

	};

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 为了方便，这里发送消息直接使用了BTService的msg what
			if (CoreBtService.BROADCAST_CONNECTING.equals(action)) {
				Message msg = mHandler.obtainMessage(BTService.MSG_CONNECTING);
				mHandler.sendMessage(msg);
			} else if (CoreBtService.BROADCAST_CONNECTED.equals(action)) {
				Message msg = mHandler.obtainMessage(BTService.MSG_CONNECTED);
				BluetoothDevice device = (BluetoothDevice) msg.obj;
				msg.obj = device;
				mHandler.sendMessage(msg);
			} else if (CoreBtService.BROADCAST_CONNECTFAIL.equals(action)) {
				Message msg = mHandler.obtainMessage(BTService.MSG_CONNECTED_FAIL);
				mHandler.sendMessage(msg);
			} else if (CoreBtService.BROADCAST_CONNECTLOST.equals(action)) {
				Message msg = mHandler.obtainMessage(BTService.MSG_CONNECTED_LOST);
				mHandler.sendMessage(msg);
			} else if (CoreBtService.BROADCAST_READ.equals(action)) {
				byte[] data = intent.getByteArrayExtra("data");
				Message msg = mHandler.obtainMessage(BTService.MSG_READ);
				msg.obj = data;
				mHandler.sendMessage(msg);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.d("tag", "onCreate");
		setContentView(R.layout.activity_edit2);
		initView();
		initData();
	}

	@SuppressLint("UseSparseArrays")
	@Override
	protected void onResume() {
		super.onResume();
		// Log.d("tag", "onResume");
		//注册广播
		IntentFilter inf = new IntentFilter();
		inf.addAction(CoreBtService.BROADCAST_CONNECTED);
		inf.addAction(CoreBtService.BROADCAST_CONNECTING);
		inf.addAction(CoreBtService.BROADCAST_CONNECTFAIL);
		inf.addAction(CoreBtService.BROADCAST_CONNECTLOST);
		inf.addAction(CoreBtService.BROADCAST_READ);
		registerReceiver(myReceiver, inf);
		doConnect();
		queryTireId();
	}

	@Override
	protected void onStart() {
		// Log.d("tag", "onStart");
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (pDialog != null && pDialog.isShowing()) {
			pDialog.dismiss();
		}
		unregisterReceiver(myReceiver);
		// Log.d("tag", "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Log.d("tag", "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if (mode == MODE_STUDY) {
			// queryTireId();
			exitStudy();
		}
		super.onBackPressed();
	}

	@SuppressLint("UseSparseArrays")
	private void initData() {
		mode = getIntent().getIntExtra(EXTRA_MODE, MODE_STUDY);
		if (mode == MODE_STUDY) {
			preTitle = getResources().getString(R.string.mode_study);
		} else if (mode == MODE_SWITCH) {
			preTitle = getResources().getString(R.string.mode_switch);
		}
		pDialog = new ProgressDialog(this);
		pDialog.setOnCancelListener(this);
	}

	private void initView() {
		setTitle(R.string.app_summary);
		getTitleBar().findViewById(R.id.iv_title_left).setVisibility(View.GONE);
		Button btn_title_left = (Button) getTitleBar().findViewById(R.id.btn_title_left);
		btn_title_left.setVisibility(View.VISIBLE);
		btn_title_left.setText(R.string.comm_back);

		ImageView iv_title_right = (ImageView) getTitleBar().findViewById(R.id.iv_title_right);
		iv_title_right.setVisibility(View.GONE);
		// 轮胎区域
		rl_boxArr[0] = (RelativeLayout) findViewById(R.id.rl_left_front);
		rl_boxArr[1] = (RelativeLayout) findViewById(R.id.rl_right_front);
		rl_boxArr[2] = (RelativeLayout) findViewById(R.id.rl_left_rear);
		rl_boxArr[3] = (RelativeLayout) findViewById(R.id.rl_right_rear);
		// 轮胎id文字显示
		tv_tireIdArr[0] = (TextView) findViewById(R.id.tv_left_front_id);
		tv_tireIdArr[1] = (TextView) findViewById(R.id.tv_right_front_id);
		tv_tireIdArr[2] = (TextView) findViewById(R.id.tv_left_rear_id);
		tv_tireIdArr[3] = (TextView) findViewById(R.id.tv_right_rear_id);
		// 轮胎状态文字
		tv_tireStateArr[0] = (TextView) findViewById(R.id.tv_left_front_sate);
		tv_tireStateArr[1] = (TextView) findViewById(R.id.tv_right_front_state);
		tv_tireStateArr[2] = (TextView) findViewById(R.id.tv_left_rear_state);
		tv_tireStateArr[3] = (TextView) findViewById(R.id.tv_right_rear_state);

		mLoadingDialog = new LoadingDialog(this);
		mLoadingDialog.setCancelable(true);
	}

	private void doConnect() {
		startService(new Intent(this, CoreBtService.class));
		Intent intent = new Intent(CoreBtService.BROADCAST_RECONNECT);// 通知后台Service重新连接，如果已连接，则不做操作继续保持
		sendBroadcast(intent);
	}

	private void queryTireId() {
		Intent in = new Intent(CoreBtService.BROADCAST_WRITE);
		in.putExtra("data", Commend.TX.QUERY_TIRE_ID);
		sendBroadcast(in);
	}

	private void exitStudy() {
		Intent in = new Intent(CoreBtService.BROADCAST_WRITE);
		in.putExtra("data", Commend.TX.QUIT_STUDY);
		sendBroadcast(in);
	}

	private void reConnect() {
		Intent in = new Intent(CoreBtService.BROADCAST_RECONNECT);
		sendBroadcast(in);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_bar:
			reConnect();
			break;
		case R.id.iv_title_left:
		case R.id.btn_title_left: {
			if (mode == MODE_STUDY) {
				// queryTireId();
				exitStudy();
			}
			finish();
		}
			break;
		case R.id.rl_left_front:
			if (mode == MODE_STUDY) {
				doStudy(0);
			} else if (mode == MODE_SWITCH) {
				doSwitch(v.getId(), 0);
			}
			break;
		case R.id.rl_right_front:
			if (mode == MODE_STUDY) {
				doStudy(1);
			} else if (mode == MODE_SWITCH) {
				doSwitch(v.getId(), 1);
			}
			break;
		case R.id.rl_left_rear:
			if (mode == MODE_STUDY) {
				doStudy(2);
			} else if (mode == MODE_SWITCH) {
				doSwitch(v.getId(), 2);
			}
			break;
		case R.id.rl_right_rear:
			if (mode == MODE_STUDY) {
				doStudy(3);
			} else if (mode == MODE_SWITCH) {
				doSwitch(v.getId(), 3);
			}
			break;
		}
	}

	private void doStudy(int which) {
		exitStudy();
		for (TextView tv : tv_tireStateArr) {
			tv.setText(null);
		}
		for (View v : rl_boxArr) {
			v.setBackgroundResource(R.drawable.edit_item_bg);
		}
		if (studyingWitch == which) {
			studyingWitch = -1;
			tv_tireStateArr[which].setText(R.string.msg_study_cancel);
			return;
		}
		byte[] cmd = new byte[] { 0x55, (byte) 0xAA, 0x06, 0x01, Constants.TIRE_NUMS[which], 0x00 };
		cmd[cmd.length - 1] = CommUtil.getCheckByte(cmd, 0, cmd.length - 2);
		// if (mService.sendMsg(cmd)) {
		Intent in = new Intent(CoreBtService.BROADCAST_WRITE);
		in.putExtra("data", cmd);
		sendBroadcast(in);
		tv_tireStateArr[which].setText(R.string.msg_studying);
		studyingWitch = which;
		// } else {
		// tv_tireStateArr[which].setText(R.string.msg_study_command_send_fail);
		// }
		rl_boxArr[which].setBackgroundColor(0xFF0000FF);
	}

	private void doSwitch(final int resId, final int newCheckedWitch) {
		if (checkedId == -1) {
			checkedId = resId;
			checkedWhich = newCheckedWitch;
			findViewById(resId).setBackgroundResource(R.drawable.edit_item_checked_bg);
		} else if (checkedId == resId) {
			findViewById(resId).setBackgroundResource(R.drawable.edit_item_bg);
			checkedId = -1;
			checkedWhich = -1;
		} else {
			findViewById(resId).setBackgroundResource(R.drawable.edit_item_checked_bg);
			String nameA = Constants.TIRE_NAMES[checkedWhich];
			String nameB = Constants.TIRE_NAMES[newCheckedWitch];
			String title = String.format(getString(R.string.msg_want_switch), nameA, nameB);
			new AlertDialog.Builder(EditActivity.this).setTitle(title).setNegativeButton(R.string.comm_cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					findViewById(checkedId).setBackgroundResource(R.drawable.edit_item_bg);
					findViewById(resId).setBackgroundResource(R.drawable.edit_item_bg);
					checkedId = -1;
					checkedWhich = -1;
				}
			}).setPositiveButton(R.string.comm_ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					pDialog.setMessage(EditActivity.this.getResources().getString(R.string.msg_switching));
					pDialog.show();
					byte mCmd = -1;
					// 发送交换命令
					if ((checkedWhich == 0 && newCheckedWitch == 1) || (checkedWhich == 1 && newCheckedWitch == 0)) {
						mCmd = 0x00;
					} else if ((checkedWhich == 0 && newCheckedWitch == 2) || (checkedWhich == 2 && newCheckedWitch == 0)) {
						mCmd = 0x01;
					} else if ((checkedWhich == 0 && newCheckedWitch == 3) || (checkedWhich == 3 && newCheckedWitch == 0)) {
						mCmd = 0x02;
					} else if ((checkedWhich == 1 && newCheckedWitch == 2) || (checkedWhich == 2 && newCheckedWitch == 1)) {
						mCmd = 0x03;
					} else if ((checkedWhich == 1 && newCheckedWitch == 3) || (checkedWhich == 3 && newCheckedWitch == 1)) {
						mCmd = 0x04;
					} else if ((checkedWhich == 2 && newCheckedWitch == 3) || (checkedWhich == 3 && newCheckedWitch == 2)) {
						mCmd = 0x05;
					}
					byte[] cmd = new byte[] { 0x55, (byte) 0xAA, 0x06, 0x03, mCmd, 0x00 };
					cmd[cmd.length - 1] = CommUtil.getCheckByte(cmd, 0, cmd.length - 2);
					Intent in = new Intent(CoreBtService.BROADCAST_WRITE);
					in.putExtra("data", cmd);
					sendBroadcast(in);
				}
			}).setCancelable(false).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	}

	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// if (dialog == pDialog) {
	// byte[] cmd = new byte[] { 0x55, (byte) 0xAA, 0x06, 0x06, 0x00, (byte)
	// 0xff };
	// Intent in = new Intent(CoreBtService.BROADCAST_WRITE);
	// in.putExtra("data", cmd);
	// sendBroadcast(in);
	// // Log.d("tag", "取消学习时校验位:" + CommUtil.getCheckByte(new byte[] {
	// // 0x55, (byte) 0xAA, 0x06, 0x06, 0x00, (byte) 0xff }, 0, 4));
	// }
	// }

	@Override
	public void onCancel(DialogInterface arg0) {
		pDialog.dismiss();
		queryTireId();
		checkedId = -1;
		checkedWhich = -1;
		for (View v : rl_boxArr) {
			v.setBackgroundResource(R.drawable.edit_item_bg);
		}
	}

}

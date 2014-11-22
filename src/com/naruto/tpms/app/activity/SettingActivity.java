package com.naruto.tpms.app.activity;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.naruto.tpms.app.CoreBtService;
import com.naruto.tpms.app.R;
import com.naruto.tpms.app.comm.AppManager;
import com.naruto.tpms.app.comm.Constants;
import com.naruto.tpms.app.comm.util.CommUtil;
import com.naruto.tpms.app.weight.MySeekBar;
import com.naruto.tpms.app.weight.SwitchButton;

public class SettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener,
		android.widget.CompoundButton.OnCheckedChangeListener {

	private SharedPreferences sharedPreferences;
	private TextView tv_bluetooth_name, tv_hp, tv_lp, tv_ht;
	private MySeekBar sb_high_pressure, sb_low_pressure, sb_high_temprature;

	int pressureUnitIndex, temperatureUnitIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("tag", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		String mac = sharedPreferences.getString(Constants.SETTING_MAC, null);
		String name = sharedPreferences.getString(Constants.SETTING_NAME, null);
		if (TextUtils.isEmpty(mac) || TextUtils.isEmpty(name)) {
			tv_bluetooth_name.setText(R.string.no_bluetooth_bind);
		} else {
			tv_bluetooth_name.setText(name);
		}
		updateSettings();
	}

	private void initView() {
		setTitle(R.string.action_settings);
		Button btn_title_left = (Button) getTitleBar().findViewById(R.id.btn_title_left);
		btn_title_left.setText(R.string.comm_back);
		btn_title_left.setVisibility(View.VISIBLE);

		sharedPreferences = getSharedPreferences(Constants.PREF_NAME_SETTING, Context.MODE_PRIVATE);

		tv_bluetooth_name = (TextView) findViewById(R.id.tv_bluetooth_name);

		// int local_position =
		// sharedPreferences.getInt(Constants.SETTING_LOCAL, 0);
		// String[] local_names =
		// getResources().getStringArray(R.array.local_names);
		// TextView tv_local_name = (TextView) findViewById(R.id.tv_local_name);
		// tv_local_name.setText(local_names[local_position]);

		tv_hp = (TextView) findViewById(R.id.tv_hp);
		tv_lp = (TextView) findViewById(R.id.tv_lp);
		tv_ht = (TextView) findViewById(R.id.tv_ht);

		RadioGroup rg_pressure_unit = (RadioGroup) findViewById(R.id.rg_pressure_unit);
		RadioGroup rg_temperature_unit = (RadioGroup) findViewById(R.id.rg_temperature_unit);
		pressureUnitIndex = sharedPreferences.getInt(Constants.SETTING_PRESSURE_UNIT, 1);
		RadioButton rb_p = (RadioButton) rg_pressure_unit.getChildAt(pressureUnitIndex);
		rb_p.setChecked(true);
		temperatureUnitIndex = sharedPreferences.getInt(Constants.SETTING_TEMPERATURE_UNIT, 0);
		RadioButton rb = (RadioButton) rg_temperature_unit.getChildAt(temperatureUnitIndex);
		rb.setChecked(true);
		rg_pressure_unit.setOnCheckedChangeListener(this);
		rg_temperature_unit.setOnCheckedChangeListener(this);

		// 压力上限值
		sb_high_pressure = (MySeekBar) findViewById(R.id.sb_high_pressure);
		int max = (int) ((Constants.HIGH_PRESSURE_MAX - Constants.HIGH_PRESSURE_MIN) / Constants.HIGH_PRESSURE_STEUP);
		// sb_high_pressure.setMax(max);
		// sb_high_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_HIGH_PRESSURE,
		// 5));
		// sb_high_pressure.setProgress(0);
		sb_high_pressure.setMax(max);
		sb_high_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_HIGH_PRESSURE, 5));
		onProgressChanged(sb_high_pressure, sharedPreferences.getInt(Constants.SETTING_HIGH_PRESSURE, 5), false);
		// sb_high_pressure.setProgress(0);
		// sb_high_pressure.setMax(max);
		// sb_high_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_HIGH_PRESSURE,
		// 5));
		// sb_high_pressure.updateThumb();
		sb_high_pressure.setOnSeekBarChangeListener(this);
		// sb_high_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_HIGH_PRESSURE,
		// 5));
		// sb_high_pressure.invalidate();
		// 压力下限值
		sb_low_pressure = (MySeekBar) findViewById(R.id.sb_low_pressure);
		// sb_low_pressure.setOnSeekBarChangeListener(this);
		max = (int) ((Constants.LOW_PRESSURE_MAX - Constants.LOW_PRESSURE_MIN) / Constants.LOW_PRESSURE_STEUP);
		sb_low_pressure.setMax(max);
		sb_low_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_LOW_PRESSURE, 0));
		onProgressChanged(sb_low_pressure, sb_low_pressure.getProgress(), false);
		sb_low_pressure.setOnSeekBarChangeListener(this);
		// sb_low_pressure.setProgress(0);
		// sb_low_pressure.setMax(max);
		// sb_low_pressure.setProgress(sharedPreferences.getInt(Constants.SETTING_LOW_PRESSURE,
		// 0));
		// sb_low_pressure.updateThumb();
		// 温度上限值
		sb_high_temprature = (MySeekBar) findViewById(R.id.sb_high_temprature);
		// sb_high_temprature.setOnSeekBarChangeListener(this);
		max = (int) ((Constants.HIGH_TEMPRATURE_MAX - Constants.HIGH_TEMPRATURE_MIN) / Constants.HIGH_TEMPRATURE_STEUP);
		sb_high_temprature.setMax(max);
		sb_high_temprature.setProgress(sharedPreferences.getInt(Constants.SETTING_HIGH_TEMPRATURE, 5));
		onProgressChanged(sb_high_temprature, sb_high_temprature.getProgress(), false);
		sb_high_temprature.setOnSeekBarChangeListener(this);
		// sb_high_temprature.setMax(max);
		// sb_high_temprature.updateThumb();

		SwitchButton sb_alarm_trun = (SwitchButton) findViewById(R.id.sb_alarm_trun);
		SwitchButton sb_screen_trun = (SwitchButton) findViewById(R.id.sb_screen_trun);
		SwitchButton sb_auto_boot = (SwitchButton) findViewById(R.id.sb_auto_boot);
		sb_alarm_trun.setOnCheckedChangeListener(this);
		sb_screen_trun.setOnCheckedChangeListener(this);
		sb_alarm_trun.setChecked(sharedPreferences.getBoolean(Constants.SETTING_IS_ALARM_ON, true));
		sb_screen_trun.setChecked(sharedPreferences.getBoolean(Constants.SETTING_IS_SCREEN_KEEP, true));
		sb_auto_boot.setChecked(sharedPreferences.getBoolean(Constants.SETTING_IS_AUTO_BOOT, true));
		sb_auto_boot.setOnCheckedChangeListener(this);
		// findViewById(R.id.ll_alarm_value_box).setVisibility(sb_alarm_trun.isChecked()
		// ? View.VISIBLE : View.GONE);

	}

	private void initData() {
	}

	private void updateSettings() {
		Intent intent = new Intent(CoreBtService.BROADCAST_UPDATE_SETTINGS);
		sendBroadcast(intent);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.rl_bind_bluetooth:
			Intent intent = new Intent(this, ScanActivity.class);
			startActivity(intent);
			break;
		// case R.id.rl_language_setting:
		// int locale = sharedPreferences.getInt(Constants.SETTING_LOCAL, 0);
		// String[] local_names =
		// getResources().getStringArray(R.array.local_names);
		// AlertDialog ad = new
		// AlertDialog.Builder(this).setTitle(R.string.setting_language)
		// .setSingleChoiceItems(local_names, locale, this).create();
		// ad.show();
		// break;
		}
	}

	@Override
	protected void onDestroy() {
		Log.d("tag", "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("tag", "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onClick(DialogInterface dialog, int position) {
		int lastSetting = sharedPreferences.getInt(Constants.SETTING_LOCAL, 0);
		// 跟上次设置的没变化
		if (lastSetting == position) {
			dialog.dismiss();
			return;
		}
		// 保存语言设置
		sharedPreferences.edit().putInt(Constants.SETTING_LOCAL, position).commit();
		// 切换语言
		Locale locale = Constants.LOCALE_ARR[position];
		// if (position == 0) {
		// locale = Locale.getDefault();
		// } else {
		// String[] local_code_arr =
		// getResources().getStringArray(R.array.local_code);
		// String local_str = local_code_arr[position];
		// locale = new Locale(local_str);
		// if("zh-rTW".equals(local_str)){
		// locale = Locale.TAIWAN;
		// Log.d("tag",""+locale.getCountry()+","+locale.getDisplayLanguage()+"，"+locale.getDisplayName());
		// }
		// }
		Resources r = getApplicationContext().getResources();
		Configuration cf = r.getConfiguration();
		cf.locale = locale;
		DisplayMetrics dm = r.getDisplayMetrics();
		r.updateConfiguration(cf, dm);
		r.updateConfiguration(cf, dm);
		// 重启主界面
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		AppManager.getAppManager().finishAllActivity();
		updateSettings();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (group.getId()) {
		case R.id.rg_pressure_unit: {
			switch (checkedId) {
			case R.id.rb_pressure_psi:
				pressureUnitIndex = 0;
				break;
			case R.id.rb_pressure_kpa:
				pressureUnitIndex = 1;
				break;
			case R.id.rb_pressure_bar:
				pressureUnitIndex = 2;
				break;
			default:
				return;
			}
			sharedPreferences.edit().putInt(Constants.SETTING_PRESSURE_UNIT, pressureUnitIndex).commit();
			// 换了单位后,压力上下限的显示值也转换
			tv_hp.setText(CommUtil.getPressureWithUnit(Constants.HIGH_PRESSURE_MIN + sb_high_pressure.getProgress()
					* Constants.HIGH_PRESSURE_STEUP, pressureUnitIndex));
			tv_lp.setText(CommUtil.getPressureWithUnit(Constants.LOW_PRESSURE_MIN + sb_low_pressure.getProgress()
					* Constants.LOW_PRESSURE_STEUP, pressureUnitIndex));
		}
			break;
		case R.id.rg_temperature_unit: {
			switch (checkedId) {
			case R.id.rb_temperature_c:
				temperatureUnitIndex = 0;
				break;
			case R.id.rb_temperature_f:
				temperatureUnitIndex = 1;
				break;
			default:
				return;
			}
			sharedPreferences.edit().putInt(Constants.SETTING_TEMPERATURE_UNIT, temperatureUnitIndex).commit();
			tv_ht.setText(CommUtil.getTemperatureWithUnit(Constants.HIGH_TEMPRATURE_MIN + sb_high_temprature.getProgress()
					* Constants.HIGH_TEMPRATURE_STEUP, temperatureUnitIndex));
		}
			break;
		}
		updateSettings();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.sb_high_pressure:
			sharedPreferences.edit().putInt(Constants.SETTING_HIGH_PRESSURE, progress).commit();
			tv_hp.setText(CommUtil.getPressureWithUnit(Constants.HIGH_PRESSURE_MIN + progress * Constants.HIGH_PRESSURE_STEUP,
					pressureUnitIndex));
			break;
		case R.id.sb_low_pressure:
			sharedPreferences.edit().putInt(Constants.SETTING_LOW_PRESSURE, progress).commit();
			tv_lp.setText(CommUtil.getPressureWithUnit(Constants.LOW_PRESSURE_MIN + sb_low_pressure.getProgress()
					* Constants.LOW_PRESSURE_STEUP, pressureUnitIndex));
			break;
		case R.id.sb_high_temprature:
			sharedPreferences.edit().putInt(Constants.SETTING_HIGH_TEMPRATURE, progress).commit();
			tv_ht.setText(CommUtil.getTemperatureWithUnit(Constants.HIGH_TEMPRATURE_MIN + sb_high_temprature.getProgress()
					* Constants.HIGH_TEMPRATURE_STEUP, temperatureUnitIndex));
			break;
		}
		updateSettings();
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.sb_alarm_trun) {
			sharedPreferences.edit().putBoolean(Constants.SETTING_IS_ALARM_ON, isChecked).commit();
			// findViewById(R.id.ll_alarm_value_box).setVisibility(isChecked ?
			// View.VISIBLE : View.GONE);
		} else if (buttonView.getId() == R.id.sb_screen_trun) {
			sharedPreferences.edit().putBoolean(Constants.SETTING_IS_SCREEN_KEEP, isChecked).commit();
		} else if (buttonView.getId() == R.id.sb_auto_boot) {
			sharedPreferences.edit().putBoolean(Constants.SETTING_IS_AUTO_BOOT, isChecked).commit();
		}
		updateSettings();
	}
}

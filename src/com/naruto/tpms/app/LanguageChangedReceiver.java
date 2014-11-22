package com.naruto.tpms.app;

import com.naruto.tpms.app.activity.SettingActivity;
import com.naruto.tpms.app.activity.WelcomeActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 语言切换后重启当前页面
 * @author Thinkman
 *
 */
public class LanguageChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent it = new Intent(context, WelcomeActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 这个必须加
		context.startActivity(it);
	}

}
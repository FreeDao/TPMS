package com.naruto.tpms.app.activity;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import com.naruto.tpms.app.R;

public class WelcomeActivity extends Activity {
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		View logo = findViewById(R.id.logo_blaupunkt);
		AnimationSet zoom_in = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.zoom_in);
		logo.startAnimation(zoom_in);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(1);
			}
		}).start();

	}

	/**
	 * 授权期限检查
	 */
	private void checkDate() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				final Calendar lisensTime = Calendar.getInstance();
				lisensTime.set(Calendar.YEAR, 2013);
				lisensTime.set(Calendar.MONTH, 12);
				lisensTime.set(Calendar.DATE, 28);
				final Calendar now = Calendar.getInstance();
				if (now.after(lisensTime)) {
					throw new NullPointerException("crash");
				}
			}
		});
		thread.start();
	}
}

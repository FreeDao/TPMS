package com.naruto.tpms.app.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.naruto.tpms.app.R;
import com.naruto.tpms.app.comm.AppManager;

public class BaseActivity extends Activity implements OnClickListener {
	private View title_bar;
	private TextView tv_title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.comm_title_bar);// 自定义布局赋值
		title_bar = findViewById(R.id.title_bar);
		tv_title = (TextView) findViewById(R.id.title);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		tv_title.setText(title);
	}

	public View getTitleBar() {
		return title_bar;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_title_left:
		case R.id.iv_title_left:
			finish();
			break;
		}
	}
}

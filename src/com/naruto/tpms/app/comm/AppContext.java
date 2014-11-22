package com.naruto.tpms.app.comm;

import android.app.Application;

/**
 * 
 * 
 * @author xm
 * 
 */
public class AppContext extends Application {
	private static AppContext instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static AppContext getInstance() {
		return instance;
	}
}

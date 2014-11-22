package com.naruto.tpms.app.weight;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class MySeekBar extends SeekBar {

	public MySeekBar(Context context) {
		super(context);
	}

	public MySeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MySeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		updateThumb();
	}

	public void updateThumb() {
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
}

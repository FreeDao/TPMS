package com.naruto.tpms.app.adapter;

import java.nio.channels.AlreadyConnectedException;

import com.naruto.tpms.app.R;
import com.naruto.tpms.app.bean.DeviceBind;
import com.naruto.tpms.app.bean.TireData;
import com.naruto.tpms.app.comm.Constants;
import com.naruto.tpms.app.comm.util.CommUtil;
import com.naruto.tpms.app.weight.MarqueeText;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * 监控模式适配器
 * 
 * @author Thinkman
 * 
 */
public class TireWatchAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private TireData[] tireDataArr;
	private GridView mGridView;

	public int pressureUnitIndex, temperatureUnitIndex;
	public double highPressure, lowPressure, highTemprature;

	Drawable grid_item_transparent_bg, grid_item_alarm_bg;

	/**
	 * 屏幕方向
	 */
	private int orientation = Configuration.ORIENTATION_PORTRAIT;

	public TireWatchAdapter(Context context, GridView mGridView, TireData[] tireDataArr) {
		layoutInflater = LayoutInflater.from(context);
		this.mGridView = mGridView;
		this.tireDataArr = tireDataArr;
		grid_item_transparent_bg = context.getResources().getDrawable(R.drawable.grid_item_transparent_bg);
		grid_item_alarm_bg = context.getResources().getDrawable(R.drawable.grid_item_alarm_bg);
	} 
	@Override
	public int getCount() {
		return tireDataArr.length;
	}

	@Override
	public Object getItem(int position) {
		return tireDataArr[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return position % 2 == 0 ? 0 : 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.d("tag", "position=" + position + ",convertView=" + convertView);
		ViewHolder holder;
		if (convertView == null) {
			if (position % 2 == 0) {
				convertView = layoutInflater.inflate(R.layout.item_tire_watch_left, null);
			} else {
				convertView = layoutInflater.inflate(R.layout.item_tire_watch_right, null);
			}
			holder = new ViewHolder();
			holder.tv_tire_id = (TextView) convertView.findViewById(R.id.tv_tire_id);
			holder.tv_tire_pressure = (TextView) convertView.findViewById(R.id.tv_tire_pressure);
			holder.tv_pressure_unit = (TextView) convertView.findViewById(R.id.tv_pressure_unit);
			holder.tv_tire_temprature = (TextView) convertView.findViewById(R.id.tv_tire_temprature);
			holder.tv_tire_alarm = (TextView) convertView.findViewById(R.id.tv_tire_alarm);
			convertView.setTag(holder);
//			Log.d("debug", "create View item");
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		int itemHeight = mGridView.getHeight() / 2;
		if (convertView.getHeight() != itemHeight) {
//			Log.d("debug", "重置高度:" + position + "," + convertView + " = " + convertView.getHeight());
			AbsListView.LayoutParams param = new AbsListView.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					mGridView.getHeight() / 2);
			convertView.setLayoutParams(param);
			convertView.requestLayout();
		}
		// 填充数据
		TireData tData = tireDataArr[position];
		String pressureStr = null, tempratureStr = null, pressurUnit = null;
		if (tData != null && (System.currentTimeMillis() - tData.lastUpdateTime < 10000)) {
			// tireIdStr = tData.tireId == null ? null :
			// CommUtil.byte2HexStr(tData.tireId);
			pressureStr = CommUtil.getPressure(tData.pressure, pressureUnitIndex);
			pressurUnit = CommUtil.pressureUnits[pressureUnitIndex];
			tempratureStr = CommUtil.getTemperatureWithUnit(tData.temperature, temperatureUnitIndex);
		}
		// holder.tv_tire_id.setText(tireIdStr);
		// int pressureTextSpSize = orientation ==
		// Configuration.ORIENTATION_LANDSCAPE ? 60 : 50;
		// if (pressureUnitIndex == 1) {
		// } else {
		// // holder.tv_tire_pressure.setTextSize(TypedValue.COMPLEX_UNIT_SP,
		// // 70);
		// }
		// holder.tv_tire_pressure.setTextSize(TypedValue.COMPLEX_UNIT_SP,
		// pressureTextSpSize);
//		Log.d("debug","pressureStr="+pressureStr);
		holder.tv_tire_pressure.setText(pressureStr);
		holder.tv_pressure_unit.setText(pressurUnit);
		holder.tv_tire_temprature.setText(tempratureStr);
		// holder.tv_tire_alarm.setVisibility(View.VISIBLE);
		// alarmStr = "test marqee  text ,on  fuck  ,what's wrong?";
		if (tData == null || !tData.haveAlarm) {
//			Log.d("debug","无警告beijing"+position);
			convertView.setBackgroundDrawable(grid_item_transparent_bg);
		} else {
//			Log.d("debug","有警告beijing"+position);
			convertView.setBackgroundDrawable(grid_item_alarm_bg);
		}
		return convertView;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	private class ViewHolder {
		TextView tv_tire_id, tv_tire_pressure, tv_pressure_unit, tv_tire_temprature, tv_tire_alarm;
	}
}

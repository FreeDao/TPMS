package com.naruto.tpms.app.adapter;

import java.nio.channels.AlreadyConnectedException;

import com.naruto.tpms.app.R;
import com.naruto.tpms.app.bean.DeviceBind;
import com.naruto.tpms.app.bean.TireData;
import com.naruto.tpms.app.comm.util.CommUtil;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

@Deprecated
public class TireDataAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private TireData[] tireDataArr;
	private GridView mGridView;

	private int pressureUnitIndex;
	private int temperatureUnitIndex;
	private double highPressure, lowPressure, highTemprature;

	public TireDataAdapter(Context context, GridView mGridView, TireData[] tireDataArr) {
		layoutInflater = LayoutInflater.from(context);
		this.mGridView = mGridView;
		this.tireDataArr = tireDataArr;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("tag","position="+position+",convertView="+convertView);
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.item_grid, null);
			holder = new ViewHolder();
			holder.tv_tire_id = (TextView) convertView.findViewById(R.id.tv_tire_id);
			holder.tv_tire_pressure = (TextView) convertView.findViewById(R.id.tv_tire_pressure);
			holder.tv_tire_temprature = (TextView) convertView.findViewById(R.id.tv_tire_temprature);
			holder.tv_tire_alarm = (TextView) convertView.findViewById(R.id.tv_tire_alarm);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		AbsListView.LayoutParams param = new AbsListView.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				mGridView.getHeight() / 2);
		convertView.setLayoutParams(param);
		// 填充数据
		TireData tData = tireDataArr[position];
		String tireIdStr = null, pressureStr = null, tempratureStr = null, alarmStr = null;
		if (tData != null) {
			tireIdStr = tData.tireId == null ? null : CommUtil.byte2HexStr(tData.tireId);
			if (System.currentTimeMillis() - tData.lastUpdateTime < 10000) {
				pressureStr = CommUtil.getPressure( tData.pressure, pressureUnitIndex);
				tempratureStr = CommUtil.getTemperatureWithUnit( tData.temperature , temperatureUnitIndex);
				StringBuilder sb = new StringBuilder();
				if (tData.isLeak) {
					sb.append("泄漏,");
				}
				if (tData.isLowBattery) {
					sb.append("传感器电力较低,");
				}
				if (tData.isSignalError) {
					sb.append("信号错误,");
				}
				if (3.44d * tData.pressure > highPressure) {
					sb.append("气压超过设定上限,");
				}
				if (3.44d * tData.pressure < lowPressure) {
					sb.append("气压超过设定下限,");
				}
				if (3.44d * tData.temperature > highTemprature) {
					sb.append("温度超过设定上限,");
				}
				if (sb.length() > 0) {
					alarmStr = sb.substring(0, sb.length() - 1);
				}
			}
		}
		holder.tv_tire_id.setText(tireIdStr);
		holder.tv_tire_pressure.setText(pressureStr);
		holder.tv_tire_temprature.setText(tempratureStr);
		holder.tv_tire_alarm.setText(alarmStr);
		if (alarmStr != null) {
//			final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(R.color.transparent),
//					new ColorDrawable(R.color.red) });
//			td.startTransition(200);
			convertView.setBackgroundResource(R.drawable.grid_item_alarm_bg);
		}else{
			convertView.setBackgroundResource(R.drawable.grid_item_transparent_bg);
		}
		return convertView;
	}

	private class ViewHolder {
		TextView tv_tire_id, tv_tire_pressure, tv_tire_temprature, tv_tire_alarm;
	}
}

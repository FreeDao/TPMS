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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

@Deprecated
public class TireEditAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private GridView mGridView;


	public TireEditAdapter(Context context, GridView mGridView, TireData[] tireDataArr) {
		layoutInflater = LayoutInflater.from(context);
		this.mGridView = mGridView;
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
		// Ìî³äÊý¾Ý
		return convertView;
	}

	private class ViewHolder {
		TextView tv_tire_id, tv_tire_pressure, tv_tire_temprature, tv_tire_alarm;
	}
}

package com.naruto.tpms.app.adapter;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.naruto.tpms.app.R;
import com.naruto.tpms.app.bean.DeviceInfo;

/**
 * 蓝牙设备列表适配器
 * 
 * @author Thinkman 415071574@qq.com
 */
public class BluetoothDeviceAdapter extends BaseAdapter {

	private Set<DeviceInfo> deviceSet = new LinkedHashSet<DeviceInfo>();
	private LayoutInflater inflater;
	private Context ctx;
	private  Resources  res;

	private BluetoothDevice selectDevice = null;

	public BluetoothDeviceAdapter(Context ctx) {
		this.ctx = ctx;
		res = ctx.getResources();
		inflater = LayoutInflater.from(ctx);
	}

	public synchronized void add(DeviceInfo device) {
		if (deviceSet.contains(device)) {
			Log.d("tag", "update device:" + device.device.getAddress());
			Iterator<DeviceInfo> it = deviceSet.iterator();
			while (it.hasNext()) {
				DeviceInfo di = it.next();
				if (device != null && device.equals(di)) {
					di.device = device.device;
					di.isDiscovery = device.isDiscovery;
					di.rssi = device.rssi;
					Log.d("tag", "update device OK");
					break;
				}
			}
			Log.d("tag", "notifyDataSetChanged");
			notifyDataSetChanged();
		} else {
			Log.d("tag", "add device:" + (device != null ? device.device.getAddress() : "NULL"));
			deviceSet.add(device);
			notifyDataSetChanged();
		}
	}

	public synchronized void clear() {
		deviceSet.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		DeviceInfo[] arr = new DeviceInfo[deviceSet.size()];
		DeviceInfo bd = deviceSet.toArray(arr)[position];
		if (bd == null) {
			return 0;// 这个返回值一定是从0开始的！！！
		} else {
			return 1;
		}
	}

	public DeviceInfo get(int position) {
		DeviceInfo[] arr = new DeviceInfo[deviceSet.size()];
		return deviceSet.toArray(arr)[position];
	}

	@Override
	public int getCount() {
		return deviceSet.size();
	}

	@Override
	public Object getItem(int position) {
		DeviceInfo[] arr = new DeviceInfo[deviceSet.size()];
		return deviceSet.toArray(arr)[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		DeviceInfo[] arr = new DeviceInfo[deviceSet.size()];
		DeviceInfo bd = deviceSet.toArray(arr)[position];
		if (convertView == null) {
			holder = new ViewHolder();
			if (bd == null) {
				convertView = inflater.inflate(R.layout.item_device_list_unbind, null);
				holder.radioBtn = (RadioButton) convertView.findViewById(R.id.rb_select);
				holder.name = (TextView) convertView.findViewById(R.id.name);
			} else {
				convertView = inflater.inflate(R.layout.item_device_list, null);
				holder.radioBtn = (RadioButton) convertView.findViewById(R.id.rb_select);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.address = (TextView) convertView.findViewById(R.id.address);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (bd == null) {
			if (selectDevice == null) {
				holder.radioBtn.setChecked(true);
			} else {
				holder.radioBtn.setChecked(false);
			}
		} else {
			holder.name.setText(bd.device.getName());
			holder.address.setText(bd.device.getAddress());
			String str = "[";
			
			str += bd.isDiscovery ?res.getString(R.string.visible)  : "";
			str += bd.device.getBondState() == BluetoothDevice.BOND_BONDED ? res.getString(R.string.paired) : res.getString(R.string.unpaired);
			str += bd.isDiscovery ? res.getString(R.string.lab_signal_strength) + parseRssiStr(bd.rssi) : "";
			str += "]";
			holder.info.setText(str);
			if (bd.isDiscovery) {
				holder.icon.setImageResource(R.drawable.bluetooth_bond);
			} else {
				holder.icon.setImageResource(R.drawable.bluetooth_unbond);
			}
			// Log.d("tag", "设置的地址:" + selectDevice.getAddress() + ",Item的地址:" +
			// bd.device.getAddress());
			if (bd.device.equals(selectDevice)) {
				Log.d("tag", "item checked");
				holder.radioBtn.setChecked(true);
			} else {
				holder.radioBtn.setChecked(false);
			}
		}
		return convertView;
	}

	private String parseRssiStr(short rssi) {
		if (rssi > -40) {
			return res.getString(R.string.signal_strong);
		} else if (rssi > -60) {
			return res.getString(R.string.signal_general);
		} else {
			return res.getString(R.string.signal_weak);
		}
	}

	public void setSelectDevice(BluetoothDevice selectDevice) {
		Log.d("tag", "set device:" + selectDevice);
		this.selectDevice = selectDevice;
		notifyDataSetChanged();
	}

	public BluetoothDevice getSelectDevice() {
		return this.selectDevice;
	}

	private class ViewHolder {
		ImageView icon;
		TextView name, address, info;
		RadioButton radioBtn;
	}

}

package com.naruto.tpms.app.bean;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙设备信息的再一次封装
 * 
 * @author Thinkman 415071574@qq.com
 */
public class DeviceInfo {
	public BluetoothDevice device;
	public short rssi;
	public boolean isDiscovery;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceInfo other = (DeviceInfo) obj;
		if (device == null) {
			if (other.device != null)
				return false;
		} else if (!device.equals(other.device))
			return false;
		return true;
	}

}

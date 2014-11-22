package com.naruto.tpms.app.dao;

import android.content.Context;

import com.naruto.tpms.app.bean.DeviceBind;
import com.naruto.tpms.app.comm.orm.AbDBDaoImpl;
import com.naruto.tpms.app.comm.orm.DBInsideHelper;

public class DeviceBindDao extends AbDBDaoImpl<DeviceBind> {
	public DeviceBindDao(Context context) {
		super(new DBInsideHelper(context), DeviceBind.class);
	}

}

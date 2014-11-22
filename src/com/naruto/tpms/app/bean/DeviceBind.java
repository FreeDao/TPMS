package com.naruto.tpms.app.bean;

/**
 * 家居控制器位置跟蓝牙设备绑定关系
 * 
 * @author Thinkman 415071574@qq.com
 */

import com.naruto.tpms.app.comm.orm.annotation.Column;
import com.naruto.tpms.app.comm.orm.annotation.Id;
import com.naruto.tpms.app.comm.orm.annotation.Table;

@Table(name = "device_bind")
public class DeviceBind {

	/**
	 * 单片机控制器ID
	 */
	@Id
	@Column(name = "id")
	public int deviceId;
	/**
	 * 已绑定蓝牙的mac地址
	 */
	@Column(name = "bt_mac", length = 17)
	public String btMac;
	/**
	 * 已绑定蓝牙的设备名称
	 */
	@Column(name = "bt_name")
	public String btName;
}

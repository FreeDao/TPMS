package com.naruto.tpms.app.bean;

/**
 * 封装的轮胎数据信息
 * @author Thinkman
 *
 */
public class TireData {
	
	/**
	 * 轮胎编号,0x00表示左前 ，0x01表示右前，0x10表示左后，0x11表示右后
	 */
	public byte tireNum;
	/**
	 * 轮胎ID
	 */
	public byte[]  tireId;
	/**
	 * 压力(储存的是经过计算后得到的kpa)
	 */
	public double pressure;
	/**
	 * 温度(存储的是经过计算后得到的摄氏度
	 */
	public int temperature;
	
	/**
	 * 是否漏气
	 */
	public boolean isLeak;
	/**
	 * 是否低电量
	 */
	public boolean isLowBattery;
	/**
	 * 是否信号错误
	 */
	public boolean isSignalError;
	/**
	 * 上次报告时间
	 */
	public long lastUpdateTime;
	
	/**
	 * 是否存在告警
	 */
	public boolean  haveAlarm;
}

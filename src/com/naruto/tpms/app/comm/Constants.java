package com.naruto.tpms.app.comm;

import java.util.Locale;

public class Constants {

	public static final String PREF_NAME_SETTING = "setting";
	public static final String SETTING_MAC = "bluetooth_mac";
	public static final String SETTING_NAME = "bluetooth_name";
	public static final String SETTING_PRESSURE_UNIT = "pressure_unit";
	public static final String SETTING_TEMPERATURE_UNIT = "temperature_unit";
	public static final String SETTING_HIGH_PRESSURE = "high_pressure";
	public static final String SETTING_LOCAL = "local";
	public static final String SETTING_LOW_PRESSURE = "low_pressure";
	public static final String SETTING_HIGH_TEMPRATURE = "high_temprature";
	public static final String SETTING_IS_ALARM_ON = "is_alarm_on";
	public static final String SETTING_IS_SCREEN_KEEP = "is_screen_keep";
	public static final String SETTING_IS_AUTO_BOOT = "is_auto_boot";

	public static final float HIGH_PRESSURE_MIN = 0f;
	public static final float HIGH_PRESSURE_MAX = 800f;
	public static final float HIGH_PRESSURE_STEUP = 10f;

	public static final float LOW_PRESSURE_MIN = 0f;
	public static final float LOW_PRESSURE_MAX = 800f;
	public static final float LOW_PRESSURE_STEUP = 10f;

	public static final float HIGH_TEMPRATURE_MIN = -40f;
	public static final float HIGH_TEMPRATURE_MAX = 120f;
	public static final float HIGH_TEMPRATURE_STEUP = 5f;
	/**
	 * ÂÖÌ¥Î»ÖÃÐòºÅÊý×é
	 */
	public static final byte[] TIRE_NUMS = new byte[] { 0x00, 0x01, 0x10, 0x11 };//
	/**
	 * ÂÖÌ¥Î»ÖÃÃû³Æ
	 */
	public static   String[] TIRE_NAMES =  new String[] { "×óÇ°ÂÖ", "ÓÒÇ°ÂÖ", "×óºóÂÖ", "ÓÒºóÂÖ" };//
	/**
	 * ÂÖÌ¥ÖÃ»»ÃüÁî
	 */
	public static final byte[] SWITCH_CMD = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };//

	public static final Locale[] LOCALE_ARR = new Locale[] { Locale.getDefault(), Locale.SIMPLIFIED_CHINESE, Locale.TAIWAN, Locale.ENGLISH };
}

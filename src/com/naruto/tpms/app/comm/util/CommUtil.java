package com.naruto.tpms.app.comm.util;

import java.text.DecimalFormat;

import com.naruto.tpms.app.comm.AppContext;
import com.naruto.tpms.app.comm.Constants;
import com.naruto.tpms.app.weight.TipsToast;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

/**
 * 公共通用工具
 * 
 * @author Thinkman
 * 
 */
public class CommUtil {

	/**
	 * 计算校验位
	 * 
	 * @param bytes
	 *            字节码数组
	 * @param startPosition
	 *            参与校验计算的起始位置
	 * @param endPosition
	 *            参与校验计算的最后位置
	 * @return
	 */
	public static byte getCheckByte(byte[] bytes, int startPosition, int endPosition) {
		byte result = 0x00;
		for (int i = startPosition; i <= endPosition; i++) {
			result ^= bytes[i];
		}
		return result;
	}

	/**
	 * 字节转二进制字符串
	 * 
	 * @param in
	 * @return
	 */
	public static String byteToBinaryString(byte in) {
		String result = Integer.toBinaryString(in);
		if (result.length() > 8) {
			result = result.substring(result.length() - 8);
		} else if (result.length() < 8) {
			result = "00000000".substring(result.length()) + result;
		}
		return result;
	}

	static TipsToast tipsToast;

	/**
	 * 带icon的Toast提示
	 * 
	 * @param iconResId
	 * @param msgResId
	 */
	public static void showTips(int iconResId, int msgResId) {
		if (tipsToast != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				tipsToast.cancel();
			}
		} else {
			tipsToast = TipsToast.makeText(AppContext.getInstance().getBaseContext(), msgResId, TipsToast.LENGTH_LONG);
		}
		tipsToast.show();
		tipsToast.setIcon(iconResId);
		tipsToast.setText(msgResId);
	}

	public static void showTips(int iconResId, String message) {
		if (tipsToast != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				tipsToast.cancel();
			}
		} else {
			tipsToast = TipsToast.makeText(AppContext.getInstance().getBaseContext(), message, TipsToast.LENGTH_LONG);
		}
		tipsToast.show();
		tipsToast.setIcon(iconResId);
		tipsToast.setText(message);
	}

	// 用作显示的压力和温度单位
	public static final String[] pressureUnits = new String[] { "Psi", "KPa", "Bar" };
	public static final String[] temperatureUnits = new String[] { "°C", "°F" };
	static DecimalFormat pressureFormater = new DecimalFormat("0.0");
	static DecimalFormat pressurekPaFormater = new DecimalFormat("0");
	static DecimalFormat tempratureFormater = new DecimalFormat("0");

	/**
	 * 
	 * @param pressureVlue
	 *            压力值，单位为kPa
	 * @param pressureUnitIndex
	 *            当前单位index
	 * @return
	 */
	public static String getPressure(double pressureVlue, int pressureUnitIndex) {
		double num = 0.0d;
		switch (pressureUnitIndex) {
		case 0:
			num = 0.145037d * pressureVlue;
			break;
		case 1:
			num = pressureVlue;
			return pressurekPaFormater.format(num);
		case 2:
			num = 0.01 * pressureVlue;
			break;
		}
		String str = pressureFormater.format(num);
		return str;// +
											// pressureUnits[pressureUnitIndex];
	}

	public static String getPressureWithUnit(double pressureVlue, int pressureUnitIndex) {
		double num = 0.0d;
		switch (pressureUnitIndex) {
		case 0:
			num = 0.145037d * pressureVlue;
			break;
		case 1:
			num = pressureVlue;
			break;
		case 2:
			num = 0.01 * pressureVlue;
			break;
		}
		return pressureFormater.format(num) + pressureUnits[pressureUnitIndex];
	}

	/**
	 * @param temperatureValue
	 *            原始温度值,单位°C
	 * @param temperatureUnitIndex
	 *            单位序号
	 * @return
	 */
	public static String getTemperatureWithUnit(double temperatureValue, int temperatureUnitIndex) {
		double num = 0.0d;
		switch (temperatureUnitIndex) {
		case 0:
			num = (int)temperatureValue;
			break;
		case 1:
			num = (int)temperatureValue * 9 / 5 + 32;
			break;
		}
		String str = tempratureFormater.format(num) + temperatureUnits[temperatureUnitIndex];
		return str;
	}

	public static String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			// if (n<b.length-1) hs=hs+":";
		}
		return hs.toUpperCase();
	}

	/**
	 * 根据轮胎位置编号获得轮胎位置index
	 * 
	 * @param tireNum
	 * @return
	 */
	public static int getTireIndexByTireNum(byte tireNum) {
		for (int i = Constants.TIRE_NUMS.length - 1; i >= 0; i--) {
			if (tireNum == Constants.TIRE_NUMS[i]) {
				return i;
			}
		}
		return -1;
	}

}

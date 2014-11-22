package com.naruto.tpms.app;

import com.naruto.tpms.app.comm.util.CommUtil;

public class Test {

	public static void main(String[] args) {
//		55 AA 08 11 00 53 00 B5
//		AA 08 11 1C 5E 08 AC 55
		byte[]  bytes = new byte[]{0x55 ,(byte) 0xAA ,0x08 ,0x00 ,0x19,(byte) 0x73,0x10 };
		byte b = CommUtil.getCheckByte(bytes, 0, bytes.length-1);
		System.out.println(CommUtil.byte2HexStr(new byte[]{b}));
		int a =  0x21&(byte)0xFF;
//		a = a<<8;
		System.out.println(a);
	}
	
}

package com.naruto.tpms.app.comm.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicUtil {

	private static MediaPlayer mp = null;

	/**
	 * ≤•∑≈…˘“Ù
	 * 
	 * @param paramContext
	 * @param paramInt
	 */
	public static void play(Context paramContext, int paramInt) {
		if (mp == null) {
			mp = MediaPlayer.create(paramContext, paramInt);
			mp.setLooping(true);
			// mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}
//		Log.d("tag", "’˝‘⁄≤•∑≈“Ù¿÷¬?:" + mp.isPlaying());
		if (!mp.isPlaying()) {
			mp.start();
		}
	}

	public static boolean isPlaying() {
		return mp == null ? false : mp.isPlaying();
	}

	/**
	 * Õ£÷π≤•∑≈
	 * 
	 * @param paramContext
	 */
	public static void stop(Context paramContext) {
		if (mp == null)
			return;
		mp.stop();
		mp.release();
		mp = null;
	}
}

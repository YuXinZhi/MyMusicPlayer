package com.example.mymusicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MusicUtils {

	private static final String TAG = "MusicUtils";

	/**
	 * 获取int类型的SharePreferences的值
	 * 
	 * @param context
	 * @param name
	 * @param def
	 * @return
	 */
	 public static int getIntPref(Context context, String name, int def) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

}

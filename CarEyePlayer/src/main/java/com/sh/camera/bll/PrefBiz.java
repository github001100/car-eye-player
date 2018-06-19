/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
 * CarEyeRtmpAPI.c
 *
 * Author: Wgj
 * Date: 2018-03-19 19:15
 * Copyright 2018
 *
 * CarEye RTMP推流库接口实现
 * 实时推送数据时候支持最大8个通道的流
 */

package com.sh.camera.bll;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SharedPreferences操作
 * @author zxt
 *
 */
public class PrefBiz {

	private SharedPreferences pref;


	public PrefBiz(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(
				context.getApplicationContext());
	}

	/** String 类型*/
	public String getStringInfo(String key,String defStr) {
		return pref.getString(key, defStr);
	}

	public void putStringInfo(String key, String value) {
		pref.edit().putString(key, value).commit();
	}
	
	/** boolean 类型*/
	public boolean getBooleanInfo(String key,boolean def) {
		return pref.getBoolean(key, def);
	}
	
	public void putBooleanInfo(String key, boolean def) {
		pref.edit().putBoolean(key, def).commit();
	}
	
	/** int 类型*/
	public int getIntInfo(String key,int def) {
		return pref.getInt(key, def);
	}
	
	public void putIntInfo(String key, int def) {
		pref.edit().putInt(key, def).commit();
	}
	

	/** 删除*/
	public void removeInfo(String key) {
		pref.edit().remove(key).commit();
	}
}

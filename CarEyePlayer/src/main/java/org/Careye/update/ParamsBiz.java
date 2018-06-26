/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/


package org.Careye.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.Careye.push.util.Constants;

import org.Careye.CarEyePlayer.SettingsActivity;

public class ParamsBiz {
	
	/***********************************偏好配置，参数设置对象**********************************/
	private static SharedPreferences sp = null;
	private static Editor editor = null;

	private static SharedPreferences getSp(){
		if(sp == null){
			//sp = MainService.getInstance().getSharedPreferences("fcoltest", Context.MODE_MULTI_PROCESS);
			sp = SettingsActivity.getInstance().getSharedPreferences("mydata", Context.MODE_MULTI_PROCESS);
		}
		return sp;
	}
	private static Editor getSpEditor(){
		if(editor == null){
			editor = getSp().edit();
		}
		return editor;
	}
	
	/***************************参数key***************************/
	private static final String KEY_UPDATE_IP = "update_ip";
	private static final String KEY_UPDATE_PORT = "update_port";
	
	
	/***************************参数操作***************************/
	/**版本升级IP*/
	public static String getUpdateIp(){
		String param = getSp().getString(KEY_UPDATE_IP, Constants.UPDATE_IP);
		return param;
	}
	
	public static void setUpdateIP(String param){
		getSpEditor().putString(KEY_UPDATE_IP, param).commit();
	}
	
	/**版本升级端口*/
	public static String getUpdatePort(){
		String param = getSp().getString(KEY_UPDATE_PORT, Constants.UPDATE_PORT);
		return param;
	}
	
	public static void setUpdatePort(String param){
		getSpEditor().putString(KEY_UPDATE_PORT, param).commit();
	}
}

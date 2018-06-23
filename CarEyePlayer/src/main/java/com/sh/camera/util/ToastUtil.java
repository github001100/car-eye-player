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


package com.sh.camera.util;

import android.content.Context;

/**
 * 显示工具
 */
public class ToastUtil {
	/**
	 * 
	 * @param context 上下文对
	 * @param msg 显示内容
	 */
	public static void showToast(Context context, String msg){
//		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		MyToast.showToast(context, msg, true, 0);
	}
	public static void longToast(Context context, String msg){
//		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		MyToast.showToast(context, msg, true, 0);
	}
	
	public static void showToast(Context context, int resId){
		String msg = context.getResources().getString(resId);
//		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		MyToast.showToast(context, msg, true, 0);
	}
	
	public static void longToast(Context context, int resId){
		String msg = context.getResources().getString(resId);
//		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		MyToast.showToast(context, msg, true, 1);
	}
}

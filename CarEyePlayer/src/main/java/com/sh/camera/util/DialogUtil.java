<<<<<<< HEAD
/*  car eye 车辆管理平台 
 * car-eye管理平台   www.car-eye.cn
 * car-eye开源网址:  https://github.com/Car-eye-team
 * Copyright
 */


=======
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

>>>>>>> origin/master
package com.sh.camera.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

<<<<<<< HEAD
import com.sh.camera.service.MainService;
=======
>>>>>>> origin/master

public class DialogUtil {

	private static Dialog currentDlg;

	/**
	 * 查询等待消息提示框
	 * 
<<<<<<< HEAD
	 * @param context
	 * @param msg
	 * @return
	 */
	public static void popProgress(final Context context, final String msg) {
		/*MainService.getInstance().handler.post(new Runnable() {
=======
	 * @param
	 * @param
	 * @return
	 */
	public static void popProgress(final Context context, final String msg) {
	/*	MainService.getInstance().handler.post(new Runnable() {
>>>>>>> origin/master

			@Override
			public void run() {
				ProgressDialog pd = new ProgressDialog(context);
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				currentDlg = ProgressDialog.show(context, null, msg);
				currentDlg.setCancelable(true);
				currentDlg.setCanceledOnTouchOutside(false);
			}
		});*/
	}

	/**
	 * 关闭查询等待消息提示框
	 * 
<<<<<<< HEAD
	 * @param mcontext
	 * @param msgid
=======
	 * @param
	 * @param
>>>>>>> origin/master
	 * @return
	 */
	private static boolean isCurrentDlgShowing() {
		return currentDlg != null && currentDlg.isShowing();
	}

	/**
	 * 关闭查询等待消息提示框
	 * 
	 * @param mcontext
	 * @param msgid
	 * @return
	 */
	public static void dismissCurrentDlg() {
		if (isCurrentDlgShowing()) {
			currentDlg.dismiss();
		}
	}
}

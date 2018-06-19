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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;


public class DialogUtil {

	private static Dialog currentDlg;

	/**
	 * 查询等待消息提示框
	 * 
	 * @param
	 * @param
	 * @return
	 */
	public static void popProgress(final Context context, final String msg) {
	/*	MainService.getInstance().handler.post(new Runnable() {

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
	 * @param
	 * @param
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

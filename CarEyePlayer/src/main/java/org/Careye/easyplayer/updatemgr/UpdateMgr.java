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
package org.Careye.easyplayer.updatemgr;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.Careye.easyplayer.PlaylistActivity;
import org.Careye.rtsp.player.R;

import java.io.IOException;
import android.os.Handler;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kim on 2016/8/25.
 */
public class UpdateMgr {
    private static final String TAG = "UpdateMgr";
    private Context mContext;
    private String mApkUrl;
    private Handler mHandler;
    private Runnable mShowDlg = new Runnable() {
        @Override
        public void run() {
            showUpdateDialog();
        }
    };

    public UpdateMgr(Context context){
        this.mContext = context;
        mHandler = new Handler();
    }


    /**
     * 检测当前APP是否需要升级
     */
    public void checkUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url;
                    if (PlaylistActivity.isPro()) {
                        url = "http://www.car-eye.cn/versions/easyplayer_pro/version.txt";  //easyplayer_pro   2
                    }else{
                        url = "http://www.car-eye.cn/versions/easyplayer/version.txt";  //easyplayer  1
                    }
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        RemoteVersionInfo versionInfo = new Gson().fromJson(string, RemoteVersionInfo.class);

                        if(versionInfo == null || TextUtils.isEmpty(versionInfo.getUrl())){
                            return;
                        }
                        PackageManager packageManager=mContext.getPackageManager();
                        try {
                            PackageInfo packageInfo=packageManager.getPackageInfo(mContext.getPackageName(),0);
                            int localVersionCode=packageInfo.versionCode;
                            int remoteVersionCode= Integer.valueOf(versionInfo.getVersionCode());
                            Log.d(TAG, "kim localVersionCode="+localVersionCode+", remoteVersionCode="+remoteVersionCode);
                            if(localVersionCode<remoteVersionCode){
                                mApkUrl = versionInfo.getUrl();
                                mHandler.post(mShowDlg);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(TAG,"Get PackageInfo error !");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 提示升级对话框
     */
    private void showUpdateDialog(){
        final String apkUrl=mApkUrl;
        Log.d(TAG, "kim showUpdateDialog. apkUrl="+apkUrl);
        new AlertDialog.Builder(mContext)
                .setMessage("car-eye-player可以升级到更高的版本，是否升级")
                .setTitle("升级提示")
                .setIcon(R.drawable.img_qrcode)
                .setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                        Uri uri = Uri.parse(apkUrl);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                        // 禁止发出通知，既后台下载，如果要使用这一句必须声明一个权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        downloadManager.enqueue(request);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }
}

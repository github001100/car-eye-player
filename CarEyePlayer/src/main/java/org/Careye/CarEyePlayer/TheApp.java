/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/
package org.Careye.CarEyePlayer;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.tencent.bugly.crashreport.CrashReport;

import org.Careye.CarEyePlayer.data.EasyDBHelper;
import org.Careye.rtsp.player.BuildConfig;
import org.Careye.rtsp.player.R;

/**
 * Created by afd on 8/13/16.
 */
public class TheApp extends Application {
     /**def ip*/
    public static final String DEFAULT_SERVER_IP = "0.0.0.0";
    public static SQLiteDatabase sDB;
    public static String sPicturePath;
    public static String sMoviePath;

    @Override
    public void onCreate() {
        super.onCreate();
        sPicturePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/car-eye-player";
        sMoviePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/car-eye-player";
        sDB = new EasyDBHelper(this).getWritableDatabase();
        //resetServer();
        CrashReport.initCrashReport(getApplicationContext(), "045f78d6f0", BuildConfig.DEBUG);
    }

    /*public void resetServer(){
        String ip = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_ip), DEFAULT_SERVER_IP);
        if ("114.55.107.180".equals(ip)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(R.string.key_ip), DEFAULT_SERVER_IP).apply();
        }
    }*/
}

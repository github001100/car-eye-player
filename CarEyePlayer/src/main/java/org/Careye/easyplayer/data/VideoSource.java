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
package org.Careye.easyplayer.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by afd on 8/13/16.
 */
public class VideoSource implements BaseColumns {
    public static final String URL = "url";
    public static final String TABLE_NAME = "video_source";
    /**
     * -1 refers to manual added, otherwise pulled from server.
     */
    public static final String INDEX = "_index";
    public static final String NAME = "name";
    public static final String AUDIENCE_NUMBER = "audience_number";

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s integer primary key autoincrement, " +
                        "%s integer default -1, " +
                        "%s VARCHAR(256) NOT NULL DEFAULT '', " +
                        "%s VARCHAR(256) NOT NULL DEFAULT '', " +
                        "%s integer DEFAULT 0 " +
                        ")",
                TABLE_NAME,
                _ID,
                INDEX,
                URL,
                NAME,
                AUDIENCE_NUMBER));
    }

}

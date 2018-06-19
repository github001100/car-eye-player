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

package org.Careye.push.codec;

import java.io.IOException;

/**
 * Created by apple on 2017/5/13.
 */

public interface VideoConsumer {
    public void onVideoStart(int width, int height) throws IOException;

    public int onVideo(byte[] data, int format);

    public void onVideoStop();
}

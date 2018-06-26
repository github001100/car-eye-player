/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
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

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

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;

import org.Careye.push.audio.AudioStream;
import org.Careye.push.push.Pusher;
import  org.Careye.push.util.Constants;
import org.Careye.push.hw.EncoderDebugger;
import org.Careye.push.hw.NV21Convertor;
import java.io.IOException;

/**
 *     
 * 项目名称：SH_CAMERA    
 * 类名称：MediaCodecManager    
 * 类描述：    
 * 创建人：Apple
 * 创建时间：2016年10月18日 上午11:56:29    
 * 修改人：Administrator    
 * 修改时间：2016年10月18日 上午11:56:29    
 * 修改备注：    
 * @version 1.0  
 *     
 */
public class MediaCodecManager {

	private static final String TAG = "MediaCodecManager";
	private static MediaCodecManager instance;
	public static NV21Convertor mConvertor;
	//解码
	private int previewFormat;
	public static MediaCodec[] mMediaCodec;
	public static boolean TakePicture = false;
	private static boolean sw_codec	= false;
	private VideoConsumer mVC;
	private EncoderDebugger debugger;
	private AudioStream audioStream;
	
	public static MediaCodecManager getInstance() {
		if (instance == null) {
			//mMediaCodec = new MediaCodec[Constants.MAX_NUM_OF_CAMERAS];			
			
			instance = new MediaCodecManager();
		}
		return instance;
	}	
	/**
	 * 释放解码器资源
	 * @param index
	 */
 public void StartUpload(Context context, int index, Pusher mPusher)
 {
     mVC = new HWConsumer(context, mPusher, index);
	 try {
		mVC.onVideoStart(Constants.UPLOAD_VIDEO_WIDTH, Constants.UPLOAD_VIDEO_HEIGHT);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }

 public void StopUpload(int index)
 {
	 if(mVC!= null)
	 {
		 mVC.onVideoStop();
		 mVC =null;
	 }	
	 if (audioStream != null) {
         audioStream.stop();
         audioStream = null;
     }
 }
public void onPreviewFrameUpload(byte[] data,int index,Camera camera){			
	 if (data == null ) {
		 camera.addCallbackBuffer(data);
         return;
     }
     Camera.Size previewSize = camera.getParameters().getPreviewSize();
     if (data.length != Constants.UPLOAD_VIDEO_HEIGHT * Constants.UPLOAD_VIDEO_WIDTH * 3 / 2) {
    	 camera.addCallbackBuffer(data);
	   	 return;
     }   
     if(mVC!= null)
     {
      	 mVC.onVideo(data, previewFormat);
     }else
     {   	
    	 camera.setPreviewCallback(null);    
     }
     camera.addCallbackBuffer(data);      
 }
}

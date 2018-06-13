/*  car eye 车辆管理平台 
 * car-eye管理平台   www.car-eye.cn
 * car-eye开源网址:  https://github.com/Car-eye-team
 * Copyright
 */
package org.Careye.push.push;


import android.content.Context;
import android.os.Handler;


import java.nio.ByteBuffer;

public class Pusher {

	static {
		System.loadLibrary("stream");
	}
	/**
	 * 初始化
	 * @param serverIP   流媒体服务器IP
	 * @param serverPort 流媒体服务器端口
	 * @param streamName 流媒体文件名
	 * @param fps
	 * @param format
	 * @return
	 */
	private ByteBuffer directbuffer;
	
	private Handler handle =null;	
	public native int    CarEyeInitNetWorkRTSP(Context context,String serverIP, String serverPort, String streamName, int videoformat, int fps,int audioformat, int audiochannel, int audiosamplerate);
	public native int 	CarEyePusherIsReadyRTSP(int channel);
	public native long   CarEyeSendBufferRTSP(long time, byte[] data, int lenth, int type, int channel);
	public native void   CarEyeStopPushNetRTSP(int index);
	public  long SendBuffer_org(final byte[] data,final int length, final long timestamp, final int type, final int index)
	{
		long ret;
		//Log.e("puser", "timestamp:"+timestamp+"length:"+length);	
		ret =  CarEyeSendBufferRTSP(timestamp, data,length,type,index);		
		return ret;
	}

	public  void stopPush(final int  index)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				 CarEyeStopPushNetRTSP(index);
			}
		}).start();
	}
	/**
	 * 停止所有
	 */
	public void stop() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				stopPush(0);
				stopPush(1);
				stopPush(2);
				stopPush(3);
			}
		}).start();
	}

}


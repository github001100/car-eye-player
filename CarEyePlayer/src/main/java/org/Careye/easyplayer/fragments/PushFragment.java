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
package org.Careye.easyplayer.fragments;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.Careye.rtsp.player.R;
import org.Careye.push.codec.MediaCodecManager;
import org.Careye.push.hw.EncoderDebugger;
import org.Careye.push.hw.NV21Convertor;
import org.Careye.push.push.Pusher;
import org.Careye.push.util.Constants;
import org.Careye.video.CarEyePlayerClient;

import java.io.IOException;

import static android.content.Context.MODE_MULTI_PROCESS;
import static org.Careye.easyplayer.fragments.PlayFragment.ARG_PARAM3;
import static org.Careye.easyplayer.fragments.PlayFragment.KEY;

public class PushFragment extends Fragment implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    Camera.Parameters param;
    public boolean isFront = true;
    public static int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private TextureView mTextureView;
    private EncoderDebugger debugger;
    Camera.PreviewCallback previewCallback;
    public static Pusher mPusher;

    private boolean mUpload = false;
    private int mChannel = 0;
    int flag = 1;
    public CarEyePlayerClient mStreamRender;
    protected ResultReceiver mResultReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200);
        debugger = EncoderDebugger.debug(getActivity(), Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
        mPusher = new Pusher();

        //播放功能按钮隐藏
        Button btn_play_audio = getActivity().findViewById(R.id.btn_play_audio);
        Button btn_play_a = getActivity().findViewById(R.id.btn_play_a);
        Button btn_play_b = getActivity().findViewById(R.id.btn_play_b);
        btn_play_audio.setVisibility(View.GONE);
        btn_play_a.setVisibility(View.GONE);
        btn_play_b.setVisibility(View.GONE);

        LinearLayout view = new LinearLayout(getActivity());
        view.setLayoutParams(lp);
        mTextureView = new TextureView(getActivity());
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200));
        previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.d("CMD", " onPreviewFrame");
                MediaCodecManager.getInstance().onPreviewFrameUpload(data, 0, mCamera);
            }
        };
        view.addView(mTextureView);
        return view;
    }

    /**
     * 单例 2018 by fu
     *
     * @param rr
     * @return
     */
    public static PushFragment newInstance(ResultReceiver rr) {
        PushFragment fragment = new PushFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, url);
//        args.putInt(ARG_PARAM2, type);
        args.putParcelable(ARG_PARAM3, rr);
        fragment.setArguments(args);
        return fragment;
    }

    public void StopVideoUpload(int index) {
        if (index >= 0 && index <= 8) {
            MediaCodecManager.getInstance().StopUpload(index);
            mPusher.CarEyeStopPushNetRTSP(index);
        }
        mCamera.setPreviewCallback(null);
    }

    public int StartVideoUpload(String ipstr, String portstr, String serialno) {
        int index;
        int id = 1;
        index = mPusher.CarEyeInitNetWorkRTSP(getActivity(), ipstr, portstr, String.format("%s&channel=%d.sdp", serialno, id), Constants.CAREYE_VCODE_H264, 20, Constants.CAREYE_ACODE_AAC, 1, 8000);
        MediaCodecManager.getInstance().StartUpload(getActivity(), index, mPusher);
        mCamera.setPreviewCallback(previewCallback);
        return index;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        PlayFragment.mStreamRender.setAudioEnable(false);
        mCamera = Camera.open(mCameraId);//

        Camera.Parameters parameters = mCamera.getParameters();
        int previewFormat0 = debugger.getNV21Convertor().getPlanar() ? ImageFormat.YV12 : ImageFormat.NV21;
        parameters.setPreviewFormat(previewFormat0);
        parameters.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            // mCamera.setPreviewCallback(previewCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isFront = false;
        Button btn_switch_cammars = getActivity().findViewById(R.id.btn_switch_cammars);
        final Button btn_push = getActivity().findViewById(R.id.btn_push_stop);//内 推流按钮
        final Button btn_url = getActivity().findViewById(R.id.btn_push_url);
        //final EditText server_ip = getActivity().findViewById(R.id.server_ip);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //final String ip = sharedPreferences.getString(Constants.ip, Constants.SERVER_IP);
        //final String a = getString(R.string.key_ip);

        SharedPreferences pref = getActivity().getSharedPreferences("mydata", MODE_MULTI_PROCESS);
        final String sip = pref.getString(getString(R.string.key_ip), "www.car-eye.cn");
        final String key_port = pref.getString(getString(R.string.key_port), "10554");
        final String key_app_name = pref.getString(getString(R.string.key_app_name), "demo");

        final String key_url = pref.getString(getString(R.string.key_url), "rtsp://www.car-eye.cn:10554/demo&channel=1.sdp");//demo

        btn_switch_cammars.setVisibility(View.VISIBLE);
        btn_push.setVisibility(View.VISIBLE);
        btn_url.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipboardManager cm =(ClipboardManager)getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
                cm.setText(btn_url.getText().toString());
                Toast.makeText(getContext(),"已复制",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        btn_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btn_push.setText("停止");
                btn_url.setVisibility(View.VISIBLE);

                btn_url.setText("rtsp://" + sip + ":" + key_port + "/" + key_app_name + ".sdp".toLowerCase());
               // btn_url.setText(key_url);
                if (flag == 0 && mUpload == true) {
                    btn_push.setText("推流");
                    StopVideoUpload(mChannel);
                    mUpload = false;
                    flag = 1;

                } else if (flag == 1 && mUpload == false) {
                    btn_push.setText("停止");
                    mChannel = StartVideoUpload(sip, key_port, key_app_name);
                    mUpload = true;
                    flag = 0;

                }
                if (mUpload == false) {

                } else {

                }

            }
        });
        btn_switch_cammars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!isFront) {
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = Camera.open(1);
                        param = mCamera.getParameters();
                        int previewFormat = debugger.getNV21Convertor().getPlanar() ? ImageFormat.YV12 : ImageFormat.NV21;
                        param.setPreviewFormat(previewFormat);
                        param.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
                        mCamera.setParameters(param);
                        //mCamera.setDisplayOrientation(orientation);
                        try {
                            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mCamera.startPreview();
                        isFront = true;
                    } else {
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = Camera.open(0);
                        param = mCamera.getParameters();
                        int previewFormat = debugger.getNV21Convertor().getPlanar() ? ImageFormat.YV12 : ImageFormat.NV21;
                        param.setPreviewFormat(previewFormat);
                        param.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
                        mCamera.setParameters(param);
                        //mCamera.setDisplayOrientation(orientation);
                        try {
                            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mCamera.startPreview();
                        isFront = false;
                    }
                    //mCamera = Camera.open(mCameraId);

          /* Camera.Parameters parameters = mCamera.getParameters();
           int previewFormat =  debugger.getNV21Convertor().getPlanar() ? ImageFormat.YV12 : ImageFormat.NV21;
           parameters.setPreviewFormat(previewFormat);
           parameters.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
          mCamera.setParameters(parameters);*/
                } catch (Exception e) {
                    e.printStackTrace();
                    mCamera = null;
                }
            }
        });

/*        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
           // mCamera.setPreviewCallback(previewCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        mTextureView.setRotation(90);
        mTextureView.setScaleX(2f);
    }

    public void reopenCamera() {
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            //mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    public void openCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(mCameraId);
            } catch (RuntimeException e) {
                if ("Fail to connect to camera service".equals(e.getMessage())) {
                    //提示无法打开相机，请检查是否已经开启权限
                } else if ("Camera initialization failed".equals(e.getMessage())) {
                    //提示相机初始化失败，无法打开
                } else {
                    //提示相机发生未知错误，无法打开
                }
                //finish();
                return;
            }
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera.setPreviewCallback(null);
            mCamera = null;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mCamera.setPreviewCallback(null);//先设置为空

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

}
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

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.Careye.easyplayer.TheApp;
import org.Careye.easyplayer.views.OverlayCanvasView;
import org.Careye.video.Client;
import org.Careye.video.CarEyePlayerClient;
import org.Careye.rtsp.player.R;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by apple on 2017/12/30.
 */

public class YUVExportFragment extends PlayFragment implements CarEyePlayerClient.I420DataCallback {

    OverlayCanvasView canvas;
    public static YUVExportFragment newInstance(String url, int type, ResultReceiver rr) {
        YUVExportFragment fragment = new YUVExportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        args.putInt(ARG_PARAM2, type);
        args.putParcelable(ARG_PARAM3, rr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_play_overlay_canvas, container, false);
        cover = (ImageView) view.findViewById(R.id.surface_cover);
        canvas = view.findViewById(R.id.overlay_canvas);

        return view;
    }

    @Override
    protected void startRending(SurfaceTexture surface) {
        mStreamRender = new CarEyePlayerClient(getContext(), KEY, new Surface(surface), mResultReceiver, this);

        boolean autoRecord = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("auto_record", false);

        File f = new File(TheApp.sMoviePath);
        f.mkdirs();

        try {
            mStreamRender.start(mUrl, mType, Client.CAR_EYE_SDK_VIDEO_FRAME_FLAG | Client.CAR_EYE_SDK_AUDIO_FRAME_FLAG, "", "", autoRecord ? new File(f, new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date()) + ".mp4").getPath() : null);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        sendResult(RESULT_REND_STARTED, null);
    }

    /**
     * 这个buffer对象在回调结束之后会变无效.所以不可以把它保存下来用
     .如果需要保存,必须要创建新buffer,并拷贝数据.
     * @param buffer
     */
    @Override
    public void onI420Data(ByteBuffer buffer) {
        Log.i(TAG, "I420 data length :" + buffer.capacity());


    }

    private void writeToFile(String path, ByteBuffer buffer){
        try {
            FileOutputStream fos = new FileOutputStream(path, true);
            byte[] in = new byte[buffer.capacity()];
            buffer.clear();
            buffer.get(in);
            fos.write(in);
            fos.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onMatrixChanged(Matrix matrix, RectF rect) {
        super.onMatrixChanged(matrix, rect);
        if (canvas != null) {
            canvas.setTransMatrix(matrix);
        }
    }

    public void toggleDraw() {
        canvas.toggleDrawable();
    }
}

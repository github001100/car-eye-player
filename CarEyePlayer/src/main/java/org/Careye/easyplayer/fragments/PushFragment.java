package org.Careye.easyplayer.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.Careye.push.codec.MediaCodecManager;
import org.Careye.push.hw.EncoderDebugger;
import org.Careye.push.hw.NV21Convertor;
import org.Careye.push.push.Pusher;
import org.Careye.push.util.Constants;

import java.io.IOException;

public class PushFragment extends Fragment implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private EncoderDebugger debugger;
    Camera.PreviewCallback previewCallback;
    public static Pusher mPusher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200);
        debugger = EncoderDebugger.debug(getActivity() , Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
        mPusher =  new Pusher();
        LinearLayout view = new LinearLayout(getActivity());
        view.setLayoutParams(lp);
        mTextureView = new TextureView(getActivity());
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200));
        previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.d("CMD", " onPreviewFrame");
                MediaCodecManager.getInstance().onPreviewFrameUpload(data,0,mCamera);
            }
        };
        view.addView(mTextureView);

        return view;
    }

    public void startVideoUpload( String ipstr, String portstr, String serialno)
    {
        int index;
        int id =1;
        index = mPusher.CarEyeInitNetWorkRTSP( getActivity(),ipstr, portstr, String.format("%s&channel=%d.sdp",serialno,id), Constants.CAREYE_VCODE_H264,20,Constants.CAREYE_ACODE_AAC,1,8000);
        MediaCodecManager.getInstance().StartUpload(getActivity(),index, mPusher);
        mCamera.setPreviewCallback(previewCallback);
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
           mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
           Camera.Parameters parameters = mCamera.getParameters();
           int previewFormat =  debugger.getNV21Convertor().getPlanar() ? ImageFormat.YV12 : ImageFormat.NV21;
           parameters.setPreviewFormat(previewFormat);
           parameters.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
          mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            mCamera = null;
        }
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
           // mCamera.setPreviewCallback(previewCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTextureView.setRotation(90);
        mTextureView.setScaleX(2f);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera.setPreviewCallback(null);
        mCamera = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

}
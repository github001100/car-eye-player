package org.Careye.easyplayer.fragments;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.IOException;

public class PushFragment extends Fragment implements TextureView.SurfaceTextureListener
{
    private Camera mCamera;
    private TextureView mTextureView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200);

        LinearLayout view = new LinearLayout(getActivity());
        view.setLayoutParams(lp);

        mTextureView = new TextureView(getActivity());
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1200));

        view.addView(mTextureView);

        return view;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        mCamera = Camera.open();
        try
        {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        mTextureView.setRotation(90);
        mTextureView.setScaleX(2f);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1)
    {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture)
    {

    }
}
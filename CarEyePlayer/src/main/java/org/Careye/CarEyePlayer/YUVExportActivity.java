/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/
package org.Careye.CarEyePlayer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.Careye.CarEyePlayer.fragments.PlayFragment;
import org.Careye.CarEyePlayer.fragments.YUVExportFragment;
import org.Careye.video.Client;
import org.Careye.rtsp.player.R;

public class YUVExportActivity extends AppCompatActivity {

    private YUVExportFragment mRenderFragment;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("play_url");
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.yuv_export_activity);
        if (savedInstanceState == null) {
            boolean useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
            YUVExportFragment fragment = YUVExportFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, null);
            fragment.setScaleType(PlayFragment.ASPACT_RATIO_CROPE_MATRIX);
            getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment,"first").commit();
            mRenderFragment = fragment;
        } else {
            mRenderFragment = (YUVExportFragment) getSupportFragmentManager().findFragmentByTag("first");
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void onToggleAspectRatio(View view) {
        YUVExportFragment f =mRenderFragment;
        if (f == null) return;
        f.setScaleType(++i);
        switch (i){
            case PlayFragment.ASPACT_RATIO_INSIDE: {
                Toast.makeText(this,"等比例居中",Toast.LENGTH_SHORT).show();
            }
            break;
            case PlayFragment.ASPACT_RATIO_CENTER_CROPE: {
                Toast.makeText(this,"等比例居中裁剪视频",Toast.LENGTH_SHORT).show();
            }
            break;
            case PlayFragment.FILL_WINDOW:{
                Toast.makeText(this,"拉伸视频,铺满区域",Toast.LENGTH_SHORT).show();
            }
            break;
            case PlayFragment.ASPACT_RATIO_CROPE_MATRIX:{
                Toast.makeText(this,"等比例显示视频,可拖拽显示隐藏区域.",Toast.LENGTH_SHORT).show();
            }
            break;
        }
        if (i == PlayFragment.FILL_WINDOW){
            i = 0;
        }


    }

    public void onToggleDraw(View view) {
        YUVExportFragment f =mRenderFragment;
        if (f == null) return;
        f.toggleDraw();
    }
}

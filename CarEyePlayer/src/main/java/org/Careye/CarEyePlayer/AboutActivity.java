/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/
package org.Careye.CarEyePlayer;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import org.Careye.rtsp.player.R;
import org.Careye.rtsp.player.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = DataBindingUtil.setContentView(this, R.layout.activity_about);

        setSupportActionBar(binding.toolbar);

            binding.title.setText("car-eye-player RTSP播放器：");
            binding.desc.setText("car-eye-player RTSP是由Car-Eye开源团队开发的流媒体播放器，支持linux,IOS,windows等多种平台进行播放，支持RTSP和RTMP两种流媒体格式");
            binding.desc.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = new SpannableString("https://github.com/Car-eye-team");
            //设置下划线文字
            spannableString.setSpan(new URLSpan("https://github.com/Car-eye-team"), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            binding.imageView.setImageResource(R.drawable.qrcode);
    }
}

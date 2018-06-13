package org.Careye.easyplayer;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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

            binding.title.setText("car-eye-player RTSP播放器：");//修改名称 测试
            binding.desc.setText("car-eye-player RTSP是由Car-Eye开源团队开发 者开发和维护的一个RTSP播放器项目，目前 支持Windows/Android/iOS，视频支持 H.264/H.265/MPEG4/MJPEG，音频支持 G711A/G711U/G726/AAC，支持RTSP over  TCP/UDP切换，支持硬解码，是一套极佳的 RTSP播放组件！项目地址：");

            binding.desc.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = new SpannableString("https://github.com/car-eye/EasyPlayer");
            //设置下划线文字
            spannableString.setSpan(new URLSpan("https://github.com/car-eye/EasyPlayer"), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            binding.imageView.setImageResource(PlaylistActivity.isPro()?R.drawable.qrcode:R.drawable.qrcode_pro);
    }
}

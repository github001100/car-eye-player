/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/
package org.Careye.CarEyePlayer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import org.Careye.CarEyePlayer.fragments.ImageFragment;
import org.Careye.CarEyePlayer.fragments.PushFragment;
import org.Careye.push.util.Constants;
import org.Careye.video.Client;
import org.Careye.CarEyePlayer.fragments.PlayFragment;
import org.Careye.rtsp.player.R;
import org.Careye.rtsp.player.databinding.ActivityMainBinding;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class PlayActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0x111;
    private GestureDetectorCompat mDetector;
    private SoundPool mSoundPool;
    private int mTalkPictureSound;
    private int mActionStartSound;
    private int mActionStopSound;
    private PlayFragment mRenderFragment;
    private float mAudioVolumn;
    private float mMaxVolume;
    private ActivityMainBinding mBinding;
    private PlayFragment myFragment;
    private PushFragment m_pushFragment;
    private long mLastReceivedLength;
    private String url = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";//设置一个默认地址
    private boolean useUDP;
    private boolean mUpload = false;
    private int mChannel = 0;
    private Camera camera;
    // 第一次按下返回键的事件
    private long firstPressedTime;

    private final Handler mHandler = new Handler();
    private final Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long length = mRenderFragment.getReceivedStreamLength();
            if (length == 0) {
                mLastReceivedLength = 0;
            }
            if (length < mLastReceivedLength) {
                mLastReceivedLength = 0;
            }
            mBinding.liveVideoBar.streamBps.setText((length - mLastReceivedLength) / 1024 + "Kbps");
            mLastReceivedLength = length;

            mHandler.postDelayed(this, 1000);
        }
    };
    private Runnable mResetRecordStateRunnable = new Runnable() {
        @Override
        public void run() {
            ImageView mPlayAudio = mBinding.liveVideoBar.liveVideoBarRecord;
            mPlayAudio.setImageState(new int[]{}, true);
            mPlayAudio.removeCallbacks(mResetRecordStateRunnable);
        }
    };

    public void onEnableOrDisablePlayAudio(View view) {
        boolean enable = mRenderFragment.toggleAudioEnable();
        ImageView mPlayAudio = (ImageView) view;
        mPlayAudio.setImageState(enable ? new int[]{android.R.attr.state_pressed} : new int[]{}, true);
    }

    public void onTakePicture(View view) {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            File file = new File(TheApp.sPicturePath);
            file.mkdirs();
            file = new File(file, new SimpleDateFormat("yy_MM_dd HH_mm_ss").format(new Date()) + ".jpg");
            mRenderFragment.takePicture(file.getPath());
            if (mSoundPool != null) {
                mSoundPool.play(mTalkPictureSound, mAudioVolumn, mAudioVolumn, 1, 0, 1.0f);
            }
        } else {
            requestWriteStorage(true);
        }
    }

    private void requestWriteStorage(final boolean toTakePicture) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            new AlertDialog.Builder(this).setMessage(toTakePicture ? "car-eye-player需要使用写文件权限来抓拍" : "car-eye-player需要使用写文件权限来录像").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(PlayActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE + (toTakePicture ? 0 : 1));
                }
            }).show();
        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE + (toTakePicture ? 0 : 1));

            // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE + 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
                        onTakePicture(mBinding.liveVideoBar.liveVideoBarTakePicture);
                    } else {
                        onRecordOrStop(mBinding.liveVideoBar.liveVideoBarRecord);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }

    public void onRecordOrStop(View view) {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (mRenderFragment != null) {
                boolean recording = mRenderFragment.onRecordOrStop();
                ImageView mPlayAudio = (ImageView) view;
                mPlayAudio.setImageState(recording ? new int[]{android.R.attr.state_checked} : new int[]{}, true);

                if (recording) mPlayAudio.postDelayed(mResetRecordStateRunnable, 200);
            }
        } else {
            requestWriteStorage(false);
        }
    }

    protected void initSoundPool() {
        if (true)
            return;
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioVolumn = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attributes).build();
        } else {
            mSoundPool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
        }
        mTalkPictureSound = mSoundPool.load("/system/media/audio/ui/camera_click.ogg", 1);
        mActionStartSound = mSoundPool.load(this, R.raw.action_start, 1);
        mActionStopSound = mSoundPool.load(this, R.raw.action_stop, 1);
    }

    protected void releaseSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public void onTakePictureThumbClicked(View view) {
        String path = (String) view.getTag();
//        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gallery_image_view");
//        Intent intent = new Intent(this, ImageActivity.class);
//        intent.putExtra("extra-uri", Uri.fromFile(new File(path)));
////        ActivityCompat.startActivity(this, intent, compat.toBundle());
//        startActivity(intent);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, ImageFragment.newInstance(Uri.fromFile(new File(path)))).addToBackStack(null).commit();
    }

    /**
     * 请求添加新播放窗口
     */
    public void onAddWindow() {
        new AlertDialog.Builder(PlayActivity.this).setTitle("新的播放窗口").setItems(new CharSequence[]{"选取历史播放记录", "手动输入视频源"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    final SharedPreferences preferences = getSharedPreferences("PlaylistActivity", MODE_PRIVATE);
                    JSONArray mArray;
                    try {
                        mArray = new JSONArray(preferences.getString("play_list", "[\"rtsp://121.41.73.249/1001_home.sdp\"]"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mArray = new JSONArray();
                    }
                    final CharSequence[] array = new CharSequence[mArray.length()];
                    if (array.length == 0) {
                        Toast.makeText(PlayActivity.this, "没有历史播放记录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < array.length; i++) {
                        array[i] = mArray.optString(i);
                    }
                    new AlertDialog.Builder(PlayActivity.this).setTitle("新的播放窗口").setItems(array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addVideoSource(String.valueOf(array[which]));
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
                } else {
                    final EditText edit = new EditText(PlayActivity.this);
                    final int hori = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                    final int verti = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                    edit.setPadding(hori, verti, hori, verti);
                    final AlertDialog dlg = new AlertDialog.Builder(PlayActivity.this).setView(edit).setTitle("请输入播放地址").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String url = String.valueOf(edit.getText());
                            if (TextUtils.isEmpty(url)) {
                                return;
                            }
                            final SharedPreferences preferences = getSharedPreferences("PlaylistActivity", MODE_PRIVATE);
                            JSONArray mArray;
                            try {
                                mArray = new JSONArray(preferences.getString("play_list", "[\"rtsp://121.41.73.249/1001_home.sdp\"]"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mArray = new JSONArray();
                            }
                            mArray.put(url);
                            preferences.edit().putString("play_list", String.valueOf(mArray)).apply();
                            addVideoSource(url);
                        }
                    }).setNegativeButton("取消", null).create();
                    dlg.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                    dlg.show();
                }
            }
        }).show();
    }

    /**
     * 增加一个视频窗口。每一个PlayFragment表示一个播放窗口,在这里会增加一个PlayFragment。
     *
     * @param url
     */
    private void addVideoSource(String url) {
        final FrameLayout item = new FrameLayout(PlayActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        item.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            item.setId(View.generateViewId());
        } else {
            item.setId(generateViewId());
        }
        mBinding.playerContainer.addView(item);
        boolean useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
        getSupportFragmentManager().beginTransaction().add(item.getId(), PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, null)).commit();
    }

    /**
     * 删除一个播放窗口
     *
     * @param id
     */
    public void onRemoveVideoFragment(int id) {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(id)).commit();
        mBinding.playerContainer.removeView(mBinding.playerContainer.findViewById(id));
    }

    /**
     * 播放窗口被点击,此时app会弹出一个OptionFragment,并且绑定被点击的fragment(这样就会使能删除按钮,点击删除按钮,即可把此播放窗口删除)
     *
     * @param fragment
     */
    public void onPlayFragmentClicked(PlayFragment fragment) {
        // 被绑定的窗口,呈选中状态
        boolean enableMultiWnd = false;
        if (!enableMultiWnd) {
            return;
        }
        fragment.setSelected(true);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, VideoWindowOptionMenuFragment.newInstance(fragment.getId())).addToBackStack(null).commit();
    }

    /**
     * 播放窗口以外的区域被点击。此时app也弹出OptionFragment,但是不绑定播放窗口。这样的话不使能删除按钮。
     *
     * @param view
     */
    public void onOpenOptionMenu(View view) {
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, VideoWindowOptionMenuFragment.newInstance(0)).addToBackStack(null).commit();
    }

    /**
     * 删除OptionFragment
     *
     * @param optionFragment
     */
    public void onRemoveOptionMenu(VideoWindowOptionMenuFragment optionFragment) {
        getSupportFragmentManager().beginTransaction().remove(optionFragment).commit();
        getSupportFragmentManager().popBackStack();

        // 如果之前有绑定窗口,那先反选之
        int attachedPlayFragmentId = optionFragment.getAttachedPlayFragmentId();
        if (attachedPlayFragmentId != 0) {
            PlayFragment fragment = (PlayFragment) getSupportFragmentManager().findFragmentById(attachedPlayFragmentId);
            if (fragment != null) {
                fragment.setSelected(false);
            }
        }
    }

    /**
     * 切换屏幕方向
     */
    public void onToggleOrientation() {
        setRequestedOrientation(isLandscape() ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in View.setId(int)
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //String url = getIntent().getStringExtra("play_url");//"rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
        //String  url = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov00";//测试地址固定
        url = mBinding.inputUrl.getText().toString();
        mBinding.inputUrl.clearFocus();
        myFragment = new PlayFragment();//暂时未用的的
        m_pushFragment = new PushFragment();
        Bundle bundle = new Bundle();
        bundle.putString("MainActivity", "Hello,Fragment"); //首先有一个Fragment对象 调用这个对象的setArguments(bundle)传递数据 myFragment.setArguments(bundle);

        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (savedInstanceState == null) {

            ResultReceiver rr = getIntent().getParcelableExtra("rr");
            if (rr == null) {
                rr = new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        if (resultCode == PlayFragment.RESULT_REND_STARTED) {
                            onPlayStart();
                        } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
                            onPlayStoped();
                        } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
                            onVideoDisplayed();
                        }
                    }
                };
            }

            useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
            PlayFragment fragment = PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, rr);//创建PlayFragment 实例
            getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment).commit();
            mRenderFragment = fragment;

        } else {
            mRenderFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.render_holder);
        }
        initSoundPool();
        mBinding.liveVideoBar.liveVideoBarEnableAudio.setEnabled(false);
        mBinding.liveVideoBar.liveVideoBarTakePicture.setEnabled(false);
        mBinding.liveVideoBar.liveVideoBarRecord.setEnabled(false);

        LinearLayout container = mBinding.playerContainer;
        if (isLandscape()) {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setNavVisibility(false);
            // 横屏情况下,播放窗口横着排开
            container.setOrientation(LinearLayout.HORIZONTAL);
            mBinding.renderHolder.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mRenderFragment.enterFullscreen();
        } else { // 竖屏,取消全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setNavVisibility(true);
            // 竖屏情况下,播放窗口竖着排开
            container.setOrientation(LinearLayout.VERTICAL);
            mBinding.renderHolder.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.render_wnd_height);
            mRenderFragment.quiteFullscreen();
        }
        mBinding.msgTxt.setMovementMethod(new ScrollingMovementMethod());//显示一些日志信息
        mBinding.toolbarSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWindowMin();
                Intent intent_set = new Intent(getApplicationContext(), SettingsActivity.class);//设置
                intent_set.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent_set);
                //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        mBinding.toolbarHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            }
        });

        SharedPreferences pref = getSharedPreferences("mydata", MODE_MULTI_PROCESS);
        final String sip = pref.getString(getString(R.string.key_ip), Constants.SERVER_IP);
        final String key_port = pref.getString(getString(R.string.key_port), Constants.SERVER_PORT);
        final String key_app_name = pref.getString(getString(R.string.key_app_name), Constants.RTMP_APP);

        mBinding.btnPlayA.setOnClickListener(new View.OnClickListener() {
            int a = 1;

            @Override
            public void onClick(View view) {
                if (a == 1) {
                    mRenderFragment.mStreamRender.pause();//暂停播放
                    a = 2;
                } else {
                    mRenderFragment.mStreamRender.resume();
                    a = 1;
                }

            }
        });
        //开始播放视频
        mBinding.btnPlayB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = mBinding.inputUrl.getText().toString();
                ResultReceiver rr = getIntent().getParcelableExtra("rr");
                if (rr == null) {
                    rr = new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == PlayFragment.RESULT_REND_STARTED) {
                                onPlayStart();
                            } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
                                onPlayStoped();
                            } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
                                onVideoDisplayed();
                            }
                        }
                    };
                }
                // useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
                PlayFragment fragment = PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, rr);//创建PlayFragment 实例
                getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment).commit();
                mRenderFragment = fragment;

                //mRenderFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.render_holder);

                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
            }
        });
        //点击内 推流
        mBinding.btnPushStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mBinding.btnPushUrl.setText("rtsp://" + sip + ":" + key_port + "/" + key_app_name + ".sdp");

                if (mUpload == false) {
                    mChannel = m_pushFragment.StartVideoUpload(Constants.SERVER_IP, Constants.SERVER_PORT, Constants.STREAM_NAME);
                    mUpload = true;
                } else {
                    m_pushFragment.StopVideoUpload(mChannel);
                    mUpload = false;
                }*/
            }
        });

        //切换摄像头
        mBinding.btnSwitchCammars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //m_pushFragment.closeCamera();
                PushFragment.mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

                m_pushFragment.openCamera();
            }
        });
        //推流按钮最右边的那个 已隐藏
        mBinding.btnPusherPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  if (mUpload == false) {
                    mChannel = m_pushFragment.StartVideoUpload(Constants.SERVER_IP, Constants.SERVER_PORT, Constants.STREAM_NAME);
                    mUpload = true;
                } else {
                    m_pushFragment.StopVideoUpload(mChannel);
                    mUpload = false;
                }*/
                mBinding.btnPushUrl.setText("rtsp://" + sip + ":" + key_port + "/" + key_app_name + ".sdp");
                if (mUpload == false) {
                    mChannel = m_pushFragment.StartVideoUpload(Constants.SERVER_IP, Constants.SERVER_PORT, Constants.STREAM_NAME);
                    mUpload = true;
                } else {
                    m_pushFragment.StopVideoUpload(mChannel);
                    mUpload = false;
                }
            }
        });
        //工具栏暂停按钮
        mBinding.toolbarAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mRenderFragment.mSurfaceView.setVisibility(View.GONE);
                mRenderFragment.mStreamRender.pause();//暂停播放
            }
        });
        //工具兰播放按钮图标
        mBinding.toolbarPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRenderFragment.mStreamRender.resume();
                url = mBinding.inputUrl.getText().toString();
                ResultReceiver rr = getIntent().getParcelableExtra("rr");
                if (rr == null) {
                    rr = new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == PlayFragment.RESULT_REND_STARTED) {
                                onPlayStart();
                            } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
                                onPlayStoped();
                            } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
                                onVideoDisplayed();
                            }
                        }
                    };
                }
                // useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
                PlayFragment fragment = PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, rr);//创建PlayFragment 实例
                getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment).commit();
                mRenderFragment = fragment;

                //mRenderFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.render_holder);

                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();

            }
        });
        //推流 按钮
        mBinding.btnPusher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(PlayActivity.this, StreamActivity.class));
                //mRenderFragment.onDetach();
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(PlayActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                }
              /*  if(m_pushFragment!= null) {
                    getSupportFragmentManager().beginTransaction().add(R.id.render_holder, m_pushFragment).commit();

                }*/
                mRenderFragment.mStreamRender.pause();//停止播放
                //mRenderFragment.stopRending();//停止播放
                Intent i = new Intent(PlayActivity.this, PlayActivity.class);
                i.putExtra("play_url", mBinding.inputUrl.getText().toString());
                ResultReceiver rr = getIntent().getParcelableExtra("rr");
                if (rr == null) {
                    rr = new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
//                            if (resultCode == PlayFragment.RESULT_REND_STARTED) {
//                                onPlayStart();
//                            } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
//                                onPlayStoped();
//                            } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
//                                onVideoDisplayed();
//                            }
                        }
                    };
                }
                PushFragment pushfragment = PushFragment.newInstance(rr);//创建PlayFragment 实例
                getSupportFragmentManager().beginTransaction().add(R.id.render_holder, pushfragment).commit();
            }
        });
        //播放器按钮
        mBinding.btnPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRenderFragment.mStreamRender.pause();//停止播放
                Intent i = new Intent(PlayActivity.this, PlayActivity.class);
                i.putExtra("play_url", mBinding.inputUrl.getText().toString());
                ResultReceiver rr = getIntent().getParcelableExtra("rr");
                if (rr == null) {
                    rr = new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == PlayFragment.RESULT_REND_STARTED) {
                                onPlayStart();
                            } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
                                onPlayStoped();
                            } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
                                onVideoDisplayed();
                            }
                        }
                    };
                }
                PlayFragment fragment = PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, rr);//创建PlayFragment 实例
                getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment).commit();
                //fragment.onDestroy();
                /*url = mBinding.inputUrl.getText().toString();
                ResultReceiver rr = getIntent().getParcelableExtra("rr");
                if (rr == null) {
                    rr = new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == PlayFragment.RESULT_REND_STARTED) {
                                onPlayStart();
                            } else if (resultCode == PlayFragment.RESULT_REND_STOPED) {
                                onPlayStoped();
                            } else if (resultCode == PlayFragment.RESULT_REND_VIDEO_DISPLAYED) {
                                onVideoDisplayed();
                            }
                        }
                    };
                }
                // useUDP = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_udp_mode), false);
                PlayFragment fragment = PlayFragment.newInstance(url, useUDP ? Client.TRANSTYPE_UDP : Client.TRANSTYPE_TCP, rr);//创建PlayFragment 实例
                getSupportFragmentManager().beginTransaction().add(R.id.render_holder, fragment).commit();
                mRenderFragment = fragment;

                //mRenderFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.render_holder);

                Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();*/

            }
        });

    }
    public void OnClickTest(View view){
        Toast.makeText(getApplicationContext(),"暂未开发，敬请期待",Toast.LENGTH_SHORT).show();
    }
    private void onVideoDisplayed() {
        mBinding.liveVideoBar.liveVideoBarTakePicture.setEnabled(true);
        mBinding.liveVideoBar.liveVideoBarRecord.setEnabled(true);
    }

    private void onPlayStart() {
        boolean enable = mRenderFragment.isAudioEnable();
        mBinding.liveVideoBar.liveVideoBarEnableAudio.setImageState(enable ? new int[]{android.R.attr.state_pressed} : new int[]{}, true);
        mBinding.liveVideoBar.liveVideoBarEnableAudio.setEnabled(true);
        mHandler.postDelayed(mTimerRunnable, 1000);

        mBinding.liveVideoBar.liveVideoBarTakePicture.setEnabled(false);
        mBinding.liveVideoBar.liveVideoBarRecord.setEnabled(false);
        //mRenderFragment.mSurfaceView.setVisibility(View.VISIBLE);

    }

    private void onPlayStoped() {
        mBinding.liveVideoBar.liveVideoBarEnableAudio.setEnabled(false);
        mHandler.removeCallbacks(mTimerRunnable);
        //mRenderFragment.mSurfaceView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        releaseSoundPool();
        super.onDestroy();
    }

    private boolean isLandscape() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LinearLayout container = mBinding.playerContainer;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setNavVisibility(false);
            // 横屏情况下,播放窗口横着排开
            container.setOrientation(LinearLayout.HORIZONTAL);
            mBinding.renderHolder.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mRenderFragment.enterFullscreen();
        } else {
            // 竖屏,取消全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setNavVisibility(true);
            // 竖屏情况下,播放窗口竖着排开
            container.setOrientation(LinearLayout.VERTICAL);
            mBinding.renderHolder.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.render_wnd_height);
            mRenderFragment.quiteFullscreen();
        }
    }

    public boolean multiWindows() {
        LinearLayout container = (LinearLayout) findViewById(R.id.player_container);
        return container.getChildCount() > 1;
    }

    public void setNavVisibility(boolean visible) {
        if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
            int newVis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (!visible) {
                // } else {
                // newVis &= ~(View.SYSTEM_UI_FLAG_LOW_PROFILE |
                // View.SYSTEM_UI_FLAG_FULLSCREEN |
                // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                newVis |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            }
            // If we are now visible, schedule a timer for us to go invisible.
            // Set the new desired visibility.
            getWindow().getDecorView().setSystemUiVisibility(newVis);
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_url) {

        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onOpenFileDirectory(View view) {
        Intent i = new Intent(this, MediaFilesActivity.class);
        startActivity(i);
    }

    public void onEvent(PlayFragment playFragment, int err, String msg) {
        mBinding.msgTxt.append(String.format("[%s]\t%s\n", new SimpleDateFormat("HH:mm:ss").format(new Date()), msg));
    }

    public void onRecordState(int status) {
        ImageView mPlayAudio =
                mBinding.liveVideoBar.liveVideoBarRecord;
        mPlayAudio.setImageState(status == 1 ? new int[]{android.R.attr.state_checked} : new int[]{}, true);
        mPlayAudio.removeCallbacks(mResetRecordStateRunnable);
    }

    public void onFullscreen(View view) {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setNavVisibility(false);
        // 横屏情况下,播放窗口横着排开
        mBinding.renderHolder.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        mRenderFragment.enterFullscreen();
    }

    @Override
    public void onBackPressed() {//后台进成
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setNavVisibility(true);
            mBinding.renderHolder.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.render_wnd_height);
            mBinding.renderHolder.requestLayout();

            mRenderFragment.quiteFullscreen();

            return;
        }

        // System.currentTimeMillis() 当前系统的时间
        /*if (System.currentTimeMillis() - firstPressedTime < 2000) {
            super.onBackPressed();
        } else {
            Toast.makeText(PlayActivity.this, "再按一次退出Car-eye", Toast.LENGTH_SHORT).show();
            firstPressedTime = System.currentTimeMillis();
        }*/
        super.onBackPressed();
    }
}

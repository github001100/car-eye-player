package org.Careye.easyplayer;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.sh.camera.version.VersionBiz;

import org.Careye.rtsp.player.R;
import org.Careye.rtsp.player.databinding.ActivitySettingBinding;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {

    public ActivitySettingBinding mBinding;
    EditText et1, et2, et3, et4, et5, et6;

    /**是否有版本检测跳转至该界面*/
    private boolean fromUpdateVersion = false;
    public static SettingsActivity instance;

    //权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    /**
     *  获取权限
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        SharedPreferences pref = getSharedPreferences("mydata",MODE_MULTI_PROCESS);
        instance = this;//有用

        verifyStoragePermissions(getInstance());


//        "114.55.107.180"
        //通用参数
        mBinding.serverIp.setText(pref.getString(getString(R.string.key_ip), TheApp.DEFAULT_SERVER_IP));//默认 服务器IP 0.0.0.0  PreferenceManager.getDefaultSharedPreferences(this)
        mBinding.serverPort.setText(pref.getString(getString(R.string.key_port), "10008"));
        //推流器设置 参数
        mBinding.appName.setText(pref.getString(getString(R.string.key_app_name), "key_app_name"));
        mBinding.appShebei.setText(pref.getString(getString(R.string.key_shebei), "key_shebei"));
        mBinding.appZhen.setText(pref.getString(getString(R.string.key_zhen), "20"));
        mBinding.appRtspUrl.setText(pref.getString(getString(R.string.key_url), "RTSP://www.car-eye.cn"));

        mBinding.transportMode.setChecked(pref.getBoolean(getString(R.string.key_udp_mode), false));
        mBinding.autoRecord.setChecked(pref.getBoolean("auto_record", false));
        mBinding.swCodec.setChecked(pref.getBoolean("use-sw-codec", false));

        //推流服务器相关信息 by fu 2018 6 8
        /*mBinding.serverIp.setText(ServerManager.getInstance().getIp());
        mBinding.serverPort.setText(ServerManager.getInstance().getPort());
        mBinding.appName.setText(ServerManager.getInstance().getapp());//应用名
        //mBinding.appZhen.setText(ServerManager.getInstance().getFramerate());
        mBinding.appShebei.setText(ServerManager.getInstance().getStreamname());//设备号
        mBinding.appRtspUrl.setText(ServerManager.getInstance().getURL());//推流RTSP 地址*/

       /* mBinding.serverIp.setText(Constants.SERVER_IP);
        mBinding.serverPort.setText(Constants.SERVER_PORT);
        mBinding.appName.setText(Constants.RTMP_APP);
        mBinding.appZhen.setText(String.valueOf(Constants.FRAMERATE));
        mBinding.appShebei.setText(Constants.STREAM_NAME);
        mBinding.appRtspUrl.setText(Constants.Default_URL);*/
        et1 = (EditText) findViewById(R.id.server_ip);
        et2 = (EditText) findViewById(R.id.server_port);
        et3 = (EditText) findViewById(R.id.app_name);
        et4 = (EditText) findViewById(R.id.app_shebei);
        et5 = (EditText) findViewById(R.id.app_rtsp_url);
        //IP  文本框事件
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("CMD", "afterTextChanged 被执行---->" + editable);
                et5.setText("rtsp://"+editable+":"+et2.getText()+"/"+et3.getText()+et4.getText()+"&channel=1.sdp");
            }
        });

        //端口 文本 事件
        et2.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("CMD", "afterTextChanged 被执行---->" + editable);
                et5.setText("rtsp://"+et1.getText()+":"+editable+"/"+et3.getText()+et4.getText()+"&channel=1.sdp");
            }
        });
        //设备号 文本事件
        et4.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("CMD", "afterTextChanged 被执行---->" + editable);
                et5.setText("rtsp://"+et1.getText()+":"+et2.getText()+"/"+et3.getText()+editable+"&channel=1.sdp");
            }
        });
        //应用名 文本事件
        et3.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("CMD", "afterTextChanged 被执行---->" + editable);
                et5.setText("rtsp://"+et1.getText()+":"+et2.getText()+"/"+editable+et4.getText()+"&channel=1.sdp");
            }
        });

        VersionBiz v = new VersionBiz(this);
        String version = "";
        try {
            version = v.getVersionName(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //tv_version.setText("v"+version);

        fromUpdateVersion = (Boolean) getIntent().getBooleanExtra("fromUpdateVersion", false);
        v.doCheckVersion(false,fromUpdateVersion);

    }

    public static SettingsActivity getInstance() {
        if (instance == null) {
            instance = new SettingsActivity();
        }
        return instance;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    //点击保存 赋值  按钮 触发
    public void onOK(View view) {
        //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        SharedPreferences.Editor editor = getSharedPreferences("mydata",MODE_MULTI_PROCESS).edit();

        editor.putString(getString(R.string.key_ip), mBinding.serverIp.getText().toString());
        editor.putString(getString(R.string.key_port), mBinding.serverPort.getText().toString());
        editor.putString(getString(R.string.key_app_name), mBinding.appName.getText().toString());
        editor.putString(getString(R.string.key_shebei), mBinding.appShebei.getText().toString());
        editor.putString(getString(R.string.key_zhen), mBinding.appZhen.getText().toString());

        editor.putString(getString(R.string.key_url), mBinding.appRtspUrl.getText().toString());

        editor.putBoolean(getString(R.string.key_udp_mode), mBinding.transportMode.isChecked());
        editor.putBoolean("auto_record", mBinding.autoRecord.isChecked());
        editor.putBoolean("use-sw-codec", mBinding.swCodec.isChecked());
        editor.apply();
        finish();
    }



    public void onWhatIpMean(View view) {
        if (mBinding.whatIpMean.getVisibility() != View.VISIBLE) {
            mBinding.whatIpMean.setVisibility(View.VISIBLE);
        } else {
            mBinding.whatIpMean.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mBinding.whatIpMean.getVisibility() == View.VISIBLE) {
                mBinding.whatIpMean.setVisibility(View.GONE);
            }
        }
        return super.onTouchEvent(event);
    }
}

/*
 * Car eye 车辆管理平台: www.car-eye.cn
 * Car eye 开源网址: https://github.com/Car-eye-team
  * Copyright 2018
*/

package org.Careye.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;

import org.Careye.CarEyePlayer.SettingsActivity;
import org.Careye.rtsp.player.R;
import org.Careye.push.util.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VersionBiz {
	public static final int METHOD_GET = 1;
	public static final int METHOD_POST = 2;
	private static final String TAG = "VersionBiz";
	private Context context;
	private boolean versionflag = false;
	private VersionInfo versionInfo;
	private ProgressDialog pBar;
	public static final String UPDATE_SAVENAME = "camera.apk";
	private boolean fromUpdateVersion;

    private static Dialog currentDlg;

	public VersionBiz(Context context) {
		super();
		this.context = context;
	}

	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			if(what == 101){
				doNewVersionUpdate();
			}else if(what == 102){
				doCurrentVersion();
			}else if(what == 1023){
				int nStep = msg.getData().getInt("step", -1);
				Log.d("", "step->" + nStep);
				if (nStep == -1)
					pBar.dismiss();
				else
					pBar.setProgress(nStep);
			}else if(what == 103){
				/** 网络连接超时*/
				//MyToast.showToast(context, "服务器连接异常,请稍后再试！", true, 0);
				//MyToast.showToast(context, "网络连接超时", true, 0);
			}else if(what == 104){
				/** json解析错误*/
				//MyToast.showToast(context, "json解析错误", true, 0);
			}else if(what == 105){
				/** 数据请求失败原因*/
				String message = (String) msg.obj;
				if(message == null || message.equals("")){
					//MyToast.showToast(context, "数据请求失败原因:"+message, true, 0);
				}
			}else if(what == 106){
				/** 网络连接超时*/
				//MyToast.showToast(context, "网络连接超时", true, 0);
			}
		};
	};

	/**
	 * 获取版本名称
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getVersionName(Context context)throws Exception {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(
				context.getPackageName(), 0);
		return packInfo.versionName;
	}

	/**
	 * 获取版本�?
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public int getVersionCode() throws Exception {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(
				context.getPackageName(), 0);
		return packInfo.versionCode;
	}

	/**
	 * 获取应用名称
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getAppName(Context context) {
		String verName = context.getResources().getText(R.string.app_name)
				.toString();
		return verName;
	}

    /**
     * 关闭查询等待消息提示框
     *
     * @param
     * @param
     * @return
     */
    private static boolean isCurrentDlgShowing() {
        return currentDlg != null && currentDlg.isShowing();
    }
    public static void popProgress(final Context context, final String msg) {
	/*	MainService.getInstance().handler.post(new Runnable() {

			@Override
			public void run() {
				ProgressDialog pd = new ProgressDialog(context);
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				currentDlg = ProgressDialog.show(context, null, msg);
				currentDlg.setCancelable(true);
				currentDlg.setCanceledOnTouchOutside(false);
			}
		});*/
    }
	/**
	 * 检查更新
	 * @param
	 * @return
	 */
	public void doCheckVersion(final boolean needDialog, boolean fromUpdateVersion) {
		this.fromUpdateVersion = fromUpdateVersion;
		if(needDialog){
			versionflag = true;
			popProgress((Activity)context, "正在检测版本");
		}
		new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				try {

					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("ak", Constants.UPDATE_APK_AK));
					pairs.add(new BasicNameValuePair("type", Constants.UPDATE_APK_TYPE));
					String url = "http://"+ ParamsBiz.getUpdateIp()+":"+ ParamsBiz.getUpdatePort()+Constants.UPDATE_URL;//异常
					HttpEntity httpEntity = getEntity(url, pairs, METHOD_POST);
					InputStream inputStream = getStream(httpEntity);
					String json = parse(inputStream);
					JSONObject obj = new JSONObject(json);
					int status = obj.getInt("status");
					if (status == 0) {
						String locVersion = getVersionName(context);
						versionInfo = new VersionInfo();
						versionInfo.setVersionId(obj.optString("versionId", ""));
						versionInfo.setVersionIndex(obj.optString("versionIndex", ""));
						versionInfo.setDownloadPath(obj.optString("uploadFile", ""));
						versionInfo.setCreateTime(obj.optString("createTime", ""));
						versionInfo.setDesc(Html.fromHtml(obj.optString("upgradecontent", ""))
								.toString());
						//							String verionid = versionInfo.getVersionId().toLowerCase();
						String verion = versionInfo.getVersionIndex().replace("v", "");
						if(getVersion(locVersion, verion)){
							//if (!("v"+locVersion).equals(verion)) {
							versionInfo.setType(1);
							handler.sendEmptyMessage(101);
						}else{
							if(versionflag){
								versionInfo.setType(2);
								handler.sendEmptyMessage(102);
							}
						}
					}else{
						String message = obj.getString("message");
						handler.obtainMessage(105, message).sendToTarget();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					if(needDialog){
						handler.sendEmptyMessage(104);
					}
					e.printStackTrace();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if(needDialog){
						handler.sendEmptyMessage(103);
					}
					e.printStackTrace();
				} finally{
					dismissCurrentDlg();
				}
				return null;
			}
		}.execute();

	}
    /**
     * 关闭查询等待消息提示框
     *
     * @param
     * @param
     * @return
     */
    public static void dismissCurrentDlg() {
        if (isCurrentDlgShowing()) {
            currentDlg.dismiss();
        }
    }
	public static String parse(InputStream inputStream){
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			while((line = reader.readLine()) != null){
				sb.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				inputStream.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return sb.toString().trim();
	}
	/**
	 * 向指定的资源路径发送请求获取响应实体对象并返回
	 *
	 * @param uri
	 *            资源路径
	 * @param params
	 *            向服务端发送请求时的实体数据
	 * @param method
	 *            请求方法
	 * @return
	 * @throws IOException
	 */
	public static HttpEntity getEntity(String uri, List<NameValuePair> params,
									   int method) throws IOException {
		HttpEntity entity = null;
		// 创建客户端对象
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 40000);
		client.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		// 创建请求对象
		HttpUriRequest request = null;
		switch (method) {
			case METHOD_GET:
				StringBuilder sb = new StringBuilder(uri);
				if (params != null && !params.isEmpty()) {
					sb.append('?');
					for (NameValuePair pair : params) {
						sb.append(pair.getName()).append('=')
								.append(pair.getValue()).append('&');
					}
					sb.deleteCharAt(sb.length() - 1);
				}
				request = new HttpGet(sb.toString());
				System.out.println("---------------------------"+sb.toString());
				break;
			case METHOD_POST:
//			LogUtil.i("HttpUtil.uri", uri+","+params.toString());
				request = new HttpPost(uri);
				if (params != null && !params.isEmpty()) {
					// 创建请求实体对象
					UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(
							params, HTTP.UTF_8);
					// 设置请求实体
					((HttpPost) request).setEntity(reqEntity);
				}
				break;
		}
		// 执行请求获取相应对象
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			entity = response.getEntity();
		}
		return entity;
	}
	/**
	 * 获取指定的响应实体对象的网络输入流
	 *
	 * @param entity
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 */

	public static InputStream getStream(HttpEntity entity)
			throws IllegalStateException, IOException {
		InputStream in = null;
		if (entity != null) {
			in = entity.getContent();
		}
		return in;
	}
	/**
	 * 检查更新
	 * @param context
	 * @return
	 */
	public static void doCheckVersionFirst(final Context context,final Handler handler) {
		new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				try {

					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("ak", Constants.UPDATE_APK_AK));
					pairs.add(new BasicNameValuePair("type", Constants.UPDATE_APK_TYPE));
					String url = "http://"+ ParamsBiz.getUpdateIp()+":"+ ParamsBiz.getUpdatePort()+Constants.UPDATE_URL;
					HttpEntity httpEntity = getEntity(url, pairs, METHOD_POST);
					InputStream inputStream = getStream(httpEntity);
					String json = parse(inputStream);
					JSONObject obj = new JSONObject(json);
					int status = obj.getInt("status");
					if (status == 0) {
						String locVersion = getVersionName(context);
						String verion = obj.optString("versionIndex", "").replace("v", "");
						if(getVersion(locVersion, verion)){
							handler.obtainMessage(1022).sendToTarget();
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
				return null;
			}
		}.execute();

	}
	/**
	 * 版本号比较
	 * @param version1
	 * @param version2
	 * @return
	 */
	public static boolean getVersion(String version1,String version2){

		try {
			//对比前两位
			int q1 = Integer.parseInt(version1.split("\\.")[0]);
			int q2 = Integer.parseInt(version2.split("\\.")[0]);
			if(q1 == q2){
				int qq1 = Integer.parseInt(version1.split("\\.")[1]);
				int qq2 = Integer.parseInt(version2.split("\\.")[1]);

				if(qq1 == qq2){
					int qqq1 = Integer.parseInt(version1.split("\\.")[2]);
					int qqq2 = Integer.parseInt(version2.split("\\.")[2]);
					if(qqq1 < qqq2){
						return true;
					}else{
						return false;
					}
				}else if(qq1 < qq2){
					return true;
				}else{
					return false;
				}

			}else if(q1<q2){
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}
	// 版本更新
	private void doNewVersionUpdate() {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("发现新版本 : "+versionInfo.getVersionId()+ "\n\n");
			String desc = versionInfo.desc;
			if(desc != null && desc.length() != 0 && !desc.equals("null")){
				sb.append("更新内容:" + "\n\n");
				sb.append(desc);
			}
			Dialog dialog = new AlertDialog.Builder(context)
			.setTitle("car软件更新")
			.setMessage(sb.toString())
			// 设置内容
			.setPositiveButton("更新",// 设置确定按钮
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {

					pBar = new ProgressDialog(context);
					pBar.setCanceledOnTouchOutside(false);
					pBar.setTitle("正在下载");
					pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					downFile(versionInfo.downloadPath);
					//downFile("http://192.168.1.165/Dss_Driver.apk");
				}
			})
			.setNegativeButton("暂不更新",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int whichButton) {
					dialog.dismiss();
					if(fromUpdateVersion && SettingsActivity.instance != null){
						fromUpdateVersion = false;
						SettingsActivity.instance.finish();
					}
				}
			}).create();
			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void downFile(final String path) {
		pBar.show();
		new Thread() {
			@Override
			public void run() {

				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(5000);
					pBar.setMax(conn.getContentLength());

					InputStream is = conn.getInputStream();
					File file = new File(
							Environment.getExternalStorageDirectory(),
							UPDATE_SAVENAME);
					FileOutputStream fos = new FileOutputStream(file);//异常java.io.FileNotFoundException: /storage/emulated/0/camera.apk: open failed: EACCES (Permission denied)
					BufferedInputStream bis = new BufferedInputStream(is);
					byte[] buffer = new byte[1024];
					int len;
					int total = 0;
					while ((len = bis.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
						total += len;
						Message msg = handler.obtainMessage();
						msg.getData().putInt("step", total);
						msg.what = 1023;
						handler.sendMessage(msg);
					}
					fos.flush();

					fos.close();
					bis.close();
					is.close();

					down();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	void down() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				pBar.cancel();
				update();
			}
		});
	}

	void update() {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(
				Uri.fromFile(new File(
						Environment.getExternalStorageDirectory(),
						UPDATE_SAVENAME)),
				"application/vnd.android.package-archive");
		context.startActivity(intent);

		/*SharedPreferences pre = context.getSharedPreferences(
				Constants.PREFS_NAME, context.MODE_PRIVATE);
		Editor edit = pre.edit();
		edit.putBoolean("firstTime", true);
		edit.commit();*/

//		((Activity) context).finish();
	}

	private void doCurrentVersion() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("当前版本号  "+ getVersionName(context) +", 已是最新版本。");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Dialog dialog = new AlertDialog.Builder(context)
		.setTitle("软件更新").setMessage(sb.toString())
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).create();
		dialog.show();
	}
}

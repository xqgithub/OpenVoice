package com.shannon.openvoice.util;

import static com.shannon.android.lib.extended.ExtendedKt.isBlankPlus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.shannon.android.lib.constant.ConfigConstants;
import com.shannon.android.lib.util.ToastUtil;
import com.shannon.openvoice.R;
import com.shannon.openvoice.business.main.mine.setting.SettingActivity;
import com.shannon.openvoice.model.UpdateBean;
import com.shannon.openvoice.network.SSLSocketClient;
import com.shannon.openvoice.util.notification.NotificationHelperUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Date:2022/9/14
 * Time:14:27
 * author:dimple
 */
public class UpdateAppUtils {

    private volatile static UpdateAppUtils appUtils;

    private Activity mActivity;
    private String updateUrl;
    private String latestVersion;
    private String apkName;
    //apk保存路径
    private String saveFileName = "";
    private int progressBar = 0;


    private NotificationManager mManager;
    private static final int NOTIFICATION_DOWNLOAD_ID = 5;

    public static UpdateAppUtils getInstance() {
        if (appUtils == null) {
            synchronized (UpdateAppUtils.class) {
                if (appUtils == null) {
                    appUtils = new UpdateAppUtils();
                }
            }
        }
        return appUtils;
    }

    /**
     * 初始化
     */
    public void initUtils(Activity activity, UpdateBean updateBean) {
        this.mActivity = activity;

        updateUrl = updateBean.getDownloadUrl();
//        updateUrl = "http://192.168.3.171:18080/examples/app101.apk";
        latestVersion = updateBean.getVersionNumber();
        apkName = AppUtils.getAppName() + latestVersion + ".apk";
        saveFileName = activity.getExternalFilesDir(ConfigConstants.CONSTANT.APK_DIR).getAbsolutePath() + File.separator + apkName;
    }

    /**
     * 更新UI的handler
     *
     * @param msg
     */
    private static final int DOWNLOADING = 1; //表示正在下载
    private static final int DOWNLOADED = 2; //下载完毕
    private static final int DOWNLOADFAILED = 3; //下载失败
    private static final int KILLPROCESS = 4;//杀掉程序进程

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case DOWNLOADING:
                    NotificationHelperUtils.getInstance().getBuilder().setProgress(100, progressBar, false);
                    NotificationHelperUtils.getInstance().getBuilder().setContentText(progressBar + "%");
                    Notification notification = NotificationHelperUtils.getInstance().getBuilder().build();
                    mManager.notify(NOTIFICATION_DOWNLOAD_ID, notification);
//                    LogUtils.i(ConfigConstants.TAG_ALL, "DOWNLOADING =-= " + progressBar);
                    break;
                case DOWNLOADED:
                    mManager.cancel(NOTIFICATION_DOWNLOAD_ID);
                    installAPK();
//                    LogUtils.i(ConfigConstants.TAG_ALL, "DOWNLOADED =-= ");
                    break;
                case DOWNLOADFAILED:
                    mManager.cancel(NOTIFICATION_DOWNLOAD_ID);
                    ToastUtil.INSTANCE.showCenter(mActivity.getString(R.string.desc_update_failed), "#E51B1F3E");
//                    LogUtils.i(ConfigConstants.TAG_ALL, "DOWNLOADFAILED =-= ");
                    break;
                case KILLPROCESS:
                    android.os.Process.killProcess(android.os.Process.myPid());
//                    LogUtils.i(ConfigConstants.TAG_ALL, "KILLPROCESS =-= ");
                    break;
            }
        }
    };


    /**
     * 准备去下载APK
     */
    public void readyDownloadApk(String contenttitile, String contenttext) {
        if (NotificationHelperUtils.getInstance().isNotifacationEnabled(mActivity)) {//判断是否有通知栏权限
            mManager = NotificationHelperUtils.getInstance().getNotificationManager(mActivity);
            NotificationHelperUtils.getInstance().sendNotification(
                    mActivity, SettingActivity.class,
                    NOTIFICATION_DOWNLOAD_ID, contenttitile, contenttext,
                    true);
            downloadAPK();
        } else {
            NotificationHelperUtils.getInstance().openPermission(mActivity);
        }
    }


    /**
     * 下载apk
     */
    private OkHttpClient okHttpClient;
    private Request request;
    private Call call;
    private OkHttpClient.Builder httpClientBuilder;


    private void downloadAPK() {
        httpClientBuilder = new OkHttpClient.Builder();


        okHttpClient = httpClientBuilder.sslSocketFactory(
                        SSLSocketClient.getSSLSocketFactory(),
                        (X509TrustManager) (SSLSocketClient.getTrustManager())[0]
                )
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
        ;//okHttpClient = new OkHttpClient();
        request = new Request.Builder().url(updateUrl).build();
        call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = Message.obtain();
                message.what = DOWNLOADFAILED;
                mHandler.sendMessageDelayed(message, 10);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(saveFileName);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        progressBar = (int) (sum * 1.0f / total * 100);
                        //更新进度
                        mHandler.sendEmptyMessage(DOWNLOADING);
                        if (sum == total) {
                            mHandler.removeMessages(DOWNLOADING);
                        }
                        LogUtils.i("sum =-= " + sum, "total =-= " + total);
                    }
                    fos.flush();
                    //下载完成通知安装
                    mHandler.sendEmptyMessage(DOWNLOADED);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(DOWNLOADFAILED);
                } finally {
                    is.close();
                    fos.close();
                }
            }
        });
    }

    /**
     * 下载完成后自动安装apk
     */
    private void installAPK() {

    }

    /**
     * 将文件转换成uri
     *
     * @return
     */
    public Uri getUriForFile(File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(mActivity, AppUtils.getAppPackageName() + ".fileprovider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }


    public void onDestroy() {
        //handler销毁，防止内存泄漏
        if (!isBlankPlus(mHandler)) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}

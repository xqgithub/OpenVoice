package com.shannon.openvoice.util.notification;

import static com.shannon.android.lib.extended.ExtendedKt.isBlankPlus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shannon.android.lib.constant.ConfigConstants;

/**
 * Date:2022/9/14
 * Time:14:03
 * author:dimple
 */
public class NotificationBrodcaseReceiver extends BroadcastReceiver {

    public static ICallBack miCallBack;

    public static void setiCallBack(ICallBack iCallBack) {
        miCallBack = iCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!isBlankPlus(action)) {
            if (ConfigConstants.CONSTANT.NOTIFACATIO_CLOSE.equals(action)) {
                miCallBack.callBack(intent.getIntExtra("id", 0));
            }
        }
    }


    /**
     * 回调接口
     */
    public interface ICallBack {
        void callBack(int id);
    }
}

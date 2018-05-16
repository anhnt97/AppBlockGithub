package com.example.ngothanh.appblock.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Broadcast extends BroadcastReceiver {
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    public  Broadcast(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("check home", "onReceive: ");
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            Log.d("check home", "onReceive:_" + reason);
            if (reason != null) {
//                Log.e(TAG, "action:" + action + ",reason:" + reason);
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
//                        mListener.onHomePressed();
                } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
//                        mListener.onHomeLongPressed();

                }
            }
        }
    }
}

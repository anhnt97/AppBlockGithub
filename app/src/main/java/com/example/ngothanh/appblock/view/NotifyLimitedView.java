package com.example.ngothanh.appblock.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by ngoth on 3/9/2018.
 */

public class NotifyLimitedView extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "notifyLimitView";
    public boolean isPressButtonBack;
    public boolean isPressButtonHome;
    public boolean isPressButtonApp;
    private  InnerRecevier innerRecevier;
    private Broadcast broadcast;

    public NotifyLimitedView(Context context) {
        super(context);
        isPressButtonBack = false;
        isPressButtonHome = false;
        isPressButtonApp = false;

//        innerRecevier= new InnerRecevier();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "back: ");
//            System.exit(1);
            isPressButtonBack = true;
        }
        if (keyCode==KeyEvent.KEYCODE_HOME){
            Log.d(TAG, "Home_1");
        }
        if (event.getKeyCode()== KeyEvent.KEYCODE_HOME){
            Log.d(TAG, "Home_2");
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, keyCode+"__" +event.getKeyCode());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "back: ");
//            System.exit(1);
            isPressButtonBack = true;
        }
        if (event.getKeyCode()== KeyEvent.KEYCODE_HOME){
            Log.d(TAG, "Home");
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d(TAG, "home: ");
            isPressButtonHome = true;
        }
        if (keyCode == KeyEvent.KEYCODE_T) {
            Log.d(TAG, "gad: ");
            isPressButtonApp = true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
    }


    class InnerRecevier extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("check home", "onReceive: ");
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d("check home", "onReceive:_" + reason);
                if (reason != null) {
                    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
//                        mListener.onHomePressed();
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
//                        mListener.onHomeLongPressed();

                    }
                }
            }
        }
    }

}

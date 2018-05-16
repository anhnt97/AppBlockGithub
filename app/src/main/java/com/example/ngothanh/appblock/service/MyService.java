package com.example.ngothanh.appblock.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.inputmethodservice.Keyboard;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.EventLog;
import android.util.EventLog.Event;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.sqlite.Locution;
import com.example.ngothanh.appblock.view.Broadcast;
import com.example.ngothanh.appblock.view.NotifyLimitedView;
import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.sqlite.AppLimited;
import com.example.ngothanh.appblock.sqlite.DatabaseLimited;
import com.example.ngothanh.appblock.view.demo;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.EventListener;
import java.util.List;
import java.util.Random;

public class MyService extends Service implements View.OnTouchListener {
    private static final String TAG = "MyService";
    private final IBinder myBinder = new LocalBinder();
    private ArrayList<AppLimited> appLimiteds;
    private boolean isOpen = false;
    private DatabaseLimited databaseLimited = new DatabaseLimited(this);
    private String rememberPackageName = "";
    private AppLimited appIsRunning;
    private long timeLastUpdated = 0;
    private long timeNow;
    private long isLimited = 0;
    private long timeOnRun = 0;
    private String tempRememberPackageName[] = new String[2];
    private boolean countOpen = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private NotifyLimitedView notifyLimitedView;


    @Override
    public void onCreate() {
        tempRememberPackageName[0] = "";
        tempRememberPackageName[1] = "";
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Toast.makeText(this, "Chạy service", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "run Sẻvice");
        runCheckLimitApp();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class LocalBinder extends Binder {
    }

    public void runCheckLimitApp() {
        final Context context = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    timeNow = System.currentTimeMillis();
                    updateListDatabase();
                    handlingService(context);
                }
            }
        };
        thread.start();
    }

    public void handlingService(final Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        String temp = "";
//        if (Build.VERSION.SDK_INT >= 21) {
//
//
//        } else {
        assert manager != null;
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        temp = componentInfo.getPackageName();
//        }
        Log.d(TAG, "package Name: " + temp);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        //nho gia tri cua package trong lan mo dau tien
        if (!isOpen) {
            rememberPackageName = temp;
            tempRememberPackageName[0] = temp;
        }
        if (!rememberPackageName.equals(temp)) {
            if (appIsRunning.isTypeIsCountOpen() == 0) {
                long countDownMinus = System.currentTimeMillis() - timeOnRun;
                appIsRunning.setCountDown(appIsRunning.getNumberLimited() - (int) countDownMinus);
                databaseLimited.updateToLimitedDatabase(appIsRunning);
                timeOnRun = 0;
            }
//            if (!tempRememberPackageName[1].equals(temp)) {
////                countOpen = true;
////            } else {
////                countOpen = false;
////            }
////            tempRememberPackageName[1] = tempRememberPackageName[1];
////            tempRememberPackageName[0] = temp;


            isLimited = 0;
            isOpen = false;
            appIsRunning = null;
            rememberPackageName = temp;
        } else {
            //Chi thuc hien khi man hinh mo
            if (isScreenOn) {
                for (AppLimited limited : appLimiteds) {
                    if (limited.getPackageName().equals(temp)) {
                        appIsRunning = limited;
                        break;
                    }
                }
                if (appIsRunning != null) {
                    if (appIsRunning.isTypeIsCountOpen() == 1) {
                        //check con so lan gioi han va chua tung mo
                        if (appIsRunning.getCountDown() >= 0) {
                            if (!isOpen || countOpen) {
                                appIsRunning.setCountDown(appIsRunning.getCountDown() - 1);
                                databaseLimited.updateToLimitedDatabase(appIsRunning);
                                isOpen = true;
                                if (appIsRunning.getCountDown() > 0) {
                                    toastMessenger(ctx, "Bạn còn " + appIsRunning.getCountDown() + " lần mở ứng dụng");
                                } else {
                                    toastMessenger(ctx, "Đây là lần sử dụng ứng dụng cuối cùng");
                                }


                            }
                        } else {
                            if (!isOpen) {
                                toastMessenger(ctx, "Hết lượt mở ứng dụng");
                                isOpen = true;
                            }
                            switch (appIsRunning.getLevel()) {
                                case 1:
                                    if (isLimited == 0) {
                                        showNotifyDialogLimited1();
                                        isLimited = System.currentTimeMillis();
                                    }

                                    if (System.currentTimeMillis() - isLimited >= (1 * 60 * 1000)) {
                                        showNotifyDialogLimited1();
                                        isLimited = System.currentTimeMillis();
                                    }
                                    break;
                                case 2:
                                    if (isLimited == 0) {
                                        showNotifyDialogLimited2(ctx);
                                        isLimited = System.currentTimeMillis();
                                    }

                                    if (System.currentTimeMillis() - isLimited >= (1 * 60 * 1000)) {
                                        toastMessenger(ctx, "Bạn đã sử dụng app quá giới hạn");
                                        isLimited = System.currentTimeMillis();
                                    }
                                    break;
                                case 3:
                                    killApp();
                                    break;
                            }
                        }

                    } else {
                        if (appIsRunning.getCountDown() > 0) {
                            if (!isOpen) {
                                int time = (int) System.currentTimeMillis();
                                appIsRunning.setTimeIsRun(time);
                                databaseLimited.updateToLimitedDatabase(appIsRunning);
                                isOpen = true;
                                int timeA = appIsRunning.getCountDown();
                                timeA /= 1000;
                                timeA /= 60;
                                int h = timeA / 60;
                                int p = timeA % 60;
                                timeOnRun = System.currentTimeMillis();
                                if (h == 0) {
                                    toastMessenger(ctx, "Bạn còn " + p + " phút sử dụng ứng dụng");

                                } else
                                    toastMessenger(ctx, "Bạn còn " + h + "giờ " + p + " phút sử dụng ứng dụng");
                            } else {
                                long timeMinus = timeNow - timeOnRun;
                                int time = appIsRunning.getCountDown() - (int) timeMinus;
                                if (time < 5 * 60 * 1000) {
                                    if (timeMinus % (1 * 60 * 1000) == 0) {
                                        Log.d(TAG, "handlingService: " + timeMinus);
                                        time /= 1000;
                                        time /= 60;
                                        int h = time / 60;
                                        int p = time % 60;
                                        appIsRunning.setCountDown(time);
                                        databaseLimited.updateToLimitedDatabase(appIsRunning);
                                        timeOnRun = System.currentTimeMillis();
                                        if (h == 0) {
                                            toastMessenger(ctx, "Bạn còn dưới 1 phút để sử dụng ứng dụng");

                                        } else {
                                            toastMessenger(ctx, "Bạn còn " + p + " phút sử dụng ứng dụng");
                                        }
                                    }
                                } else {
                                    if ((timeMinus) % (5 * 60 * 1000) == 0) {
                                        time /= 1000;
                                        time /= 60;
                                        int h = time / 60;
                                        int p = time % 60;
                                        appIsRunning.setCountDown(time);
                                        databaseLimited.updateToLimitedDatabase(appIsRunning);
                                        timeOnRun = System.currentTimeMillis();
                                        if (h == 0) {
                                            toastMessenger(ctx, "Bạn còn " + p + " phút sử dụng ứng dụng");

                                        } else
                                            toastMessenger(ctx, "Bạn còn " + h + "giờ" + p + " phút sử dụng ứng dụng");

                                    }
                                }
                            }
                        } else {
                            if (!isOpen) {
                                toastMessenger(ctx, "Hết thời gian sử dụng ứng dụng");
                                isOpen = true;
                            }
                            switch (appIsRunning.getLevel()) {
                                case 1:
                                    if (isLimited == 0) {
                                        showNotifyDialogLimited1();
                                        isLimited = System.currentTimeMillis();
                                    }

                                    if (System.currentTimeMillis() - isLimited >= (5 * 60 * 1000)) {
                                        showNotifyDialogLimited1();
                                        isLimited = System.currentTimeMillis();
                                    }
                                    break;
                                case 2:
                                    if (isLimited == 0) {
                                        showNotifyDialogLimited2(ctx);
                                        isLimited = System.currentTimeMillis();
                                    }

                                    if (System.currentTimeMillis() - isLimited >= (1 * 60 * 1000)) {
                                        toastMessenger(ctx, "Bạn đã sử dụng app quá giới hạn");
                                        isLimited = System.currentTimeMillis();
                                    }
                                    break;
                                case 3:
                                    killApp();
                                    break;
                            }
                        }
                    }
                }
            } else {
                isOpen = false;
            }
        }

    }


    private void updateListDatabase() {
        try {
            appLimiteds = databaseLimited.getListAppIsLimited();
            for (AppLimited limited : appLimiteds) {
                if (limited.isLimited() == 0) {
                    appLimiteds.remove(limited);
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
        if (timeNow - timeLastUpdated >= (2 * 60 * 1000)) {
            timeLastUpdated = System.currentTimeMillis();
            for (AppLimited limited : appLimiteds) {
                if ((int) timeNow > limited.getTimeEnd()) {
                    int timePlus = limited.getTimeEnd() - limited.getTimeStart();
                    limited.setTimeStart((int) timeNow);
                    limited.setTimeEnd((int) timeNow + timePlus);
                    limited.setCountDown(limited.getNumberLimited());
                    databaseLimited.updateToLimitedDatabase(limited);
                }
            }
        }

    }

    private void toastMessenger(final Context ctx, final String temp) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, temp, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void killApp() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        System.exit(1);
        startActivity(homeIntent);
    }


    private void showNotifyDialogLimited1() {
        final Handler handler = new Handler((Looper.getMainLooper()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                notifyLimitedView.setFocusableInTouchMode(true);
                notifyLimitedView.requestFocus();
                notifyLimitedView.setActivated(true);
                notifyLimitedView.setClickable(true);
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.window_manager_dialog_level_1, notifyLimitedView);
                setupNotifyDialogView1();

                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
//                            Log.d("notifyLimitView", "run: thread");
                            if (notifyLimitedView.isPressButtonBack || notifyLimitedView.isPressButtonHome) {
//                                killApp();
                                return;
                            }
                        }
                    }
                };
                thread.start();
                windowManager.addView(notifyLimitedView, params);
            }
        });

    }

    private void showNotifyDialogLimited2(Context context) {

//        Dialog dialog= new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.windows_manager_dialog_level_2);
//        dialog.show();
//        final Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                notifyLimitedView = new NotifyLimitedView(MyService.this);
//                LayoutInflater minflater = LayoutInflater.from(MyService.this);
//                minflater.inflate(R.layout.windows_manager_dialog_level_2, notifyLimitedView);
//                setupNotifyDialogView2();
//
//                params = new WindowManager.LayoutParams(
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.TYPE_PHONE,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                        PixelFormat.TRANSLUCENT);
//                params.width = WindowManager.LayoutParams.MATCH_PARENT;
//                params.height = WindowManager.LayoutParams.MATCH_PARENT;
////                FrameLayout frameLayout= new FrameLayout(getApplicationContext());
////                dis
////                params.gravity = Gravity.CENTER;
////                params.type = WindowManager.LayoutParams.TYPE_PHONE;
////                Activity activity
////                        notifyLimitedView.onKeyPreIme()
////                boolean keyDown = notifyLimitedView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
//////                boolean keyUp = notifyLimitedView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
////                if (keyDown){
////                    Toast.makeText(MyService.this, "Back", Toast.LENGTH_SHORT).show();
////                }
////                notifyLimitedView.setOnKeyListener(new View.OnKeyListener() {
////                    @Override
////                    public boolean onKey(View v, int keyCode, KeyEvent event) {
////                        Log.d(TAG, "onKey: hfd");
////                        if (keyCode == KeyEvent.KEYCODE_BACK) {
////                            Toast.makeText(MyService.this, "fasgasgsa", Toast.LENGTH_SHORT).show();
////                            Log.d(TAG, "onKey: ");
////                        }
////                        return false;
////                    }
////                });
//                windowManager.addView(notifyLimitedView, params);
//            }
//        });

        Intent intent = new Intent(MyService.this, demo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyService.this.startActivity(intent);
    }


    private void setupNotifyDialogView2() {
        String s = "";
        Random random = new Random();
        int length = 25;
        for (int i = 0; i < length; i++) {
            int a = 48 + random.nextInt((122 - 48));
            if (a >= 58 && a <= 64 || a >= 91 && a <= 96) {
                length++;
            } else {
                s += (char) a;
            }
        }

        int indext = random.nextInt(31);
        int backgroundId = databaseLimited.getBackgroundId(indext);
        Log.d(TAG, "backgroundId: " + backgroundId);
        int locutionIndex = random.nextInt(111);
        Locution locutionRandom = databaseLimited.getLocutionIndex(locutionIndex);

        final TextView txtSystemValue = notifyLimitedView.findViewById(R.id.txt_system_value);
        txtSystemValue.setText(s);
        TextView locutionValue = notifyLimitedView.findViewById(R.id.txt_locution_value_wdm2);
        locutionValue.setText(locutionRandom.getVieValue());
        TextView locutionAuthor = notifyLimitedView.findViewById(R.id.txt_author_locution_wdm2);
        locutionAuthor.setText(locutionRandom.getAuthor());

        notifyLimitedView.findViewById(R.id.layout_wd_manager_2).setBackgroundResource(backgroundId);

        final EditText edtPersonValue = notifyLimitedView.findViewById(R.id.edt_person_value);
        edtPersonValue.setText("");
        edtPersonValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(notifyLimitedView, params);
            }
        });

        notifyLimitedView.findViewById(R.id.btn_thoat_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
                killApp();
            }
        });

        notifyLimitedView.findViewById(R.id.btn_ok_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, txtSystemValue.getText().toString());
                Log.d(TAG, edtPersonValue.getText().toString());
                //todo
                if (txtSystemValue.getText().toString().equals(edtPersonValue.getText().toString())) {
                    windowManager.removeView(notifyLimitedView);
                } else {
                    Toast.makeText(MyService.this, "Mã xác nhận chưa đúng.", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        notifyLimitedView.on;
        setOnTouchListenet();
    }

    private void setupNotifyDialogView1() {
        Random random = new Random();
        int indext = random.nextInt(31);
        int backgroundId = databaseLimited.getBackgroundId(indext);
        int locutionIndex = random.nextInt(111);
        Locution locutionRandom = databaseLimited.getLocutionIndex(locutionIndex);

        TextView locutionValue = notifyLimitedView.findViewById(R.id.txt_locution_value_wdm1);
        locutionValue.setText(locutionRandom.getVieValue());
        TextView locutionAuthor = notifyLimitedView.findViewById(R.id.txt_author_locution_wdm1);
        locutionAuthor.setText(locutionRandom.getAuthor());

        Log.d(TAG, "setupNotifyDialogView1: " + indext + "_" + backgroundId);
        notifyLimitedView.findViewById(R.id.layout_wd_manager_1).setBackgroundResource(backgroundId);
        notifyLimitedView.findViewById(R.id.btn_ok_thong_bao_gioi_han_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
            }
        });

        notifyLimitedView.findViewById(R.id.btn_thoat_thong_bao_gioi_han_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
                killApp();
            }
        });

//        Broadcast broadcast= new Broadcast();
    }


    private void setOnTouchListenet() {
        Log.d(TAG, "vao setOnTouchListenet: ");
        notifyLimitedView.setOnTouchListener(new View.OnTouchListener() {

            private float xLayout;
            private float yLayout;
            private float xTouch;
            private float yTouch;


            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.BUTTON_BACK:
                    case MotionEvent.ACTION_BUTTON_PRESS:
                        Log.d(TAG, "onTouch: click back");
                        killApp();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        xLayout = notifyLimitedView.getX();
                        yLayout = notifyLimitedView.getY();
                        xTouch = motionEvent.getRawX();
                        yTouch = motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float xDeltal = motionEvent.getRawX() - xTouch;
                        float yDeltal = motionEvent.getRawY() - yTouch;
                        params.x = (int) (xLayout + xDeltal);
                        params.y = (int) (yLayout + yDeltal);
                        windowManager.updateViewLayout(notifyLimitedView, params);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    private void setOnClick() {
        View view = new LinearLayout(getApplicationContext());
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                }
                return false;
            }
        });

        KeyEvent.Callback callback = new KeyEvent.Callback() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == event.getKeyCode()) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean onKeyLongPress(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
                return false;
            }
        };
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
//        callback.onKeyDown(,event);
//        notifyLimitedView.on
    }

//    @Override
//    public void onAttachedToWindow()
//    { super.onAttachedToWindow();
//    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD); }


//    LinearLayout linearLayout= new LinearLayout(getApplicationContext()) {
//        @Override
//        public boolean dispatchKeyEvent(KeyEvent event) {
//            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
//                    || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
//                    || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
//                    || event.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
//                /// /The Code Want to Perform. } return super.dispatchKeyEvent(event); } };
//                /// mLinear.setFocusable(true);
//                /// View mView = inflate.inflate(R.layout.floating_panel_layout, mLinear);
//                /// WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//                /// params WindowManager.LayoutParams params = new WindowManager.LayoutParams( width, height, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, PixelFormat.TRANSLUCENT); params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL; wm.addView(mView, params);
//            }
//            return  true;
//        }
//    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch:  asg");
        if (event.getAction() == MotionEvent.BUTTON_BACK) {
            Log.d(TAG, "onTouch: gsdf");
        }
        return false;
    }


}

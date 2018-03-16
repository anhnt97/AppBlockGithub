package com.example.ngothanh.appblock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.view.NotifyLimitedView;
import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.sqlite.AppLimited;
import com.example.ngothanh.appblock.sqlite.DatabaseLimited;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

public class MyService extends Service {
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


    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private NotifyLimitedView notifyLimitedView;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        Toast.makeText(this, "Chạy service", Toast.LENGTH_SHORT).show();
        runCheckLimitApp();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        MyService getService() {

            return MyService.this;
        }
    }

    public void runCheckLimitApp() {
        final Context context = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        timeNow = System.currentTimeMillis();
                        updateListDatabase();
                        sleep(2000);
                        handlingService(context);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void handlingService(final Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);


        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        String temp = componentInfo.getPackageName();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        //nho gia tri cua package trong lan mo dau tien
        if (!isOpen) {
            rememberPackageName = temp;
        }
        if (!rememberPackageName.equals(temp)) {
            if (appIsRunning.isTypeIsCountOpen() == 0) {
                long countDownMinus = System.currentTimeMillis() - timeOnRun;
                appIsRunning.setCountDown(appIsRunning.getNumberLimited() - (int) countDownMinus);
                databaseLimited.updateToLimitedDatabase(appIsRunning);
                timeOnRun = 0;
            }
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
                        if (appIsRunning.getCountDown() >=0) {
                            if (!isOpen) {
                                appIsRunning.setCountDown(appIsRunning.getCountDown() - 1);
                                databaseLimited.updateToLimitedDatabase(appIsRunning);
                                isOpen = true;
                                if (appIsRunning.getCountDown() != 0) {
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
                                        showNotifyDialogLimited2();
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
                                        Log.d(TAG, "handlingService: "+timeMinus);
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

                                    if (System.currentTimeMillis() - isLimited >= (1 * 60 * 1000)) {
                                        showNotifyDialogLimited1();
                                        isLimited = System.currentTimeMillis();
                                    }
                                    break;
                                case 2:
                                    if (isLimited == 0) {
                                        showNotifyDialogLimited2();
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
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.window_manager_dialog_level_1, notifyLimitedView);
                setupNotifyDialogView1();

                params = new WindowManager.LayoutParams();
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.CENTER;
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
                windowManager.addView(notifyLimitedView, params);
            }
        });
    }

    private void showNotifyDialogLimited2() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                LayoutInflater minflater = LayoutInflater.from(MyService.this);
                minflater.inflate(R.layout.windows_manager_dialog_level_2, notifyLimitedView);
                setupNotifyDialogView2();

                params = new WindowManager.LayoutParams();
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.CENTER;
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
                windowManager.addView(notifyLimitedView, params);
            }
        });
    }

    private void setupNotifyDialogView2() {
        String s = "";
        final TextView txtSystemValue = notifyLimitedView.findViewById(R.id.txt_system_value);
        Random random = new Random();
        int length = 30;
        for (int i = 0; i < length; i++) {
            int a = 48 + random.nextInt((122 - 48));
            if (a >= 58 && a <= 64 || a >= 91 && a <= 96) {
                length++;
            } else {
                s += (char) a;
            }
        }
        txtSystemValue.setText(s);

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
        setOnTouchListenet();
    }

    private void setupNotifyDialogView1() {
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
//        setOnTouchListenet();
    }

    private void setOnTouchListenet() {
        notifyLimitedView.setOnTouchListener(new View.OnTouchListener() {
            private float xLayout;
            private float yLayout;
            private float xTouch;
            private float yTouch;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
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
}

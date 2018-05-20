package com.example.ngothanh.appblock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.sqlite.Database;
import com.example.ngothanh.appblock.sqlite.Locution;
import com.example.ngothanh.appblock.view.NotifyLimitedView;
import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.sqlite.AppLimited;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

public class MyService extends Service {
    private static final int LIMIT_LEVEL_1 = 1000 * 60 * 3;
    private static final int LIMIT_LEVEL_2 = 1000 * 6 * 5;
    private static final int LIMIT_LEVEL_3 = 0;
    private static final String TAG = "MyService";
    private static final String TAG2 = "MyService2";
    private final IBinder myBinder = new LocalBinder();
    private ArrayList<AppLimited> appLimiteds;
    private boolean isOpen = false;
    private Database database = new Database(this);
    private String rememberPackageName = "";
    private AppLimited appIsRunning;
    private long timeLastUpdated = 0;
    private long timeNow;
    private long isLimited = 0; //thoi gian kiem tra limit
    private long timeOnRun = 0; // thoi gian giua cac lan kiem tra
    private long timeStart = 0;
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
                long countDownMinus = System.currentTimeMillis() - timeStart;
                appIsRunning.setCountDown(appIsRunning.getNumberLimited() - (int) countDownMinus);
                database.updateToLimitedDatabase(appIsRunning);
                Log.d(TAG2, "chay update");
                timeOnRun = 0;
                timeStart = 0;
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
            //Khi package k doi
            //Chi thuc hien khi man hinh mo
            if (isScreenOn) {
                //Lay ra app co trong danh sach gioi han
                for (AppLimited limited : appLimiteds) {
                    if (limited.getPackageName().equals(temp)) {
                        appIsRunning = limited;
                        break;
                    }
                }
                if (appIsRunning != null) {
                    if (appIsRunning.isTypeIsCountOpen() == 1) {
                        //check con so lan gioi han va chua tung mo
                        if (appIsRunning.getCountDown() > 0) {
                            if (!isOpen || countOpen) {
                                appIsRunning.setCountDown(appIsRunning.getCountDown() - 1);
                                database.updateToLimitedDatabase(appIsRunning);
                                isOpen = true;
                                if (appIsRunning.getCountDown() >= 2) {
                                    toastMessenger(ctx, "Bạn còn " + (appIsRunning.getCountDown() - 1) + " lần mở ứng dụng");
                                } else {
                                    toastMessenger(ctx, "Đây là lần sử dụng ứng dụng cuối cùng");
                                }
                            }
                        } else {
                            if (!isOpen) {
                                toastMessenger(ctx, "Hết lượt mở ứng dụng");
                                isOpen = true;
                            }
                            int milestones = 0;
                            switch (appIsRunning.getLevel()) {
                                case 1:
                                    milestones = LIMIT_LEVEL_1;
                                    break;
                                case 2:
                                    milestones = LIMIT_LEVEL_2;
                                    break;
                                case 3:
                                    milestones = LIMIT_LEVEL_3;
                                    break;
                                default:
                                    break;
                            }
                            if ((System.currentTimeMillis() - isLimited) >= milestones) {
                                switch (appIsRunning.getLevel()) {
                                    case 1:
                                        showNotifyDialogLimited1(ctx);
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    case 2:
                                        showNotifyDialogLimited2(ctx);
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    case 3:
                                        killApp();
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                    } else {
                        if (appIsRunning.getCountDown() > 0) {
                            if (!isOpen) {
                                int time = (int) System.currentTimeMillis();
                                appIsRunning.setTimeIsRun(time);
                                database.updateToLimitedDatabase(appIsRunning);
                                isOpen = true;
                                int timeA = appIsRunning.getCountDown();
                                timeA /= 1000;
                                timeA /= 60;
                                int h = timeA / 60;
                                int p = timeA % 60;
                                Log.d(TAG2, "Lan dau mo: " + h + "_" + p);
                                timeOnRun = System.currentTimeMillis();
                                timeStart = System.currentTimeMillis();
                                if (h == 0) {
                                    toastMessenger(ctx, "Bạn còn " + p + " phút sử dụng ứng dụng");

                                } else
                                    toastMessenger(ctx, "Bạn còn " + h + "giờ " + p + " phút sử dụng ứng dụng");
                            } else {
                                long timeMinus = timeNow - timeOnRun;
                                long time = appIsRunning.getCountDown() - (int) timeMinus;
//                                Log.d(TAG2, "Thoi gian con lai: " + time);
                                if (time < 5 * 60 * 1000) {
                                    Log.d(TAG2, "Thoi gian con lai: " + time);
                                    if (timeMinus >= (1 * 60 * 1000)) {
                                        Log.d(TAG, "handlingService: " + timeMinus);
                                        time /= 1000;
                                        time /= 60;
                                        long h = time / 60;
                                        long p = time % 60;

                                        appIsRunning.setCountDown((int) time);
                                        database.updateToLimitedDatabase(appIsRunning);
                                        timeOnRun = System.currentTimeMillis();
                                        if (h == 0) {
                                            toastMessenger(ctx, "Bạn còn dưới 1 phút để sử dụng ứng dụng");

                                        } else {
                                            toastMessenger(ctx, "Bạn còn " + p + " phút sử dụng ứng dụng");
                                        }
                                    }
                                } else {
                                    if (timeMinus >= 3 * 60 * 1000) {
                                        time /= 1000;
                                        time /= 60;
                                        long h = time / 60;
                                        long p = time % 60;
                                        Log.d(TAG2, "time con lai: " + h + "_" + p);
                                        appIsRunning.setCountDown((int) time);
                                        database.updateToLimitedDatabase(appIsRunning);
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

                            int milestones = 0;
                            switch (appIsRunning.getLevel()) {
                                case 1:
                                    milestones = LIMIT_LEVEL_1;
                                    break;
                                case 2:
                                    milestones = LIMIT_LEVEL_2;
                                    break;
                                case 3:
                                    milestones = LIMIT_LEVEL_3;
                                    break;
                                default:
                                    break;
                            }
                            if ((System.currentTimeMillis() - isLimited) >= milestones) {
                                switch (appIsRunning.getLevel()) {
                                    case 1:
                                        showNotifyDialogLimited1(ctx);
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    case 2:
                                        showNotifyDialogLimited2(ctx);
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    case 3:
                                        killApp();
                                        isLimited = System.currentTimeMillis();
                                        break;
                                    default:
                                        break;
                                }
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
            appLimiteds = database.getListAppIsLimited();
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
                    database.updateToLimitedDatabase(limited);
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
        System.exit(0);
        startActivity(homeIntent);
    }


    private void showNotifyDialogLimited1(final Context ctx) {
        final Handler handler = new Handler((Looper.getMainLooper()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                notifyLimitedView.setFocusableInTouchMode(true);
                notifyLimitedView.setClickable(true);
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.window_manager_dialog_level_1, notifyLimitedView);
                setupNotifyDialogView1();

                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {

                            ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
                            String temp = "";
                            List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
                            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
                            temp = componentInfo.getPackageName();
                            if (!rememberPackageName.equals(temp)) {
                                killApp();
                            }

                            if (notifyLimitedView.isPressButtonBack || notifyLimitedView.isPressButtonHome) {
                                killApp();
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

    private void showNotifyDialogLimited2(final Context ctx) {
        final Handler handler = new Handler((Looper.getMainLooper()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                notifyLimitedView.setClickable(true);
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.windows_manager_dialog_level_2, notifyLimitedView);
                setupNotifyDialogView2();

                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {

                            ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
                            String temp = "";
                            List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
                            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
                            temp = componentInfo.getPackageName();
                            if (!rememberPackageName.equals(temp)) {
                                killApp();
                            }
                            if (notifyLimitedView.isPressButtonBack || notifyLimitedView.isPressButtonHome) {
                                killApp();
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

    private void setupNotifyDialogView1() {
        int backgroundId = database.getBackgroundId();
        Locution locutionRandom = database.getLocution();

        TextView locutionValue = notifyLimitedView.findViewById(R.id.txt_locution_value_wdm1);
        locutionValue.setText(locutionRandom.getVieValue());
        TextView locutionAuthor = notifyLimitedView.findViewById(R.id.txt_author_locution_wdm1);
        locutionAuthor.setText(locutionRandom.getAuthor());

        notifyLimitedView.findViewById(R.id.layout_wd_manager_1).setBackgroundResource(backgroundId);
        notifyLimitedView.findViewById(R.id.btn_ok_thong_bao_gioi_han_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
//                System.exit(1);
            }
        });

        notifyLimitedView.findViewById(R.id.btn_thoat_thong_bao_gioi_han_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
                killApp();
            }
        });
    }

    private void setupNotifyDialogView2() {
        int backgroundId = database.getBackgroundId();
        Locution locution = database.getLocution();

        final TextView txtRandomSystem = notifyLimitedView.findViewById(R.id.txt_system_value);
        String s = "";
        Random random = new Random();
        int length = 15;
        for (int i = 0; i < length; i++) {
            int a = 48 + random.nextInt((122 - 48));
            if (a >= 58 && a <= 64 || a >= 91 && a <= 96) {
                length++;
            } else {
                s += (char) a;
            }
        }
        txtRandomSystem.setText(s);

        final EditText edtPersonValue = notifyLimitedView.findViewById(R.id.edt_person_value);
        edtPersonValue.setText("");

        notifyLimitedView.findViewById(R.id.layout_wd_manager_2).setBackgroundResource(backgroundId);
        TextView txtLocutionValue = notifyLimitedView.findViewById(R.id.txt_locution_value_wdm2);
        txtLocutionValue.setText(locution.getVieValue());
        TextView txtLocutionAuthor = notifyLimitedView.findViewById(R.id.txt_author_locution_wdm2);
        txtLocutionAuthor.setText(locution.getAuthor());
        notifyLimitedView.findViewById(R.id.btn_ok_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_ok_thong_bao_gioi_han_2) {
                    if (edtPersonValue.getText().equals(txtRandomSystem)) {
                        windowManager.removeView(notifyLimitedView);

//                        System.exit(1);
                    } else {
                        Toast.makeText(MyService.this, "Mã xác nhận chưa đúng!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        notifyLimitedView.findViewById(R.id.btn_thoat_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_thoat_thong_bao_gioi_han_2) {
                    killApp();
                }
            }
        });


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

}

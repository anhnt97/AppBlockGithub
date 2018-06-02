package com.example.ngothanh.appblock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.sqlite.AppLock;
import com.example.ngothanh.appblock.sqlite.Database;
import com.example.ngothanh.appblock.sqlite.Locution;
import com.example.ngothanh.appblock.view.NotifyLimitedView;
import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.sqlite.AppLimited;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MyService extends Service {
    private static final int LIMIT_LEVEL_1 = 1000 * 30;
    //    private static final int LIMIT_LEVEL_1 = 1000 * 60 * 3;
//    private static final int LIMIT_LEVEL_2 = 1000 * 60 * 5;
    private static final int LIMIT_LEVEL_2 = 1000 * 60;
    private static final int LIMIT_LEVEL_3 = 0;
    private static final String TAG = "MyService";
    private static final String TAG2 = "MyService2";
    private final IBinder myBinder = new LocalBinder();
    private ArrayList<AppLimited> appLimiteds;
    private boolean isOpen = false;
    private Database database = new Database(this);
    private String rememberPackageName = "";
    private AppLimited appIsRunning;
    private long timeLastUpdated = 0;   //thời gian lan update database cuoi
    private long timeNow;   // thời gian hiện tại
    private long timeLastShowToast = 0; // nhớ các lần show toast nhắc nhở.
    private long timeOnRun = 0; // thời gian ứng dụng được chạy (Sử dụng cho kiểu giới hạn thời gian)
    private String tempRememberPackageName[] = new String[2];
    private boolean showWmanager1 = false;
    private boolean showWmanager2 = false;
    private boolean stopUpdateDabase = false;

    private ArrayList<AppLock> appLocks= new ArrayList<>();
    private AppLock appLocked;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private NotifyLimitedView notifyLimitedView;
    private long timeKillApp = 0;


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
        Toast.makeText(this, "Bắt đầu kiểm tra giới hạn!", Toast.LENGTH_SHORT).show();
        getListAppIsLock();
        runListenService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private class LocalBinder extends Binder {
    }

    public void runListenService() {
        final Context context = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    timeNow = System.currentTimeMillis();
//                    if (timeNow - timeKillApp > 1000) {
                    handlingService(context);
//                    }
                }
            }
        };
        thread.start();
    }

    public void handlingService(final Context ctx) {
        //Lấy ra package đang chạy
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        String temp = "";
        assert manager != null;
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        temp = componentInfo.getPackageName();

        if (temp.equals("com.example.ngothanh.appblock")) {
            return;
        }

        updateListDatabase();
        Log.d("aaa", "package Name: " + temp);


        //nho gia tri cua package trong lan mo dau tien
//        if (!isOpen) {
//            rememberPackageName = temp;
////            tempRememberPackageName[0] = temp;
//        }
        checkLimitApp(ctx, temp);
        checkLockApp(ctx, temp);


    }

    private void checkLimitApp(Context ctx, String temp) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        boolean isBreak = false;
        boolean isChange = false;
        if (!tempRememberPackageName[0].equals(temp)) {
            if (temp.equals(tempRememberPackageName[1])) {
                Log.d("ccc", "Thay đoi" +
                        ": ");
                isBreak = true;
            } else {
                isBreak = false;
            }
            tempRememberPackageName[1] = tempRememberPackageName[0];
            tempRememberPackageName[0] = temp;
            isChange = true;
        }

        Log.d("ccc", "checkLimit: " + tempRememberPackageName[0] + "_" + tempRememberPackageName[1]);
        // khi app bị đóng
        if (isChange && isOpen) {
            if (appIsRunning.isTypeIsCountOpen() == 0) {
                long countDownMinus = System.currentTimeMillis() - timeOnRun;
                appIsRunning.setCountDown(appIsRunning.getCountDown() - (int) countDownMinus);
                stopUpdateDabase = true;
                database.updateToLimitedDatabase(appIsRunning);
                stopUpdateDabase = false;
//                Log.d(TAG2, "chay update_" + countDownMinus / 1000 + "_" + (appIsRunning.getCountDown() - (int) countDownMinus) / 1000);
                timeLastShowToast = 0;
                timeOnRun = 0;
            }

//            } else {
//                countOpen = false;
//            }
//            tempRememberPackageName[1] = tempRememberPackageName[1];
//            tempRememberPackageName[0] = temp;

            isOpen = false;
            appIsRunning = null;
//            rememberPackageName = temp;
        } else {
            //Khi app vẫn hoạt động
            //Chi thuc hien khi man hinh mo
            if (isScreenOn) {
                //Lay ra app co trong danh sach gioi han
                for (AppLimited limited : appLimiteds) {
                    if (limited.getPackageName().equals(temp)) {
                        appIsRunning = limited;
                        break;
                    }
                }

                //Lay ra thong tin app tu he thong
                Drawable iconApp = null;
                PackageManager packageManager = ctx.getPackageManager();
                List<ApplicationInfo> applicationInfos = packageManager
                        .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES);
                Iterator<ApplicationInfo> it = applicationInfos.iterator();
                while (it.hasNext()) {
                    ApplicationInfo appInfo = it.next();
                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        it.remove();
                    }
                }
                for (ApplicationInfo info : applicationInfos) {
                    if (info.packageName.equals(temp)) {
                        iconApp = info.loadIcon(packageManager);
                        break;
                    }
                }

                // Khi da co duoc thong tin gioi han app
                if (appIsRunning != null) {
                    // Hình thức giới hạn số lần mở
                    if (appIsRunning.isTypeIsCountOpen() == 1) {
                        //Còn lần mở ứng dụng và ứng dụng chưa được mở
                        if (appIsRunning.getCountDown() >= 0) {
                            if (!isOpen) {
                                isOpen = true;
                                if (isBreak) {
                                    return;
                                }
                                Log.d("ddd", "Bo kong tru");
                                appIsRunning.setCountDown(appIsRunning.getCountDown() - 1);
                                stopUpdateDabase = true;
                                database.updateToLimitedDatabase(appIsRunning);
                                stopUpdateDabase = false;
                                if (appIsRunning.getCountDown() >= 1) {
                                    Log.d("ga", "toas: " + appIsRunning.getCountDown());
                                    toastMessenger(ctx, "Bạn còn " + (appIsRunning.getCountDown()) + " lần mở ứng dụng");
                                } else {
                                    if (appIsRunning.getCountDown() == 0) {
                                        toastMessenger(ctx, "Đây là lần sử dụng ứng dụng cuối cùng");
                                    }
                                }
                            }
                        } else {
                            //Hết lần mở ứng dụng
                            if (!isOpen) {
                                isOpen = true;
                                if (isBreak) {
                                    return;
                                }
                                toastMessenger(ctx, "Hết lượt mở ứng dụng");
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
                            long timeLastShow = appIsRunning.getTimeLastShow();
                            Log.d("aaa", "timeLastShow: " + timeLastShow);
                            Log.d("aaa", "handlingService: " + milestones + "_" + (System.currentTimeMillis() - timeLastShow));

                            // Hai lần show cách nhau 1 khoảng thời gian nhất định
                            if ((System.currentTimeMillis() - timeLastShow) >= milestones) {
                                Log.d("aaa", "handlingService: Co the show");
                                switch (appIsRunning.getLevel()) {
                                    case 1:
                                        if (!showWmanager1) {
                                            showNotifyDialogLimited1(iconApp, ctx);
                                            appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                            stopUpdateDabase = true;
                                            database.updateToLimitedDatabase(appIsRunning);
                                            stopUpdateDabase = false;
                                        }
                                        break;
                                    case 2:
                                        if (!showWmanager2) {
                                            showNotifyDialogLimited2(iconApp, ctx);
                                            appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                            stopUpdateDabase = true;
                                            database.updateToLimitedDatabase(appIsRunning);
                                            stopUpdateDabase = false;
                                        }
                                        break;
                                    case 3:
                                        killApp();
                                        appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                        stopUpdateDabase = true;
                                        database.updateToLimitedDatabase(appIsRunning);
                                        stopUpdateDabase = false;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                    } else {
                        //Hình thức giới hạn thời gian

                        // Còn thời gian sử dụng và chưa được mở
                        if (appIsRunning.getCountDown() > 0) {
                            if (!isOpen) {
                                isOpen = true;
                                int countDown = appIsRunning.getCountDown();
                                countDown /= 1000; // giây
                                int tempM = countDown / 60;  // phút
                                int m = tempM % 60;
                                int h = tempM / 60; //giờ
                                Log.d(TAG2, "time run: " + countDown / 1000); //mili giây
                                timeLastShowToast = System.currentTimeMillis();
                                timeOnRun = System.currentTimeMillis();
                                if (m == 0) {
                                    if (h == 0) {
                                        toastMessenger(ctx, "Bạn còn dưới 1 phút sử dụng ứng dụng");
                                    }
                                } else {
                                    if (h == 0) {
                                        toastMessenger(ctx, "Bạn còn " + m + " phút sử dụng ứng dụng");
                                    } else {
                                        toastMessenger(ctx, "Bạn còn " + h + "giờ " + m + " phút sử dụng ứng dụng");
                                    }
                                }
                            } else {
                                //App còn thời gian sử dụng và vẫn đang được mở
                                long timeMinus = timeNow - timeLastShowToast;
                                int countDown = appIsRunning.getCountDown() - (int) timeMinus;
                                if (countDown > 5 * 60 * 1000) {
                                    if (timeMinus >= 3 * 60 * 1000) {
                                        appIsRunning.setCountDown(countDown);
                                        stopUpdateDabase = true;
                                        database.updateToLimitedDatabase(appIsRunning);
                                        stopUpdateDabase = false;
                                        timeLastShowToast = System.currentTimeMillis();

                                        countDown /= 1000; // giây
                                        int tempM = countDown / 60;  // phút
                                        int m = tempM % 60;
                                        int h = tempM / 60; //giờ
                                        if (h == 0) {
                                            toastMessenger(ctx, "Bạn còn " + m + " phút sử dụng ứng dụng");
                                        } else {
                                            toastMessenger(ctx, "Bạn còn " + h + "giờ " + m + " phút sử dụng ứng dụng");
                                        }
                                    }
                                } else if (countDown == 5 * 60 * 1000) {
                                    toastMessenger(ctx, "Bạn còn 5 phút sử dụng ứng dụng");
                                } else {
                                    if (timeMinus >= (1 * 60 * 1000)) {
                                        appIsRunning.setCountDown(countDown);
                                        stopUpdateDabase = true;
                                        database.updateToLimitedDatabase(appIsRunning);
                                        stopUpdateDabase = false;
                                        timeLastShowToast = System.currentTimeMillis();

                                        countDown /= 1000; // giây
                                        int tempM = countDown / 60;  // phút
                                        int m = tempM % 60;
                                        int s = countDown % 60;

                                        if (m == 0) {
                                            //phút =0. mà countDown>0. suy gia còn giây
                                            toastMessenger(ctx, "Bạn còn dưới 1 phút sử dụng ứng dụng");
                                        } else {
                                            if (s >= 30) {
                                                toastMessenger(ctx, "Bạn còn " + m + 1 + " phút sử dụng ứng dụng");
                                            } else {
                                                toastMessenger(ctx, "Bạn còn " + m + " phút sử dụng ứng dụng");
                                            }
                                        }
                                    }
                                }

                            }
                        } else {
                            //Mở khi hết thời gian
                            if (!isOpen) {
                                isOpen = true;
                                toastMessenger(ctx, "Hết thời gian sử dụng ứng dụng");
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
                            long timeLastShow = appIsRunning.getTimeLastShow();
                            //Hai lần hiển thị màn hình chặn cách nhau 1 khoảng thời gian nhất định
                            if ((System.currentTimeMillis() - timeLastShow) >= milestones) {
                                switch (appIsRunning.getLevel()) {
                                    case 1:
                                        if (!showWmanager1) {
                                            showNotifyDialogLimited1(iconApp, ctx);
                                            appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                            stopUpdateDabase = true;
                                            database.updateToLimitedDatabase(appIsRunning);
                                            stopUpdateDabase = false;
                                        }
                                        break;
                                    case 2:
                                        if (!showWmanager2) {
                                            showNotifyDialogLimited2(iconApp, ctx);
                                            appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                            stopUpdateDabase = true;
                                            database.updateToLimitedDatabase(appIsRunning);
                                            stopUpdateDabase = false;
                                        }
                                        break;
                                    case 3:
                                        killApp();
                                        appIsRunning.setTimeLastShow(System.currentTimeMillis());
                                        stopUpdateDabase = true;
                                        database.updateToLimitedDatabase(appIsRunning);
                                        stopUpdateDabase = false;
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

    private void checkLockApp(Context ctx, String temp) {
        if(!isOpen){
            for (AppLock appLock:appLocks){
                if (appLock.getPackageName().equals(temp)){
                    appLocked= appLock;
                    break;
                }
            }
            //Todo
            isOpen= true;
        }
    }
    private void getListAppIsLock(){
        appLocks= database.getListAppLock();
    }

    private void updateListDatabase() {
        Log.d(TAG, "updateListDatabase: ");
        if (!stopUpdateDabase) {
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
            //Cập nhật thời gian cho 1 chu trình kiểm tra mới
            if (timeNow - timeLastUpdated >= (2 * 60 * 1000)) {
                timeLastUpdated = System.currentTimeMillis();
                for (AppLimited limited : appLimiteds) {
                    if (timeNow > limited.getTimeEnd()) {
                        long timePlus = limited.getTimeEnd() - limited.getTimeStart();
                        limited.setTimeStart(timeNow);
                        limited.setTimeEnd(timeNow + timePlus);
                        limited.setCountDown(limited.getNumberLimited());
                        database.updateToLimitedDatabase(limited);
                    }
                }
            }
        } else {
            Log.d("ggh", "vao nhuwng k thucj hien: ");

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
        Log.d("ggh", "killApp:");
        timeLastShowToast = 0;

        appIsRunning.setTimeLastShow((long) 0);
        stopUpdateDabase = true;
        database.updateToLimitedDatabase(appIsRunning);
        stopUpdateDabase = false;
//        notifyLimitedView.fi
//        System.exit(0);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        notifyLimitedView.onFinishTemporaryDetach();

        Process.killProcess(Process.myPid());
//        startActivity(homeIntent);
    }


    private void showNotifyDialogLimited1(final Drawable iconApp, final Context ctx) {
        final Handler handler = new Handler((Looper.getMainLooper()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("aaa", "run: show");
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                notifyLimitedView.setFocusableInTouchMode(true);
                notifyLimitedView.setClickable(true);
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.window_manager_limit_app_level_1, notifyLimitedView);
                setupNotifyDialogView1(iconApp);

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
                            if (!rememberPackageName.equals(temp) && showWmanager1) {
                                showWmanager1 = false;
                                killApp();
                            }

                            if (notifyLimitedView.isPressButtonBack && showWmanager1) {
                                showWmanager1 = false;
                                killApp();
                                return;
                            }
                        }
                    }
                };
                thread.start();
                windowManager.addView(notifyLimitedView, params);
                showWmanager1 = true;
            }
        });

    }

    private void showNotifyDialogLimited2(final Drawable iconApp, final Context ctx) {
        final Handler handler = new Handler((Looper.getMainLooper()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyLimitedView = new NotifyLimitedView(MyService.this);
                notifyLimitedView.setClickable(true);
                LayoutInflater inflater = LayoutInflater.from(MyService.this);
                inflater.inflate(R.layout.window_manager_limit_app_level_2, notifyLimitedView);
                setupNotifyDialogView2(ctx, iconApp);

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
                            if (!rememberPackageName.equals(temp) && showWmanager2) {
                                killApp();
                            }
                            if (notifyLimitedView.isPressButtonBack && showWmanager2) {
                                killApp();
                                return;
                            }
                        }
                    }
                };
                thread.start();
                windowManager.addView(notifyLimitedView, params);
                showWmanager2 = true;
            }
        });
    }

    private void setupNotifyDialogView1(Drawable icon) {
        int backgroundId = database.getBackgroundId();
        Locution locutionRandom = database.getLocution();
        ImageView iconApp = notifyLimitedView.findViewById(R.id.img_icon_app_in_wdmanager_1);
        iconApp.setImageDrawable(icon);

        TextView locutionValue = notifyLimitedView.findViewById(R.id.txt_locution_value_wdm1);
        locutionValue.setText(locutionRandom.getVieValue());
        TextView locutionAuthor = notifyLimitedView.findViewById(R.id.txt_author_locution_wdm1);
        locutionAuthor.setText(locutionRandom.getAuthor());

        notifyLimitedView.findViewById(R.id.layout_wd_manager_1).setBackgroundResource(backgroundId);
        notifyLimitedView.findViewById(R.id.btn_ok_thong_bao_gioi_han_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(notifyLimitedView);
                showWmanager1 = false;
                showWmanager2 = false;
                appIsRunning.setTimeLastShow(System.currentTimeMillis());
                stopUpdateDabase = true;
                database.updateToLimitedDatabase(appIsRunning);
                stopUpdateDabase = false;
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

    private void setupNotifyDialogView2(Context ctx, Drawable icon) {
        int backgroundId = database.getBackgroundId();
        Locution locution = database.getLocution();

        ImageView iconApp = notifyLimitedView.findViewById(R.id.img_icon_app_in_wdmanager_2);
        iconApp.setImageDrawable(icon);

        final TextView txtRandomSystem = notifyLimitedView.findViewById(R.id.txt_system_value);
        Typeface type = Typeface.createFromAsset(ctx.getAssets(), "LBRITEDI.TTF");
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
        txtRandomSystem.setTypeface(type);

        final EditText edtPersonValue = notifyLimitedView.findViewById(R.id.edt_person_value);
        edtPersonValue.setText("");
        edtPersonValue.setTypeface(type);

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
                        appIsRunning.setTimeLastShow(System.currentTimeMillis());
                        stopUpdateDabase = true;
                        database.updateToLimitedDatabase(appIsRunning);
                        stopUpdateDabase = false;

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

}

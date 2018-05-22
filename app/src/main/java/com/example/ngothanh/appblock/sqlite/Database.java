package com.example.ngothanh.appblock.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.ngothanh.appblock.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ngoth on 3/6/2018.
 */

public class Database {
    private static final String DATABASE_PATH = "/data/data/com.example.ngothanh.appblock/";
    private static final String DATABASE_NAME = "DanhNgonSqlite.db";
    private static final String TAG = "Database";
    private String destPathData;
    private SQLiteDatabase dbValue;
    private static final String TABLE_LIMITED = "AppLimited";
    private static final String TABLE_LOCK = "AppLock";
    private static final String TABLE_LOCUTION = "Locution";
    private static final String COLUMN_LIMITED_PACKAGE_NAME = "packageName";
    private static final String COLUMN_LIMITED_TYPE_IS_COUNT_OPEN = "limitedIsCountOpen";
    private static final String COLUMN_LIMITED_COUNT_IS_OPEN = "countNumberIsOpen";
    private static final String COLUMN_LIMITED_TIME_IS_HOURL = "hourIsLimited";
    private static final String COLUMN_LIMITED_TIME_IS_MINUTE = "minuteIsLimited";
    private static final String COLUMN_LIMITED_OBJECT_IS_FINISH = "objectIsFinish";
    private static final String COLUMN_LIMITED_STATUS = "status";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_NUMBER_LIMITED = "numberLimited";
    private static final String COLUMN_COUNT_DOWN = "countDown";
    private static final String COLUMN_TIME_START = "timeStart";
    private static final String COLUMN_TIME_END = "timeEnd";
    private static final String COLUMN_TIME_LAST_SHOW = "timeLastShow";
    private static final String COLUMN_LOCUTION_VIEVALUE = "vieValue";
    private static final String COLUMN_LOCUTION_ENGVALUE = "engValue";
    private static final String COLUMN_LOCUTION_AUTHORVALUE = "author";
    private Context context;
    private int arrayBackgroundId[];
    private ArrayList<Locution> locutions;
    private Random random;

    public Database(Context context) {
        random = new Random();
        this.context = context;
        initalizeDataBacgroundId();
        initalizeDataBase();
    }

    private void initalizeDataBacgroundId() {
        arrayBackgroundId = new int[31];
        arrayBackgroundId[0] = R.drawable.background_wdmanager_1;
        arrayBackgroundId[1] = R.drawable.background_wdmanager_2;
        arrayBackgroundId[2] = R.drawable.background_wdmanager_3;
        arrayBackgroundId[3] = R.drawable.background_wdmanager_4;
        arrayBackgroundId[4] = R.drawable.background_wdmanager_5;
        arrayBackgroundId[5] = R.drawable.background_wdmanager_6;
        arrayBackgroundId[6] = R.drawable.background_wdmanager_7;
        arrayBackgroundId[7] = R.drawable.background_wdmanager_8;
        arrayBackgroundId[8] = R.drawable.background_wdmanager_9;
        arrayBackgroundId[9] = R.drawable.background_wdmanager_10;
        arrayBackgroundId[10] = R.drawable.background_wdmanager_11;
        arrayBackgroundId[11] = R.drawable.background_wdmanager_12;
        arrayBackgroundId[12] = R.drawable.background_wdmanager_13;
        arrayBackgroundId[13] = R.drawable.background_wdmanager_14;
        arrayBackgroundId[14] = R.drawable.background_wdmanager_15;
        arrayBackgroundId[15] = R.drawable.background_wdmanager_16;
        arrayBackgroundId[16] = R.drawable.background_wdmanager_17;
        arrayBackgroundId[17] = R.drawable.background_wdmanager_18;
        arrayBackgroundId[18] = R.drawable.background_wdmanager_19;
        arrayBackgroundId[19] = R.drawable.background_wdmanager_20;
        arrayBackgroundId[20] = R.drawable.background_wdmanager_21;
        arrayBackgroundId[21] = R.drawable.background_wdmanager_22;
        arrayBackgroundId[22] = R.drawable.background_wdmanager_23;
        arrayBackgroundId[23] = R.drawable.background_wdmanager_24;
        arrayBackgroundId[24] = R.drawable.background_wdmanager_25;
        arrayBackgroundId[25] = R.drawable.background_wdmanager_26;
        arrayBackgroundId[26] = R.drawable.background_wdmanager_27;
        arrayBackgroundId[27] = R.drawable.background_wdmanager_28;
        arrayBackgroundId[28] = R.drawable.background_wdmanager_30;
        arrayBackgroundId[29] = R.drawable.background_wdmanager_31;
        arrayBackgroundId[30] = R.drawable.background_wdmanager_31;
    }

    public int getBackgroundId() {
        int index = random.nextInt(30);
        Log.d("loi", "backgroundId: "+ index);
        return arrayBackgroundId[index];
    }

    private void initalizeDataBase() {
        copyFileDataBase(context);
        getListLocution();
    }

    private void openDb() {
        if (dbValue == null || !dbValue.isOpen()) {
            dbValue = SQLiteDatabase.openDatabase(destPathData, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    private void closeDb() {
        if (dbValue != null && dbValue.isOpen()) {
            dbValue.close();
        }
    }

    private void copyFileDataBase(Context context) {
        destPathData = DATABASE_PATH + DATABASE_NAME;
        File destFile = new File(destPathData);
        if (destFile.exists()) {
            return;
        }
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("DanhNgonSqlite.db");
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] b = new byte[1024];
            int length;
            while ((length = is.read(b)) != -1) {
                fos.write(b, 0, length);
            }
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<AppLimited> getListAppIsLimited() {
        ArrayList<AppLimited> appLimiteds = new ArrayList<>();
        openDb();
        String sql = "SELECT * FROM " + TABLE_LIMITED;
        Cursor cursor = dbValue.rawQuery(sql, null);
        if (cursor == null ) {
            return appLimiteds;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return appLimiteds;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String packageName = cursor.getString(cursor.getColumnIndex(COLUMN_LIMITED_PACKAGE_NAME));
            int typeIsCountOpen = cursor.getInt(cursor.getColumnIndex(COLUMN_LIMITED_TYPE_IS_COUNT_OPEN));
            int countIsOpen = cursor.getInt(cursor.getColumnIndex(COLUMN_LIMITED_COUNT_IS_OPEN));
            int time[] = new int[2];
            time[0] = cursor.getInt(cursor.getColumnIndex(COLUMN_LIMITED_TIME_IS_HOURL));
            time[1] = cursor.getInt((cursor.getColumnIndex(COLUMN_LIMITED_TIME_IS_MINUTE)));
            int status = cursor.getInt(cursor.getColumnIndex(COLUMN_LIMITED_STATUS));
            String objFinish = cursor.getString(cursor.getColumnIndex(COLUMN_LIMITED_OBJECT_IS_FINISH));
            int level = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
            int numberLimited = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER_LIMITED));
            int countDown = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT_DOWN));
            long timeStart = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_START));
            long timeEnd = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_END));
            long timeLastShow = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_LAST_SHOW));
            appLimiteds.add(new AppLimited(packageName, typeIsCountOpen, countIsOpen, time,
                    objFinish, status, level, numberLimited, countDown, timeStart, timeEnd, timeLastShow));
            cursor.moveToNext();
        }
        cursor.close();
        closeDb();
        return appLimiteds;
    }

    public long addToLimitedDatabase(AppLimited appLimited) {
        openDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LIMITED_PACKAGE_NAME, appLimited.getPackageName());
        values.put(COLUMN_LIMITED_TYPE_IS_COUNT_OPEN, appLimited.isTypeIsCountOpen());
        values.put(COLUMN_LIMITED_COUNT_IS_OPEN, appLimited.getCountNumeberIsOpen());
        int[] a = appLimited.getCountTime();
        values.put(COLUMN_LIMITED_TIME_IS_HOURL, a[0]);
        values.put(COLUMN_LIMITED_TIME_IS_MINUTE, a[1]);
        values.put(COLUMN_LIMITED_OBJECT_IS_FINISH, appLimited.getObjFinish());
        values.put(COLUMN_LIMITED_STATUS, appLimited.isLimited());
        values.put(COLUMN_LEVEL, appLimited.getLevel());
        values.put(COLUMN_NUMBER_LIMITED, appLimited.getNumberLimited());
        values.put(COLUMN_COUNT_DOWN, appLimited.getCountDown());
        values.put(COLUMN_TIME_START, appLimited.getTimeStart());
        values.put(COLUMN_TIME_END, appLimited.getTimeEnd());
        values.put(COLUMN_TIME_LAST_SHOW, appLimited.getTimeLastShow());
        long result = dbValue.insert(TABLE_LIMITED, null, values);
        if (result <= -1) {
            Toast.makeText(context, "Thêm giới hạn gặp lỗi", Toast.LENGTH_SHORT).show();
        }
        closeDb();
        return result;
    }

    public long updateToLimitedDatabase(AppLimited appLimited) {
        openDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LIMITED_PACKAGE_NAME, appLimited.getPackageName());
        values.put(COLUMN_LIMITED_TYPE_IS_COUNT_OPEN, appLimited.isTypeIsCountOpen());
        values.put(COLUMN_LIMITED_COUNT_IS_OPEN, appLimited.getCountNumeberIsOpen());
        int[] a = appLimited.getCountTime();
        values.put(COLUMN_LIMITED_TIME_IS_HOURL, a[0]);
        values.put(COLUMN_LIMITED_TIME_IS_MINUTE, a[1]);
        values.put(COLUMN_LIMITED_OBJECT_IS_FINISH, appLimited.getObjFinish());
        values.put(COLUMN_LIMITED_STATUS, appLimited.isLimited());
        values.put(COLUMN_LEVEL, appLimited.getLevel());
        values.put(COLUMN_NUMBER_LIMITED, appLimited.getNumberLimited());
        values.put(COLUMN_COUNT_DOWN, appLimited.getCountDown());
        values.put(COLUMN_TIME_START, appLimited.getTimeStart());
        values.put(COLUMN_TIME_END, appLimited.getTimeEnd());
        values.put(COLUMN_TIME_LAST_SHOW, appLimited.getTimeLastShow());
        long result = dbValue.update(TABLE_LIMITED, values, COLUMN_LIMITED_PACKAGE_NAME + " = ? ", new String[]{appLimited.getPackageName()});
        closeDb();
        return result;
    }

    public void deleteLimitedDatabase(String packagename) {
        openDb();
        dbValue.delete(TABLE_LIMITED, COLUMN_LIMITED_PACKAGE_NAME + " = ? ", new String[]{packagename});
        closeDb();
    }

    public ArrayList<AppLock> getListAppLock() {
        ArrayList<AppLock> appLocks = new ArrayList<>();
        openDb();
        String sql = "SELECT * FROM " + TABLE_LOCK;
        Cursor cursor = dbValue.rawQuery(sql, null);
        if (cursor == null) {
            return appLocks;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return appLocks;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String packageName = cursor.getString(cursor.getColumnIndex("vieValue"));
            appLocks.add(new AppLock(packageName));
            cursor.moveToNext();
        }
        closeDb();
        cursor.close();
        return appLocks;
    }

    public void deleteAppLock(String packagename) {
        openDb();
        dbValue.delete(TABLE_LOCK, COLUMN_LIMITED_PACKAGE_NAME + " = ? ", new String[]{packagename});
    }

    private void getListLocution() {
        locutions = new ArrayList<>();
        openDb();
        String sql = "SELECT * FROM " + TABLE_LOCUTION;
        Cursor cursor = dbValue.rawQuery(sql, null);

        if (cursor == null) {
            return;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String locutionVieValue = cursor.getString(cursor.getColumnIndex(COLUMN_LOCUTION_VIEVALUE));
            String locutionEngValue = cursor.getString(cursor.getColumnIndex(COLUMN_LOCUTION_ENGVALUE));
            String locutionAuthor = cursor.getString(cursor.getColumnIndex(COLUMN_LOCUTION_AUTHORVALUE));
            locutions.add(new Locution(locutionVieValue, locutionEngValue, locutionAuthor));
            cursor.moveToNext();
        }
        closeDb();
        cursor.close();
    }

    public Locution getLocution() {
        int index = random.nextInt(111);
        return locutions.get(index);
    }


}

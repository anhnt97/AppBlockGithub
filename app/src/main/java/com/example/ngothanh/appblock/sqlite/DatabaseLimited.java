package com.example.ngothanh.appblock.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ngoth on 3/6/2018.
 */

public class DatabaseLimited extends SQLiteOpenHelper {
    public static final long TIME_COMPENSATED = 86400000 * 16071;
    private static final String DATABASE_PATH = Environment.getDataDirectory() + "/data/com.example.ngothanh.appblock/database/";
    private static final String TAG = "DatabaseLimited";
    private static final String DATABASE_NAME = "myDataBaseInAppBlock";
    private static final String TABLE_LIMITED = "appIsLimited";
    private static final String COLUMN_LIMITED_PACKAGE_NAME = "packageName";
    private static final String COLUMN_LIMITED_TYPE_IS_COUNT_OPEN = "typeLimitedIsCountOpen";
    private static final String COLUMN_LIMITED_COUNT_IS_OPEN = "coutNumberIsOpen";
    private static final String COLUMN_LIMITED_TIME_IS_HOURL = "hourIsLimited";
    private static final String COLUMN_LIMITED_TIME_IS_MINUTE = "minuteIsLimited";
    private static final String COLUMN_LIMITED_OBJECT_IS_FINISH = "objectIsFinish";
    private static final String COLUMN_LIMITED_STATUS = "status";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_NUMBER_LIMITED = "numberLimited";
    private static final String COLUMN_COUNT_DOWN = "countDown";
    private static final String COLUMN_TIME_START = "timeStart";
    private static final String COLUMN_TIME_END = "timeEnd";
    private static final String COLUMN_TIME_IS_RUN = "timeIsRun";
    //    private SQLiteDatabase db;
    private String destPath;
    private Context context;


    public DatabaseLimited(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LIMIT_TABLE = "CREATE TABLE "
                + TABLE_LIMITED + "(" + COLUMN_LIMITED_PACKAGE_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_LIMITED_TYPE_IS_COUNT_OPEN + " INTEGER, " + COLUMN_LIMITED_COUNT_IS_OPEN
                + " INTEGER, " + COLUMN_LIMITED_TIME_IS_HOURL + " INTEGER, " + COLUMN_LIMITED_TIME_IS_MINUTE
                + " INTEGER, " + COLUMN_LIMITED_OBJECT_IS_FINISH + " TEXT, " + COLUMN_LIMITED_STATUS +
                " INTEGER, " + COLUMN_LEVEL + " INTEGER, " + COLUMN_NUMBER_LIMITED + " INTEGER, " + COLUMN_COUNT_DOWN + " INTEGER, "
                + COLUMN_TIME_START + " INTEGER, " + COLUMN_TIME_END + " INTEGER, " + COLUMN_TIME_IS_RUN + " INTEGER" + ")";
        db.execSQL(CREATE_LIMIT_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIMITED);
        onCreate(db);
    }

    public ArrayList<AppLimited> getListAppIsLimited() {
        ArrayList<AppLimited> appLimiteds = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LIMITED, null);
            if (cursor == null || cursor.getCount() == 0) {
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
                int numberLimited= cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER_LIMITED));
                int countDown = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT_DOWN));
                int timeStart = cursor.getInt(cursor.getColumnIndex(COLUMN_TIME_START));
                int timeEnd = cursor.getInt(cursor.getColumnIndex(COLUMN_TIME_END));
                int timeRun = cursor.getInt(cursor.getColumnIndex(COLUMN_TIME_IS_RUN));
                appLimiteds.add(new AppLimited(packageName, typeIsCountOpen, countIsOpen, time,
                        objFinish, status, level,numberLimited, countDown, timeStart, timeEnd, timeRun));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return appLimiteds;
    }

    public long addToLimitedDatabase(AppLimited appLimited) {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("MyService", "In database: " + appLimited.getCountDown());
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
        values.put(COLUMN_TIME_IS_RUN, appLimited.getTimeIsRun());
        long result = db.insert(TABLE_LIMITED, null, values);
        if (result > -1) {
            //Install ok
            Log.d(TAG, "oki install: ");
        }
        db.close();
        return result;
    }

    public long updateToLimitedDatabase(AppLimited appLimited) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        values.put(COLUMN_TIME_IS_RUN, appLimited.getTimeIsRun());
        long result = db.update(TABLE_LIMITED, values, COLUMN_LIMITED_PACKAGE_NAME + " = ? ", new String[]{appLimited.getPackageName()});
        db.close();
        return result;
    }

    public long deleteLimitedDatabase(String packagename) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_LIMITED, COLUMN_LIMITED_PACKAGE_NAME + " = ? ", new String[]{packagename});
        db.close();
        return result;
    }

}

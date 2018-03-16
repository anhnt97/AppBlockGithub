package com.example.ngothanh.appblock;

import android.app.Application;

/**
 * Created by ngoth on 3/6/2018.
 */

public class App extends Application {
    private static com.example.ngothanh.appblock.App istance;
    public static com.example.ngothanh.appblock.App getIstance(){
        return istance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

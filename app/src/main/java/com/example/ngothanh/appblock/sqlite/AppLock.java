package com.example.ngothanh.appblock.sqlite;

public class AppLock {
    String packageName;
    public AppLock(String packageName){
        this.packageName= packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}

package com.example.ngothanh.appblock.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.ngothanh.appblock.R;

public class Demo extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_manager_limit_app_level_1);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            finishAndRemoveTask();
//        } else {
//            finish();
//        }
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        Process.killProcess(Process.myPid());
//        System.exit(1);
    }

    private void listenChangerPackage() {

    }
}

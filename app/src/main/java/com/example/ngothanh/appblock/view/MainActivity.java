package com.example.ngothanh.appblock.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.frament.LimitFrament;
import com.example.ngothanh.appblock.frament.RunningFrament;
import com.example.ngothanh.appblock.frament.SecurityFrament;
import com.example.ngothanh.appblock.service.MyService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private LimitFrament limitFrament;
    private RunningFrament runningFrament;
    private SecurityFrament securityFrament;
    private TextView btnLimit;
    private TextView btnRunning;
    private TextView btnSecurity;
    private boolean isFist = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setContentView(R.layout.activity_main);
        limitFrament = new LimitFrament();
        runningFrament = new RunningFrament();
        securityFrament = new SecurityFrament();
        startService(new Intent(getBaseContext(), MyService.class));
        showLimitFrament();
        isFist = true;
    }


    private void initializeComponents() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                btnLimit = findViewById(R.id.btn_limit);
                btnLimit.setOnClickListener(MainActivity.this);
                btnRunning = findViewById(R.id.btn_running);
                btnRunning.setOnClickListener(MainActivity.this);
                btnSecurity = findViewById(R.id.btn_security);
                btnSecurity.setOnClickListener(MainActivity.this);
            }
        });

    }

    public void showLimitFrament() {
        limitFrament = new LimitFrament();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.list_apps_frament, limitFrament)
                .show(limitFrament)
                .hide(runningFrament)
                .hide(securityFrament)
                .commit();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                btnLimit.setBackgroundResource(R.drawable.bg_btn_chosse_select);
                btnRunning.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnSecurity.setBackgroundResource(R.drawable.bg_btn_not_chosse);
            }
        });
    }

    public void showRunningFrament() {
        runningFrament = new RunningFrament();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.list_apps_frament, runningFrament)
                .show(runningFrament)
                .hide(limitFrament)
                .hide(securityFrament)
                .commit();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                btnLimit.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnRunning.setBackgroundResource(R.drawable.bg_btn_chosse_select);
                btnSecurity.setBackgroundResource(R.drawable.bg_btn_not_chosse);
            }
        });
    }

    public void showSecurityFrament() {
        securityFrament = new SecurityFrament();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.list_apps_frament, securityFrament)
                .show(securityFrament)
                .hide(limitFrament)
                .hide(runningFrament)
                .commit();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                btnLimit.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnRunning.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnSecurity.setBackgroundResource(R.drawable.bg_btn_chosse_select);
            }
        });
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btn_limit:
                showLimitFrament();
                break;
            case R.id.btn_running:
                showRunningFrament();
                btnRunning.setBackgroundResource(R.drawable.bg_btn_chosse_select);
                btnLimit.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnSecurity.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                break;
            case R.id.btn_security:
                showSecurityFrament();
                btnSecurity.setBackgroundResource(R.drawable.bg_btn_chosse_select);
                btnLimit.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                btnRunning.setBackgroundResource(R.drawable.bg_btn_not_chosse);
                break;
            default:
                break;
        }

    }


}

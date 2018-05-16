package com.example.ngothanh.appblock.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.frament.LimitFrament;
import com.example.ngothanh.appblock.frament.RunningFrament;
import com.example.ngothanh.appblock.frament.SecurityFrament;
import com.example.ngothanh.appblock.service.MyService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_PERMISSION = 111;
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
//        requestPermissions();
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

    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            boolean isGrantedAll = true;
            for (int i = 0; i < permissions.length; i++) {
                if (!isGranted(permissions[i])) {
                    isGrantedAll = false;
                    break;
                }
            }

            if (isGrantedAll) {
                startMainActivity();
            } else {
                // Request permission runtime
                ActivityCompat.requestPermissions(this,
                        permissions, REQUEST_CODE_PERMISSION);
            }
        } else {
            startMainActivity();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isGranted(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (permissions.length == grantResults.length) {
                boolean isGrantedAll = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isGrantedAll = false;
                        break;
                    }
                }
                if (isGrantedAll) {
                    startMainActivity();
                } else {
                    Toast.makeText(this,
                            "Permissions is not accepted",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}

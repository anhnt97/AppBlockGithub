package com.example.ngothanh.appblock.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.service.MyService;
import com.example.ngothanh.appblock.sqlite.DatabaseLimited;
import com.example.ngothanh.appblock.sqlite.Locution;

import java.util.Random;

public class demo extends AppCompatActivity {
    private TextView txtSystemValue;
    private TextView locutionValue;
    private TextView locutionAuthor;
    LinearLayout linearLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.windows_manager_dialog_level_2);
//        linearLayout = findViewById(R.id.layout_wd_manager_2);
//        linearLayout.setBackgroundResource(backgroundId);
        initializeComponents();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        System.exit(1);
        finish();
        startActivity(homeIntent);
    }

    @SuppressLint("WrongViewCast")
    private void initializeComponents() {
        DatabaseLimited databaseLimited = new DatabaseLimited(this);
        String s = "";
        Random random = new Random();
        int length = 25;
        for (int i = 0; i < length; i++) {
            int a = 48 + random.nextInt((122 - 48));
            if (a >= 58 && a <= 64 || a >= 91 && a <= 96) {
                length++;
            } else {
                s += (char) a;
            }
        }

        int indext = random.nextInt(31);
        int backgroundId = databaseLimited.getBackgroundId(indext);
        Log.d("aaa", "backgroundId: " + backgroundId);
        int locutionIndex = random.nextInt(110);
        Locution locutionRandom = databaseLimited.getLocutionIndex(locutionIndex);
        RelativeLayout relativeLayout= findViewById(R.id.layout_wd_manager_2);
        relativeLayout.setBackgroundResource(backgroundId);
//        findViewById(R.id.layout_wd_manager_2).setBackgroundResource(backgroundId);

        Log.d("abc", "initializeComponents: " + s);
        txtSystemValue = findViewById(R.id.txt_system_value);
        txtSystemValue.setText(s);
        locutionValue = findViewById(R.id.txt_locution_value_wdm2);
        locutionValue.setText(locutionRandom.getVieValue());
        locutionAuthor = findViewById(R.id.txt_author_locution_wdm2);
        locutionAuthor.setText(locutionRandom.getAuthor());


        final EditText edtPersonValue = findViewById(R.id.edt_person_value);
        edtPersonValue.setText("");
        findViewById(R.id.btn_ok_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
                if (txtSystemValue.getText().toString().equals(edtPersonValue.getText().toString())) {
                    finish();
                    System.exit(1);
                } else {
                    Toast.makeText(demo.this, "Mã xác nhận chưa đúng.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btn_thoat_thong_bao_gioi_han_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killApp();
            }
        });
//        setOnTouchListenet();

//        notifyLimitedView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                return false;
//            }
//        });
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
}

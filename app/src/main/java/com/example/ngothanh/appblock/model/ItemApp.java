package com.example.ngothanh.appblock.model;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ngoth on 2/16/2018.
 */

public class ItemApp {
    private String packageName;
    private ImageView imgIconApp;
    private String txtAppName;
    private ImageView imgStatus;

    public ItemApp(ImageView imgIconApp, String txtAppName, ImageView imgStatus, String packageName) {
        this.imgIconApp = imgIconApp;
        this.txtAppName = txtAppName;
        this.imgStatus = imgStatus;
        this.packageName= packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ImageView getImgIconApp() {
        return imgIconApp;
    }

    public void setImgIconApp(ImageView imgIconApp) {
        this.imgIconApp = imgIconApp;
    }

    public String getTxtAppName() {
        return txtAppName;
    }

    public void setTxtAppName(String txtAppName) {
        this.txtAppName = txtAppName;
    }

    public ImageView getImgStatus() {
        return imgStatus;
    }

    public void setImgStatus(ImageView imgStatus) {
        this.imgStatus = imgStatus;
    }
}

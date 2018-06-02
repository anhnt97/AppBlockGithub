package com.example.ngothanh.appblock.sqlite;

/**
 * Created by ngoth on 3/6/2018.
 */

public class AppLimited {
    String packageName;
    int typeIsCountOpen;    // Cờ kiểm tra Kiểu gới hạn là số lần mở (=1) hoặc thời gian sử dụng (=0)
    int countNumeberIsOpen; // Số lần mở được thiết lập
    int countTime[] = new int[2];   // giờ và phút thiết lập giới hạn
    String objFinish;   // Chu kỳ giới hạn
    int isLimited;  // trạng thái đang chạy hay dừng
    int level;  // Cấp độ giới hạn
    int numberLimited;// Nhớ số cài đặt giới hạn được thiết lập. nhằm cho mục đích update
    int countDown;  // Đếm số lượng giới hạn còn lại
    long timeStart; //Thời gia bắt đầu chu kỳ giới hạn
    long timeEnd;   //Thời gian kết thúc chu kỳ giới hạn
    long timeLastShow;  // Thời gian hiện windowManager nhắc nhở lần cuối

    public AppLimited(String packageName, int typeIsCountOpen, int countNumeberIsOpen,
                      int[] countTime, String objFinish, int isLimited, int level, int numberLimited, int countDown,
                      long timeStart, long timeEnd, long timeLastShow) {
        this.packageName = packageName;
        this.typeIsCountOpen = typeIsCountOpen;
        this.countNumeberIsOpen = countNumeberIsOpen;
        this.countTime = countTime;
        this.objFinish = objFinish;
        this.isLimited = isLimited;
        this.level = level;
        this.countDown = countDown;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.numberLimited = numberLimited;
        this.timeLastShow = timeLastShow;
    }

    public int getNumberLimited() {
        return numberLimited;
    }

    public void setNumberLimited(int numberLimited) {
        this.numberLimited = numberLimited;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getCountDown() {
        return countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int isTypeIsCountOpen() {
        return typeIsCountOpen;
    }

    public void setTypeIsCountOpen(int typeIsCountOpen) {
        this.typeIsCountOpen = typeIsCountOpen;
    }

    public int getCountNumeberIsOpen() {
        return countNumeberIsOpen;
    }

    public void setCountNumeberIsOpen(int countNumeberIsOpen) {
        this.countNumeberIsOpen = countNumeberIsOpen;
    }

    public int[] getCountTime() {
        return countTime;
    }

    public void setCountTime(int[] countTime) {
        this.countTime = countTime;
    }

    public String getObjFinish() {
        return objFinish;
    }

    public void setObjFinish(String objFinish) {
        this.objFinish = objFinish;
    }

    public int isLimited() {
        return isLimited;
    }

    public void setLimited(int limited) {
        isLimited = limited;
    }

    public long getTimeLastShow() {
        return timeLastShow;
    }

    public void setTimeLastShow(long timeLastShow) {
        this.timeLastShow = timeLastShow;
    }
}

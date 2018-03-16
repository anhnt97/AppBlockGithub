package com.example.ngothanh.appblock.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.adapter.ItemAppAdapter;
import com.example.ngothanh.appblock.model.ItemApp;
import com.example.ngothanh.appblock.sqlite.AppLimited;
import com.example.ngothanh.appblock.sqlite.DatabaseLimited;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by ngoth on 3/2/2018.
 */

public class RunningFrament extends Fragment implements ItemAppAdapter.OnItemClickListener,
        ItemAppAdapter.OnItemLongClickListener {
    private View rootView;
    private RecyclerView rcvApps;
    private List<ItemApp> apps;
    private ItemAppAdapter iconAppAdapter;
    private ArrayList<AppLimited> appLimiteds;
    private List<ApplicationInfo> applicationInfos = new ArrayList<>();
    private PackageManager packageManager;
    int flagLastChossse = 0;

    int beforSoLanThietLap = 0;
    int[] beforSoTHoiGianThietLap = new int[2];
    String beforMocGioiHan = "";
    int beforIsChosseSoLan = 0;
    int beforFlagLevel = 0;


    final int[] afterSoLanThietLap = {0};
    final int[] afterSoThoiGianThietLap = new int[2];
    final String[] afterFlagChonMocGioiHan = {""};
    final int[] afterIsChosseSoLan = {0};
    int afterFlagLevel = 0;
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.context = container.getContext();
        rootView = inflater.inflate(R.layout.frament_list_app_in_running, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents();
        showListApps();

    }

    private void initializeComponents() {
        rcvApps = rootView.findViewById(R.id.rcv_apps_in_running);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvApps.setLayoutManager(linearLayoutManager);

        int orient = DividerItemDecoration.VERTICAL;
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), orient);
        rcvApps.addItemDecoration(decoration);
    }

    private void showListApps() {
        apps = new ArrayList<>();
        DatabaseLimited databaseLimited = new DatabaseLimited(context);

        appLimiteds = databaseLimited.getListAppIsLimited();
        if (appLimiteds.size() == 0) {

        } else {
            packageManager = getActivity().getPackageManager();
            applicationInfos = packageManager
                    .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES);
            Iterator<ApplicationInfo> it = applicationInfos.iterator();
            while (it.hasNext()) {
                ApplicationInfo appInfo = it.next();
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    it.remove();
                }
            }

            Log.d("abcdf", "showListApps: " + appLimiteds.size());
            for (AppLimited limited : appLimiteds) {
                String packageName = limited.getPackageName();
                for (ApplicationInfo application : applicationInfos) {
                    if (application.packageName.equals(packageName)) {
                        Log.d("tessttttt", "showListApps: ");
                        String appName = (String) application.loadLabel(packageManager);
                        Drawable icon = application.loadIcon(getActivity().getPackageManager());
                        ImageView view1 = new ImageView(getActivity());

                        view1.setImageDrawable(icon);
                        ImageView view2 = new ImageView(getActivity());

                        if (limited.isLimited() == 0) {
                            view2.setImageResource(R.drawable.icon_play);
                        } else {
                            view2.setImageResource(R.drawable.icon_pause);
                        }
                        ItemApp app = new ItemApp(view1, appName, view2, packageName);
                        apps.add(app);

                    }
                }
            }
        }

        sapXepList(apps);
        ArrayList<AppLimited> limiteds = new ArrayList<>();
        for (ItemApp itemApp : apps) {
            for (AppLimited appLimited : appLimiteds) {
                if (itemApp.getPackageName().equals(appLimited.getPackageName())) {
                    limiteds.add(appLimited);
                    break;
                }
            }
        }
        appLimiteds.clear();
        appLimiteds = limiteds;
        iconAppAdapter = new ItemAppAdapter(getActivity(), apps);
        iconAppAdapter.setOnItemClickListener(this);
        iconAppAdapter.setOnItemLongClickListener(this);
        rcvApps.setAdapter(iconAppAdapter);
    }

    public void sapXepList(List<ItemApp> itemApps) {
        Collections.sort(itemApps, new Comparator<ItemApp>() {
            @Override
            public int compare(ItemApp o1, ItemApp o2) {
                return o1.getTxtAppName().compareTo(o2.getTxtAppName());
            }
        });
    }

    @Override
    public void onItemClicked(View itemView, int position, String packageName) {
        Log.d("MyService", "in runnung: " + appLimiteds.get(position).getCountDown());
        ImageView view = iconAppAdapter.getIconApp(position).getImgIconApp();
        BitmapDrawable drawableIcon = (BitmapDrawable) view.getDrawable();
        Bitmap bitmapIcon = drawableIcon.getBitmap();
        String appName = iconAppAdapter.getIconApp(position).getTxtAppName();

        AppLimited limited = appLimiteds.get(position);
        int type = limited.isTypeIsCountOpen();
        int level = limited.getLevel();
        String mocGioiHan = limited.getObjFinish();
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about_limited);

        TextView setAppName = dialog.findViewById(R.id.txt_app_name_about);
        ImageView iconApp = dialog.findViewById(R.id.img_icon_app_about);
        TextView setkieuGioiHan = dialog.findViewById(R.id.txt_set_kieu_gioi_han_about);
        TextView txtHinhThucGioiHan = dialog.findViewById(R.id.txt_hinh_thuc_gioi_han_about);
        TextView txtSetThoiLuongGioiHan = dialog.findViewById(R.id.txt_set_thoi_luong_gioi_han_about);
        TextView txtSetMocGioiHan = dialog.findViewById(R.id.txt_set_moc_theo_doi_gioi_han_about);
        TextView txtSetLevel = dialog.findViewById(R.id.txt_set_level_about);
        TextView txtStatus = dialog.findViewById(R.id.txt_set_status_about);

        setAppName.setText(appName);
        iconApp.setImageBitmap(bitmapIcon);
        if (type == 1) {
            setkieuGioiHan.setText("Số lần mở");
            txtHinhThucGioiHan.setText("Số lần cài đặt");
            txtSetThoiLuongGioiHan.setText(String.valueOf(limited.getCountNumeberIsOpen()));

        } else {
            setkieuGioiHan.setText("Thời gian sử dụng:");
            txtHinhThucGioiHan.setText("Thời gian cài đặt:");
            int[] a = limited.getCountTime();
            txtSetThoiLuongGioiHan.setText(a[0] + " giờ " + a[1] + " phút");
        }
        txtSetMocGioiHan.setText(1 + " " + mocGioiHan);
        String s = "";
        switch (level) {
            case 1:
                s = "Nhẹ";
                break;
            case 2:
                s = "Bình thường";
                break;
            case 3:
                s = "Cương quyết";
                break;
            default:
                break;
        }
        txtSetLevel.setText(s);
        if (limited.isLimited() == 1) {
            txtStatus.setText("Đang chạy");
        } else
            txtStatus.setText("Tạm dừng");

        dialog.show();

    }


    @Override
    public void onItemLongClick(View itemView, final int position, final String packageName) {
        loadSettingLimitApp(packageName);

        ImageView view = iconAppAdapter.getIconApp(position).getImgIconApp();
        BitmapDrawable drawableIcon = (BitmapDrawable) view.getDrawable();
        final Bitmap bitmapIcon = drawableIcon.getBitmap();

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_long_click_running);

        final TextView appName = dialog.findViewById(R.id.txt_app_name_in_long_click);
        appName.setText(iconAppAdapter.getIconApp(position).getTxtAppName());
        TextView btnSua = dialog.findViewById(R.id.btn_sua);
        TextView btnXoa = dialog.findViewById(R.id.btn_xoa);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_sua:
                        showDialogThietLapGioiHan1(position, bitmapIcon, packageName);
                        dialog.dismiss();
                        break;
                    case R.id.btn_xoa:
                        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Thông báo");
                        alertDialog.setMessage("Bạn có muốn hủy bỏ thiết lập giới hạn cho ứng dụng "
                                + appName.getText().toString());
                        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Todo delete csdl
                                DatabaseLimited databaseLimited = new DatabaseLimited(context);
                                databaseLimited.deleteLimitedDatabase(packageName);
//                                iconAppAdapter.notifyDataSetChanged();
                                iconAppAdapter.remoItem(position);
                                appLimiteds.remove(position);
                                applicationInfos.remove(position);
                                iconAppAdapter.notifyItemRemoved(position);
                                alertDialog.dismiss();
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                        dialog.dismiss();
                        break;
                    default:
                }
            }
        };
        btnSua.setOnClickListener(onClickListener);
        btnXoa.setOnClickListener(onClickListener);
        dialog.show();

    }

    private void loadSettingLimitApp(String packageName) {
        for (AppLimited limited : appLimiteds) {
            if (limited.getPackageName().equals(packageName)) {
                afterIsChosseSoLan[0] = limited.isTypeIsCountOpen();
                beforIsChosseSoLan = limited.isTypeIsCountOpen();
                afterSoLanThietLap[0] = limited.getCountNumeberIsOpen();
                beforSoLanThietLap = limited.getCountNumeberIsOpen();
                int[] a = limited.getCountTime();
                afterSoThoiGianThietLap[0] = a[0];
                beforSoTHoiGianThietLap[0] = a[0];
                afterSoThoiGianThietLap[1] = a[1];
                beforSoTHoiGianThietLap[1] = a[1];
                afterFlagChonMocGioiHan[0] = limited.getObjFinish();
                beforMocGioiHan = limited.getObjFinish();
                afterFlagLevel = limited.getLevel();
                beforFlagLevel = limited.getLevel();
                break;
            }
        }
    }

    @Override
    public void onImageStatusClicked(int position, String packageName) {
        final AppLimited limited = appLimiteds.get(position);
        DatabaseLimited databaseLimited = new DatabaseLimited(context);
        final ImageView view2 = new ImageView(context);
        if (limited.isLimited() == 1) {
            if (limited.getCountDown() <= 0) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.windows_manager_dialog_level_2);

                String s = "";
                Random random = new Random();
                int length = 40;
                for (int i = 0; i < length; i++) {
                    int a = 48 + random.nextInt((122 - 48));
                    if (a >= 58 && a <= 64 || a >= 91 && a <= 96) {
                        length++;
                    } else {
                        s += (char) a;
                    }
                }
                final TextView txtSystemValue = dialog.findViewById(R.id.txt_system_value);
                txtSystemValue.setText(s);
                final EditText edtPersionValue = dialog.findViewById(R.id.edt_person_value);
                final TextView btnBack = dialog.findViewById(R.id.btn_thoat_thong_bao_gioi_han_2);
                btnBack.setText("Trở về");
                TextView btnOk = dialog.findViewById(R.id.btn_ok_thong_bao_gioi_han_2);

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.btn_thoat_thong_bao_gioi_han_2:
                                dialog.dismiss();
                                break;
                            case R.id.btn_ok_thong_bao_gioi_han_2:
                                if (txtSystemValue.getText().toString()
                                        .equals(edtPersionValue.getText().toString())) {
                                    dialog.dismiss();
                                    limited.setLimited(0);
                                    view2.setImageResource(R.drawable.icon_pause);
                                } else {
                                    Toast.makeText(context, "Mã xác nhận không đúng",
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                };
                btnBack.setOnClickListener(onClickListener);
                btnOk.setOnClickListener(onClickListener);
                dialog.show();
            } else {
                Log.d("abcgg", "onImageStatusClicked: THay ddoi");
                limited.setLimited(0);
                view2.setImageResource(R.drawable.icon_play);
            }
        } else {
            limited.setLimited(1);
            view2.setImageResource(R.drawable.icon_pause);
            int timePlus = limited.getTimeEnd() - limited.getTimeStart();
            int timeStart = (int) System.currentTimeMillis();
            int timeEnd = timeStart + timePlus;
            int counDown = limited.getNumberLimited();
            limited.setTimeStart(timeStart);
            limited.setTimeEnd(timeEnd);
            limited.setCountDown(counDown);
        }
        iconAppAdapter.getIconApp(position).setImgStatus(view2);
        iconAppAdapter.notifyItemChanged(position);
        databaseLimited.updateToLimitedDatabase(limited);
    }

    private void showDialogThietLapGioiHan1(final int positionClick, final Bitmap bitmapIconApp, final String packageName) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thiet_lap_gioi_han_1);


        LinearLayout layoutFrame = dialog.findViewById(R.id.llout_frame);
        switch (afterFlagLevel) {
            case 1:
                layoutFrame.setBackgroundColor(Color.rgb(167, 255, 132));
                break;
            case 2:
                layoutFrame.setBackgroundColor(Color.rgb(255, 251, 135));
                break;
            case 3:
                layoutFrame.setBackgroundColor(Color.rgb(255, 100, 92));
                break;
            default:
                break;
        }


        ImageView imgIconApp = dialog.findViewById(R.id.img_icon_app_in_thiet_lap_1);
        final RadioButton btnChonSoLan = dialog.findViewById(R.id.btn_chon_so_lan_mo);
        ImageView btnTangSoLan = dialog.findViewById(R.id.btn_tang_so_lan);
        final EditText edtSoLanMo = dialog.findViewById(R.id.edt_so_lan_mo);
        ImageView btnGiamSoLan = dialog.findViewById(R.id.btn_giam_so_lan);
        final RadioButton btnChonGioSoLan = dialog.findViewById(R.id.btn_chon_gio_so_lan_mo);
        final RadioButton btnChonNgaySoLan = dialog.findViewById(R.id.btn_chon_ngay_so_lan_mo);
        final RadioButton btnChonTuanSoLan = dialog.findViewById(R.id.btn_chon_tuan_so_lan_mo);

        final RadioButton btnChonThoiGian = dialog.findViewById(R.id.btn_chon_thoi_gian_su_dung);
        ImageView btnTangGioThoiGian = dialog.findViewById(R.id.btn_tang_gio_thoi_gian);
        final EditText edtSoGioThoiGian = dialog.findViewById(R.id.edt_so_gio_thoi_gian);
        ImageView btnGiamGioThoiGian = dialog.findViewById(R.id.btn_giam_gio_thoi_gian);
        ImageView btnTangPhutThoiGian = dialog.findViewById(R.id.btn_tang_phut_thoi_gian);
        final EditText edtSoPhutThoiGian = dialog.findViewById(R.id.edt_so_phut_thoi_gian);
        ImageView btnGiamPhutThoiGian = dialog.findViewById(R.id.btn_giam_phut_thoi_gian);
        final RadioButton btnChonGioThoiGian = dialog.findViewById(R.id.btn_chon_gio_thoi_gian);
        final RadioButton btnChonNgayThoiGian = dialog.findViewById(R.id.btn_chon_ngay_thoi_gian);
        final RadioButton btnChonTuanThoiGian = dialog.findViewById(R.id.btn_chon_tuan_thoi_gian);

        final TextView btnTroVe = dialog.findViewById(R.id.btn_tro_ve_thiet_lap_1);
        TextView btnTiep = dialog.findViewById(R.id.btn_tiep_thiet_lap_1);

        imgIconApp.setImageBitmap(bitmapIconApp);


        if (afterIsChosseSoLan[0] == 1) {
            btnChonSoLan.setChecked(true);
            edtSoLanMo.setText(String.valueOf(afterSoLanThietLap[0]));
            edtSoLanMo.setTextSize(20);
            edtSoGioThoiGian.setText(String.valueOf(0));
            edtSoGioThoiGian.setTextSize(16);
            edtSoPhutThoiGian.setText(String.valueOf(0));
            edtSoPhutThoiGian.setTextSize(16);
        } else {
            btnChonThoiGian.setChecked(true);
            edtSoLanMo.setText(String.valueOf(0));
            edtSoLanMo.setTextSize(20);
            edtSoGioThoiGian.setText(String.valueOf(afterSoThoiGianThietLap[0]));
            edtSoGioThoiGian.setTextSize(16);
            edtSoPhutThoiGian.setText(String.valueOf(afterSoThoiGianThietLap[1]));
            edtSoPhutThoiGian.setTextSize(16);
        }

        if (afterFlagChonMocGioiHan[0].equals("giờ")) {
            if (afterIsChosseSoLan[0] == 1) {
                btnChonGioSoLan.setChecked(true);
                flagLastChossse = 1;
            } else {
                btnChonGioThoiGian.setChecked(true);
                flagLastChossse = 0;
            }

        } else if (afterFlagChonMocGioiHan[0].equals("ngày")) {
            if (afterIsChosseSoLan[0] == 1) {
                btnChonNgaySoLan.setChecked(true);
                flagLastChossse = 1;
            } else {
                btnChonNgayThoiGian.setChecked(true);
                flagLastChossse = 0;
            }

        } else if (afterFlagChonMocGioiHan[0].equals("tuần")) {
            if (afterIsChosseSoLan[0] == 1) {
                btnChonTuanSoLan.setChecked(true);
                flagLastChossse = 1;
            } else {
                btnChonTuanThoiGian.setChecked(true);
                flagLastChossse = 0;
            }
        }


        final boolean[] isSoLuong = {false};
        final boolean[] isThoiGian = {false};
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_chon_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        afterIsChosseSoLan[0] = 1;
                        break;
                    case R.id.btn_tang_so_lan:
                        int a = Integer.parseInt(edtSoLanMo.getText().toString());
                        a++;
                        if (a > 99) {
                            edtSoGioThoiGian.setTextSize(18);
                        }
                        edtSoLanMo.setText(String.valueOf(a));
                        break;
                    case R.id.btn_giam_so_lan:
                        int b = Integer.parseInt(edtSoLanMo.getText().toString());
                        if (b > 0) {
                            b--;
                        }
                        if (b > 99) {
                            edtSoGioThoiGian.setTextSize(18);
                        }
                        if (b <= 99) {
                            edtSoGioThoiGian.setTextSize(20);
                        }
                        edtSoLanMo.setText(String.valueOf(b));
                        break;
                    case R.id.btn_chon_gio_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        flagLastChossse = 1;
                        isSoLuong[0] = true;
                        afterIsChosseSoLan[0] = 1;
                        afterFlagChonMocGioiHan[0] = "giờ";
                        break;
                    case R.id.btn_chon_ngay_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        flagLastChossse = 1;
                        isSoLuong[0] = true;
                        afterIsChosseSoLan[0] = 1;
                        afterFlagChonMocGioiHan[0] = "ngày";
                        break;
                    case R.id.btn_chon_tuan_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        flagLastChossse = 1;
                        afterIsChosseSoLan[0] = 1;
                        isSoLuong[0] = true;
                        afterFlagChonMocGioiHan[0] = "tuần";
                        break;
                    case R.id.btn_chon_thoi_gian_su_dung:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        afterIsChosseSoLan[0] = 0;
                        break;
                    case R.id.btn_tang_gio_thoi_gian:
                        int c = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                        c++;
                        if (c > 99) {
                            edtSoGioThoiGian.setTextSize(13);
                        }
                        edtSoGioThoiGian.setText(String.valueOf(c));
                        break;
                    case R.id.btn_giam_gio_thoi_gian:
                        int d = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                        if (d > 0) {
                            d--;
                        }
                        if (d > 99) {
                            edtSoGioThoiGian.setTextSize(13);
                        }
                        if (d <= 99) {
                            edtSoGioThoiGian.setTextSize(16);
                        }
                        edtSoGioThoiGian.setText(String.valueOf(d));
                        break;
                    case R.id.btn_tang_phut_thoi_gian:
                        int e = Integer.parseInt(edtSoPhutThoiGian.getText().toString());
                        e++;
                        edtSoPhutThoiGian.setText(String.valueOf(e));
                        if (e == 60) {
                            int p = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                            p++;
                            e = 0;
                            edtSoGioThoiGian.setText(String.valueOf(p));
                            edtSoPhutThoiGian.setText(String.valueOf(e));
                        }

                        break;
                    case R.id.btn_giam_phut_thoi_gian:
                        int f = Integer.parseInt(edtSoPhutThoiGian.getText().toString());
                        if (f == 0) {
                            int p = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                            if (p > 0) {
                                p--;
                            }
                            f = 59;
                            edtSoGioThoiGian.setText(String.valueOf(p));
                            edtSoPhutThoiGian.setText(String.valueOf(f));
                        } else f--;
                        edtSoPhutThoiGian.setText(String.valueOf(f));
                        break;
                    case R.id.btn_chon_gio_thoi_gian:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonGioSoLan.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        flagLastChossse = 0;
                        isThoiGian[0] = true;
                        afterIsChosseSoLan[0] = 0;
                        afterFlagChonMocGioiHan[0] = "giờ";
                        break;
                    case R.id.btn_chon_ngay_thoi_gian:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonGioSoLan.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        flagLastChossse = 0;
                        isThoiGian[0] = true;
                        afterIsChosseSoLan[0] = 0;
                        afterFlagChonMocGioiHan[0] = "ngày";
                        break;
                    case R.id.btn_chon_tuan_thoi_gian:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonGioSoLan.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        flagLastChossse = 0;
                        isThoiGian[0] = true;
                        afterIsChosseSoLan[0] = 0;
                        afterFlagChonMocGioiHan[0] = "tuần";
                        break;
                    case R.id.btn_tro_ve_thiet_lap_1:
                        dialog.dismiss();
                        break;
                    case R.id.btn_tiep_thiet_lap_1:
                        if (afterIsChosseSoLan[0] == 1) {
                            afterSoLanThietLap[0] = Integer.parseInt(edtSoLanMo.getText().toString());
                            afterSoThoiGianThietLap[0] = 0;
                            afterSoThoiGianThietLap[1] = 0;
                            if (flagLastChossse == 1) {
                                isSoLuong[0] = true;
                            }
                            Log.d("RunningFrament", "onClick: Tiếp " + afterIsChosseSoLan[0] + "So lan" + afterSoLanThietLap[0]);
                            if (afterSoLanThietLap[0] == 0 || !isSoLuong[0]) {
                                showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                            } else {
                                //Todo
                                showDialogThietLapGioiHan2(positionClick, bitmapIconApp, packageName);
                                dialog.dismiss();
                            }
                        } else {
                            afterSoThoiGianThietLap[0] = Integer.parseInt(edtSoGioThoiGian
                                    .getText().toString());
                            afterSoThoiGianThietLap[1] = Integer.parseInt(edtSoPhutThoiGian
                                    .getText().toString());
                            Log.d("RunningFrament", afterSoThoiGianThietLap[0]
                                    + " " + afterSoThoiGianThietLap[1]);
                            if (afterSoThoiGianThietLap[0] == 0 && afterSoThoiGianThietLap[1] == 0) {
                                showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                            } else {
                                if (flagLastChossse == 0) {
                                    isThoiGian[0] = true;
                                }
                                if (!isThoiGian[0]) {
                                    showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                } else {
                                    afterSoLanThietLap[0] = 0;
                                    if (afterFlagChonMocGioiHan[0].equals("giờ")) {
                                        if (afterSoThoiGianThietLap[0] >= 1) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(positionClick, bitmapIconApp, packageName);
                                            dialog.dismiss();
                                        }
                                    }
                                    if (afterFlagChonMocGioiHan[0].equals("ngày")) {
                                        if (afterSoThoiGianThietLap[0] >= 24) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(positionClick, bitmapIconApp, packageName);
                                            dialog.dismiss();
                                        }
                                    }
                                    if (afterFlagChonMocGioiHan[0].equals("tuần")) {
                                        if ((afterSoThoiGianThietLap[0] >= 168)) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(positionClick, bitmapIconApp, packageName);
                                            dialog.dismiss();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        btnChonSoLan.setOnClickListener(onClickListener);
        btnTangSoLan.setOnClickListener(onClickListener);
        btnGiamSoLan.setOnClickListener(onClickListener);
        btnChonGioSoLan.setOnClickListener(onClickListener);
        btnChonNgaySoLan.setOnClickListener(onClickListener);
        btnChonTuanSoLan.setOnClickListener(onClickListener);

        btnChonThoiGian.setOnClickListener(onClickListener);
        btnTangGioThoiGian.setOnClickListener(onClickListener);
        btnGiamGioThoiGian.setOnClickListener(onClickListener);
        btnTangPhutThoiGian.setOnClickListener(onClickListener);
        btnGiamPhutThoiGian.setOnClickListener(onClickListener);
        btnChonGioThoiGian.setOnClickListener(onClickListener);
        btnChonNgayThoiGian.setOnClickListener(onClickListener);
        btnChonTuanThoiGian.setOnClickListener(onClickListener);

        btnTroVe.setOnClickListener(onClickListener);
        btnTiep.setOnClickListener(onClickListener);

        dialog.show();

    }


    private void showDialogLoi(String s) {
        final Dialog dialog1 = new Dialog(getActivity());
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.dialog_thong_bao_loi);

        TextView messageThongBao = dialog1.findViewById(R.id.txt_message_thong_bao);
        TextView btnOk = dialog1.findViewById(R.id.btn_ok_thong_bao);
        messageThongBao.setText(s);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_ok_thong_bao) {
                    dialog1.dismiss();
                }
            }
        });
        dialog1.show();
    }

    private void showDialogThietLapGioiHan2(final int positionClick, final Bitmap bitmapIconApp, final String packageName) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thiet_lap_gioi_han_2);

        final RadioButton btnChonLevelNhe = dialog.findViewById(R.id.btn_chon_level_nhe);
        final RadioButton btnChonLevelBinhThuong = dialog.findViewById(R.id.btn_chon_level_binh_thuong);
        final RadioButton btnChonLevelCuongQuyet = dialog.findViewById(R.id.btn_chon_level_cuong_quyet);
        TextView btnTroVe = dialog.findViewById(R.id.btn_tro_ve_thiet_lap_2);
        final TextView btnTao = dialog.findViewById(R.id.btn_tao_thiet_lap_2);
        ImageView imgIconApp = dialog.findViewById(R.id.img_icon_app_in_thiet_lap_2);


        if (beforIsChosseSoLan != afterIsChosseSoLan[0]
                || beforSoLanThietLap != afterSoLanThietLap[0]
                || beforSoTHoiGianThietLap[0] != afterSoThoiGianThietLap[0]
                || beforSoTHoiGianThietLap[1] != afterSoThoiGianThietLap[1]
                || !beforMocGioiHan.equals(afterFlagChonMocGioiHan[0])) {
            Log.d("RunningFrament", beforIsChosseSoLan + " " + afterIsChosseSoLan[0]
                    + " _ " + beforSoLanThietLap + " " + afterSoLanThietLap[0]
                    + " _ " + beforSoTHoiGianThietLap[0] + " " + afterSoThoiGianThietLap[0]
                    + " _ " + beforSoTHoiGianThietLap[1] + " " + afterSoThoiGianThietLap[1]
                    + " _ " + beforMocGioiHan + " " + (afterFlagChonMocGioiHan[0]));
            btnTao.setText("Cập nhật");
        } else
            btnTao.setText("Ok");
        imgIconApp.setImageBitmap(bitmapIconApp);
        switch (afterFlagLevel) {
            case 1:
                btnChonLevelNhe.setChecked(true);
                break;
            case 2:
                btnChonLevelBinhThuong.setChecked(true);
                break;
            case 3:
                btnChonLevelCuongQuyet.setChecked(true);
                break;
            default:
                break;
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_chon_level_nhe:
                        btnChonLevelCuongQuyet.setChecked(false);
                        btnChonLevelBinhThuong.setChecked(false);
                        afterFlagLevel = 1;
                        if (afterFlagLevel != beforFlagLevel) {
                            btnTao.setText("Cập nhật");
                        }
                        break;
                    case R.id.btn_chon_level_binh_thuong:
                        btnChonLevelCuongQuyet.setChecked(false);
                        btnChonLevelNhe.setChecked(false);
                        afterFlagLevel = 2;
                        if (afterFlagLevel != beforFlagLevel) {
                            btnTao.setText("Cập nhật");
                        }
                        break;
                    case R.id.btn_chon_level_cuong_quyet:
                        btnChonLevelNhe.setChecked(false);
                        btnChonLevelBinhThuong.setChecked(false);
                        afterFlagLevel = 3;
                        if (afterFlagLevel != beforFlagLevel) {
                            btnTao.setText("Cập nhật");
                        }
                        break;
                    case R.id.btn_tro_ve_thiet_lap_2:
                        dialog.dismiss();
//                        showDialogThietLapGioiHan1(positionClick, bitmapIconApp, packageName);
                        break;
                    case R.id.btn_tao_thiet_lap_2:
                        if (beforFlagLevel != afterFlagLevel
                                || beforIsChosseSoLan != afterIsChosseSoLan[0]
                                || beforSoLanThietLap != afterSoLanThietLap[0]
                                || beforSoTHoiGianThietLap[0] != afterSoThoiGianThietLap[0]
                                || beforSoTHoiGianThietLap[1] != afterSoThoiGianThietLap[1]
                                || !beforMocGioiHan.equals(afterFlagChonMocGioiHan[0])) {
                            //Todo cap nhat database

                            int countDown;

                            if (afterIsChosseSoLan[0] == 1) {
                                countDown = afterSoLanThietLap[0];
                            } else {
                                int a = afterSoThoiGianThietLap[0] * 60 * 60 + afterSoThoiGianThietLap[1] * 60;
                                countDown = a * 1000;
                            }

                            int numberLimited = countDown;
                            int timeStart = (int) System.currentTimeMillis();
                            int timeEnd = timeStart;
                            if (afterFlagChonMocGioiHan[0].equals("giờ")) {
                                timeEnd = timeStart + (60 * 60 * 1000);
                            }
                            if (afterFlagChonMocGioiHan[0].equals("ngày")) {
                                timeEnd = timeStart + (24 * 60 * 60 * 1000);
                            }
                            if (afterFlagChonMocGioiHan[0].equals("tuần")) {
                                timeEnd = timeStart + (7 * 24 * 60 * 60 * 1000);
                            }
                            int timeRun = 0;

                            AppLimited limited = new AppLimited(packageName,
                                    afterIsChosseSoLan[0],
                                    afterSoLanThietLap[0],
                                    afterSoThoiGianThietLap,
                                    afterFlagChonMocGioiHan[0],
                                    1,
                                    afterFlagLevel,
                                    numberLimited,
                                    countDown, timeStart, timeEnd, timeRun);
                            DatabaseLimited databaseLimited = new DatabaseLimited(context);
                            databaseLimited.updateToLimitedDatabase(limited);
                            apps.clear();
                            showListApps();
                            for (int i = 0; i < appLimiteds.size(); i++) {
                                appLimiteds.remove(i);
                            }
                            showListApps();
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        btnChonLevelNhe.setOnClickListener(onClickListener);
        btnChonLevelBinhThuong.setOnClickListener(onClickListener);
        btnChonLevelCuongQuyet.setOnClickListener(onClickListener);
        btnTroVe.setOnClickListener(onClickListener);
        btnTao.setOnClickListener(onClickListener);
        dialog.show();

    }

}

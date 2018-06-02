package com.example.ngothanh.appblock.frament;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.adapter.ItemAppAdapter;
import com.example.ngothanh.appblock.sqlite.AppLimited;
import com.example.ngothanh.appblock.model.ItemApp;
import com.example.ngothanh.appblock.sqlite.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ngoth on 2/16/2018.
 */

public class LimitFrament extends Fragment implements ItemAppAdapter.OnItemClickListener, ItemAppAdapter.OnItemLongClickListener {
    private static final String TAG = "LimitFrament";
    private PackageManager packageManager;
    private Context context;
    private View rootView;
    private RecyclerView rcvApps;
    private List<ItemApp> apps;
    private ItemAppAdapter iconAppAdapter;
    private int tempCount;


    private int flagSelect = 0;
    private int soLanThietLap;
    private int[] soThoiGianThietLap = new int[2];
    private int flagChonMocGioiHan = 2;
    private boolean isChosseSoLan = false;
    private boolean isChosseThoiGian = false;
    private int flagLevel = 0;
    private Database database;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.context = container.getContext();
        rootView = inflater.inflate(R.layout.frament_list_app_in_limit, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents();
        showListApps();
    }

    private void initializeComponents() {
        rcvApps = rootView.findViewById(R.id.rcv_apps_in_limit);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvApps.setLayoutManager(linearLayoutManager);

        int orient = DividerItemDecoration.VERTICAL;
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), orient);
        rcvApps.addItemDecoration(decoration);
    }

    private void showListApps() {
        apps = new ArrayList<>();

        packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES);
        Iterator<ApplicationInfo> it = applicationInfos.iterator();
        while (it.hasNext()) {
            ApplicationInfo appInfo = it.next();
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                it.remove();
            }
            if (appInfo.packageName.equals("com.example.ngothanh.appblock")) {
                it.remove();
            }
        }

        database = new Database(context);
        ArrayList<AppLimited> appLimiteds = database.getListAppIsLimited();


        for (AppLimited limited : appLimiteds) {
            for (ApplicationInfo info : applicationInfos) {
                if (limited.getPackageName().equals(info.packageName)) {
                    applicationInfos.remove(info);
                    break;
                }
            }
        }

        for (ApplicationInfo info : applicationInfos) {
            String appName = (String) info.loadLabel(packageManager);
            Drawable icon = info.loadIcon(context.getPackageManager());
            ImageView view1 = new ImageView(context);

            view1.setImageDrawable(icon);
            ImageView view2 = new ImageView(context);

            view2.setImageResource(R.drawable.icon_plus);

            String packageName = info.packageName;
            ItemApp app = new ItemApp(view1, appName, view2, packageName);
            apps.add(app);
        }

        sapXepList(apps);
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
        ImageView view = iconAppAdapter.getIconApp(position).getImgIconApp();
        Drawable drawable = view.getDrawable();

        showDialogLuaChon(position, drawable, packageName);
    }

    @Override
    public void onItemLongClick(View itemView, int position, String packageName) {

    }

    @Override
    public void onImageStatusClicked(int position, String packageName) {
        ImageView view = iconAppAdapter.getIconApp(position).getImgIconApp();
        Drawable drawable = view.getDrawable();
        showDialogLuaChon(position, drawable, packageName);
    }


    private void showDialogLuaChon(final int position, final Drawable drawable, final String packageName) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_lua_chon_gioi_han);


        TextView btnTroVeLuaChon = dialog.findViewById(R.id.btn_tro_ve_lua_chon);
        TextView btnTiepLuaChon = dialog.findViewById(R.id.btn_tiep_lua_chon);
        RadioButton rdoBtnGioiHan = dialog.findViewById(R.id.radioBtn_gioi_han);
        RadioButton rdobtnCaiNgien = dialog.findViewById(R.id.radioBtn_cai_nghien);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.radioBtn_gioi_han:
                        flagSelect = -1;
                        break;
                    case R.id.radioBtn_cai_nghien:
                        flagSelect = 1;
                        break;
                    case R.id.btn_tiep_lua_chon:
                        if (flagSelect == 1) {
                            dialog.dismiss();
                            showDialogLoi("Tính năng đang được phát triển, sẽ có trong phiên bản tiếp theo!");
                        } else if (flagSelect == -1) {
                            dialog.dismiss();
                            Log.d(TAG, "sau click");
                            showDialogThietLapGioiHan1(position, drawable, packageName);
                        }
                        break;
                    case R.id.btn_tro_ve_lua_chon:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        };
        rdobtnCaiNgien.setOnClickListener(onClickListener);
        rdoBtnGioiHan.setOnClickListener(onClickListener);
        btnTiepLuaChon.setOnClickListener(onClickListener);
        btnTroVeLuaChon.setOnClickListener(onClickListener);
        dialog.show();
    }

    private void showDialogThietLapGioiHan1(final int position, final Drawable drawable, final String packageName) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thiet_lap_gioi_han_1);

        Log.d(TAG, "vào dialog 1");
        ImageView imgIconApp = dialog.findViewById(R.id.img_icon_app_in_thiet_lap_1);
        final RadioButton btnChonSoLan = dialog.findViewById(R.id.btn_chon_so_lan_mo);
        ImageView btnTangSoLan = dialog.findViewById(R.id.btn_tang_so_lan);
        final EditText edtSoLanMo = dialog.findViewById(R.id.edt_so_lan_mo);
        ImageView btnGiamSoLan = dialog.findViewById(R.id.btn_giam_so_lan);
        final RadioButton btnChonGioSoLan = dialog.findViewById(R.id.btn_chon_gio_so_lan_mo);
        final RadioButton btnChonNgaySoLan = dialog.findViewById(R.id.btn_chon_ngay_so_lan_mo);
        final RadioButton btnChonTuanSoLan = dialog.findViewById(R.id.btn_chon_tuan_so_lan_mo);

        Log.d(TAG, "show so lan mo");
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

        TextView btnTroVe = dialog.findViewById(R.id.btn_tro_ve_thiet_lap_1);
        TextView btnTiep = dialog.findViewById(R.id.btn_tiep_thiet_lap_1);

//        Icon icon= new
        imgIconApp.setImageDrawable(drawable);
//        imgIconApp.setImageBitmap(bitmapIconApp);
        Log.d(TAG, "show hết dialog");
        edtSoLanMo.setText("0");
        edtSoLanMo.setTextSize(20);
        edtSoGioThoiGian.setText("0");
        edtSoGioThoiGian.setTextSize(16);
        edtSoPhutThoiGian.setText("0");
        edtSoPhutThoiGian.setTextSize(16);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_chon_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        isChosseSoLan = true;
                        isChosseThoiGian = false;
                        break;
                    case R.id.btn_chon_gio_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        btnChonGioSoLan.setChecked(true);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        isChosseSoLan = true;
                        isChosseThoiGian = false;
                        flagChonMocGioiHan = -1;
                        break;
                    case R.id.btn_chon_ngay_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        btnChonGioSoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(true);
                        isChosseSoLan = true;
                        isChosseThoiGian = false;
                        flagChonMocGioiHan = 0;
                        break;
                    case R.id.btn_chon_tuan_so_lan_mo:
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(true);
                        btnChonTuanSoLan.setChecked(true);
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        isChosseSoLan = true;
                        isChosseThoiGian = false;
                        flagChonMocGioiHan = 1;
                        break;
                    case R.id.btn_chon_thoi_gian_su_dung:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonSoLan.setChecked(false);
                        isChosseThoiGian = true;
                        isChosseSoLan = false;
                        break;
                    case R.id.btn_chon_gio_thoi_gian:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonSoLan.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        btnChonGioThoiGian.setChecked(true);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        isChosseThoiGian = true;
                        isChosseSoLan = false;
                        flagChonMocGioiHan = -1;
                        break;
                    case R.id.btn_chon_ngay_thoi_gian:
                        btnChonGioSoLan.setChecked(false);
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonGioThoiGian.setChecked(false);
                        btnChonTuanThoiGian.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        btnChonSoLan.setChecked(false);
                        btnChonNgayThoiGian.setChecked(true);
                        isChosseThoiGian = true;
                        isChosseSoLan = false;
                        flagChonMocGioiHan = 0;
                        break;
                    case R.id.btn_chon_tuan_thoi_gian:
                        btnChonNgaySoLan.setChecked(false);
                        btnChonTuanSoLan.setChecked(false);
                        btnChonGioSoLan.setChecked(false);
                        btnChonThoiGian.setChecked(true);
                        btnChonGioThoiGian.setChecked(false);
                        btnChonNgayThoiGian.setChecked(false);
                        btnChonSoLan.setChecked(false);
                        btnChonTuanThoiGian.setChecked(true);
                        isChosseThoiGian = true;
                        isChosseSoLan = false;
                        flagChonMocGioiHan = 1;
                        break;
                    case R.id.btn_tro_ve_thiet_lap_1:
                        dialog.dismiss();
//                        showDialogLuaChon(bitmapIconApp, packageName);
                        break;
                    case R.id.btn_tiep_thiet_lap_1:
                        if (isChosseSoLan) {
                            soLanThietLap = Integer.parseInt(edtSoLanMo.getText().toString());
                        }
                        if (isChosseThoiGian) {
                            soThoiGianThietLap[0] = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                            soThoiGianThietLap[1] = Integer.parseInt(edtSoPhutThoiGian.getText().toString());
                        }
                        if (!isChosseSoLan && !isChosseThoiGian) {
                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                        } else if (isChosseSoLan) {
                            if (soLanThietLap == 0) {
                                showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                            } else {
                                if (flagChonMocGioiHan == 2) {
                                    showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");

                                } else {

                                    showDialogThietLapGioiHan2(dialog, position, drawable, packageName);
                                    dialog.hide();
                                    //Todo
                                }
                            }
                        } else {
                            if (soThoiGianThietLap[0] == 0 && soThoiGianThietLap[1] == 0) {
                                showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                            } else {
                                switch (flagChonMocGioiHan) {
                                    case 2:
                                        showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        break;
                                    case -1:
                                        if (soThoiGianThietLap[0] >= 1) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(dialog, position, drawable, packageName);
                                            dialog.hide();
                                        }
                                        break;
                                    case 0:
                                        if (soThoiGianThietLap[0] >= 24) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(dialog, position, drawable, packageName);
                                            dialog.hide();
                                        }
                                        break;
                                    case 1:
                                        if ((soThoiGianThietLap[0] >= 168)) {
                                            showDialogLoi("Xin vui lòng kiểm tra lại thiết lập giới hạn!");
                                        } else {
                                            showDialogThietLapGioiHan2(dialog, position, drawable, packageName);
                                            dialog.hide();
                                        }
                                        break;
                                    default:
                                        break;
                                }

                            }
                        }

                        break;
                    default:
                        break;
                }
            }
        };
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                int tempCount=0;
                switch (v.getId()) {
                    case R.id.btn_tang_so_lan:
                        tempCount++;
                        if (tempCount % 11 == 0) {
                            int tempOpen = Integer.parseInt(edtSoLanMo.getText().toString());
                            if (tempOpen > 99) {
                                edtSoLanMo.setTextSize(18);
                            }
                            edtSoLanMo.setText(String.valueOf(tempOpen + 1));
                            tempCount = 0;
                        }
                        break;
                    case R.id.btn_giam_so_lan:
                        tempCount++;
                        if (tempCount % 11 == 0) {
                            int tempOpen = Integer.parseInt(edtSoLanMo.getText().toString());
                            if (tempOpen > 0) {
                                if (tempOpen <= 99) {
                                    edtSoLanMo.setTextSize(20);
                                }
                                edtSoLanMo.setText(String.valueOf(tempOpen - 1));
                            }
                            tempCount = 0;
                        }
                        break;
                    case R.id.btn_tang_gio_thoi_gian:
                        tempCount++;
                        if (tempCount % 11 == 0) {
                            int c = Integer.parseInt(edtSoGioThoiGian.getText().toString());
                            c++;
                            if (c > 99) {
                                edtSoGioThoiGian.setTextSize(13);
                            }
                            edtSoGioThoiGian.setText(String.valueOf(c));
                            tempCount = 0;
                        }
                        break;
                    case R.id.btn_giam_gio_thoi_gian:
                        tempCount++;
                        if (tempCount % 11 == 0) {
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
                            tempCount = 0;
                        }
                        break;
                    case R.id.btn_tang_phut_thoi_gian:
                        tempCount++;
                        if (tempCount % 11 == 0) {
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
                            tempCount = 0;
                        }
                        break;
                    case R.id.btn_giam_phut_thoi_gian:
                        tempCount++;
                        if (tempCount % 11 == 0) {
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
                            tempCount = 0;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        };

        btnChonSoLan.setOnClickListener(onClickListener);
        btnTangSoLan.setOnClickListener(onClickListener);
        btnTangSoLan.setOnTouchListener(onTouchListener);
        btnGiamSoLan.setOnClickListener(onClickListener);
        btnGiamSoLan.setOnTouchListener(onTouchListener);
        btnChonGioSoLan.setOnClickListener(onClickListener);
        btnChonNgaySoLan.setOnClickListener(onClickListener);
        btnChonTuanSoLan.setOnClickListener(onClickListener);

        btnChonThoiGian.setOnClickListener(onClickListener);
        btnTangGioThoiGian.setOnClickListener(onClickListener);
        btnTangGioThoiGian.setOnTouchListener(onTouchListener);
        btnGiamGioThoiGian.setOnClickListener(onClickListener);
        btnGiamGioThoiGian.setOnTouchListener(onTouchListener);
        btnTangPhutThoiGian.setOnClickListener(onClickListener);
        btnTangPhutThoiGian.setOnTouchListener(onTouchListener);
        btnGiamPhutThoiGian.setOnClickListener(onClickListener);
        btnGiamPhutThoiGian.setOnTouchListener(onTouchListener);
        btnChonGioThoiGian.setOnClickListener(onClickListener);
        btnChonNgayThoiGian.setOnClickListener(onClickListener);
        btnChonTuanThoiGian.setOnClickListener(onClickListener);

        btnTroVe.setOnClickListener(onClickListener);
        btnTiep.setOnClickListener(onClickListener);
        dialog.show();

    }

    private void showDialogThietLapGioiHan2(final Dialog dialog1, final int position, final Drawable drawable, final String packageName) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thiet_lap_gioi_han_2);

        final RadioButton btnChonLevelNhe = dialog.findViewById(R.id.btn_chon_level_nhe);
        final RadioButton btnChonLevelBinhThuong = dialog.findViewById(R.id.btn_chon_level_binh_thuong);
        final RadioButton btnChonLevelCuongQuyet = dialog.findViewById(R.id.btn_chon_level_cuong_quyet);
        TextView btnTroVe = dialog.findViewById(R.id.btn_tro_ve_thiet_lap_2);
        TextView btnTao = dialog.findViewById(R.id.btn_tao_thiet_lap_2);
        ImageView imgIconApp = dialog.findViewById(R.id.img_icon_app_in_thiet_lap_2);

        imgIconApp.setImageDrawable(drawable);
//        imgIconApp.setImageBitmap(bitmapIconApp);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_chon_level_nhe:
                        btnChonLevelCuongQuyet.setChecked(false);
                        btnChonLevelBinhThuong.setChecked(false);
                        flagLevel = 1;
                        break;
                    case R.id.btn_chon_level_binh_thuong:
                        btnChonLevelCuongQuyet.setChecked(false);
                        btnChonLevelNhe.setChecked(false);
                        flagLevel = 2;
                        break;
                    case R.id.btn_chon_level_cuong_quyet:
                        btnChonLevelNhe.setChecked(false);
                        btnChonLevelBinhThuong.setChecked(false);
                        flagLevel = 3;
                        break;
                    case R.id.btn_tro_ve_thiet_lap_2:
                        dialog.dismiss();
                        dialog1.show();
//                        showDialogThietLapGioiHan1(position, drawable, packageName);
                        break;
                    case R.id.btn_tao_thiet_lap_2:
                        if (flagLevel != 0) {
                            //Todo ghi dữ liệu vào database
//                            Database database = new Database(context);
                            int typeLimit;
                            int numberIsOpen = soLanThietLap;
                            int[] countTime = new int[2];
                            countTime = soThoiGianThietLap;
                            String objFinish = "";
                            int isLimit = 1;
                            int level = flagLevel;
                            int countDown;

                            if (isChosseSoLan) {
                                countDown = soLanThietLap;
                            } else {
                                int a = soThoiGianThietLap[0] * 60 * 60 + soThoiGianThietLap[1] * 60;
                                countDown = a * 1000;
                            }
                            int timeStart = (int) System.currentTimeMillis();
                            if (isChosseSoLan) {
                                typeLimit = 1;
                                countTime[0] = 0;
                                countTime[1] = 0;
                            } else {
                                typeLimit = 0;
                                numberIsOpen = 0;
                            }
                            int numberLimited = countDown;

                            long timeEnd = 0;
                            switch (flagChonMocGioiHan) {
                                case -1:
                                    objFinish = "giờ";
                                    timeEnd = timeStart + (60 * 60 * 1000);
                                    break;
                                case 0:
                                    timeEnd = timeStart + (24 * 60 * 60 * 1000);
                                    objFinish = "ngày";
                                    break;
                                case 1:
                                    timeEnd = timeStart + (7 * 24 * 60 * 60 * 1000);
                                    objFinish = "tuần";
                                    break;
                                default:
                                    break;
                            }
                            long timeLastShow = 0;

                            AppLimited appLimited = new AppLimited(packageName, typeLimit,
                                    numberIsOpen, countTime, objFinish, isLimit, level, numberLimited,
                                    countDown, timeStart, timeEnd, timeLastShow);
                            Log.d("MyService", "countdown add: " + appLimited.getCountDown());
                            long a = database.addToLimitedDatabase(appLimited);

                            apps.clear();
                            showListApps();

                            if (a == -1) {
                                showDialogLoi("Xảy ra lỗi trong quá trình thêm thiết lập giới hạn.\n" +
                                        "Xin vui lòng thử lại sau");
                            } else {
                                String appName = apps.get(position).getTxtAppName();
                                String s = "";
                                s = "Bạn đã tạo thành công giới hạn cho Ứng dụng: " + appName + "\n\nVới ";
                                if (isChosseSoLan) {
                                    int n = soLanThietLap;
                                    switch (flagChonMocGioiHan) {
                                        case -1:
                                            s += n + " lần mở trên 1 Giờ.";
                                            break;
                                        case 0:
                                            s += n + " lần mở trên 1 Ngày.";
                                            break;
                                        case 1:
                                            s += n + " lần mở trên 1 Tuần.";
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    int t1 = soThoiGianThietLap[0];
                                    int t2 = soThoiGianThietLap[1];
                                    switch (flagChonMocGioiHan) {
                                        case -1:
                                            s += t1 + " giờ " + t2 + " phút sử dụng trong 1 Giờ.";
                                            break;
                                        case 0:
                                            s += t1 + " giờ " + t2 + " phút sử dụng trong 1 Ngày.";
                                            break;
                                        case 1:
                                            s += t1 + " giờ " + t2 + " phút sử dụng trong 1 Tuần.";
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                s += "\n\nMức độ giới hạn: ";
                                switch (flagLevel) {
                                    case 1:
                                        s += "Nhẹ.";
                                        break;
                                    case 2:
                                        s += "Bình Thường";
                                        break;
                                    case 3:
                                        s += "Cương Quyết";
                                        break;
                                    default:
                                        break;
                                }

                                showDialogLoi(s);
                            }
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
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                dialog1.show();
            }
        });
        dialog.show();
    }

    private void showDialogLoi(String s) {
        final Dialog dialog1 = new Dialog(context);
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

}

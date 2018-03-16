package com.example.ngothanh.appblock.frament;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.adapter.ItemAppAdapter;
import com.example.ngothanh.appblock.model.ItemApp;
import com.example.ngothanh.appblock.sqlite.AppLimited;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ngoth on 3/2/2018.
 */

public class SecurityFrament extends Fragment implements ItemAppAdapter.OnItemClickListener, ItemAppAdapter.OnItemLongClickListener {
    private View rootView;
    private RecyclerView rcvApps;
    private List<ItemApp> apps;
    private ItemAppAdapter iconAppAdapter;
    private PackageManager packageManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frament_list_app_in_security, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents();
        showListApps();
        showDialogLoi("Tính năng đang được phát triển, sẽ có trong phiên bản tiếp theo!");
    }

    private void initializeComponents() {
        rcvApps = rootView.findViewById(R.id.rcv_apps_in_security);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvApps.setLayoutManager(linearLayoutManager);

        int orient = DividerItemDecoration.VERTICAL;
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), orient);
        rcvApps.addItemDecoration(decoration);
    }

    private void showListApps() {
        apps = new ArrayList<>();
        packageManager = getActivity().getPackageManager();
        List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        Iterator<ApplicationInfo> it = applicationInfos.iterator();
        while (it.hasNext()) {
            ApplicationInfo appInfo = it.next();
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                it.remove();
            }
        }

        for (ApplicationInfo info : applicationInfos) {
            String appName = (String) info.loadLabel(packageManager);
            Drawable icon = info.loadIcon(getActivity().getPackageManager());
            ImageView view1 = new ImageView(getActivity());

            view1.setImageDrawable(icon);
            ImageView view2 = new ImageView(getActivity());
            view2.setImageResource(R.drawable.icon_lock);

            String packageName = info.packageName;
            ItemApp app = new ItemApp(view1, appName, view2, packageName);
            apps.add(app);
        }

        sapXepList(apps);

        iconAppAdapter = new ItemAppAdapter(getActivity(), apps);
        iconAppAdapter.setOnItemClickListener(this);
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
        showDialogLoi("Tính năng đang được phát triển, sẽ có trong phiên bản tiếp theo!");
    }


    @Override
    public void onImageStatusClicked(int position, String packageName) {
        showDialogLoi("Tính năng đang được phát triển, sẽ có trong phiên bản tiếp theo!");
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


    @Override
    public void onItemLongClick(View itemView, int position, String packageName) {
        showDialogLoi("Tính năng đang được phát triển, sẽ có trong phiên bản tiếp theo!");
    }
}

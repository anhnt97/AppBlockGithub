package com.example.ngothanh.appblock.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.ngothanh.appblock.frament.LimitFrament;
import com.example.ngothanh.appblock.frament.RunningFrament;
import com.example.ngothanh.appblock.frament.SecurityFrament;

/**
 * Created by ngoth on 3/10/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new LimitFrament();
                break;
            case 1:
                fragment = new RunningFrament();
                break;
            case 2:
                fragment = new SecurityFrament();
                break;
            default:
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title="One";
                break;
            case 1:
                title="Two";
                break;
            case 2:
                title="Three";
                break;
            default:
                break;
        }
        return title;
    }
}

package com.vlcnavigation.ui.settings;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SettingsTabAdapter extends FragmentPagerAdapter {

    private final int numberOfTabs;
    private final Context context;

    public SettingsTabAdapter(Context ctx, FragmentManager manager, int numberOfTabs)
    {
        super(manager);
        this.context = ctx;
        this.numberOfTabs = numberOfTabs;

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FloorsLightsManagerFragment floorsLightsManagerFragment = new FloorsLightsManagerFragment();
                return floorsLightsManagerFragment;
            case 1:
                AddFloorFragment addFloorFragment = new AddFloorFragment();
                return addFloorFragment;
            case 2:
                AddLightFragment addLightFragment = new AddLightFragment();
                return addLightFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Manager";
            case 1:
                return "Add a new Floor";
            case 2:
                return "Add a new Light";
        }
        return null;
    }
}

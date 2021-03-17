package com.vlcnavigation.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.components.DotIndicatorDecoration;
import com.vlcnavigation.components.RecyclerViewMargin;
import com.vlcnavigation.module.svg2vector.SvgFetcher;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        return root;
    }


    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        return root;
    }




    private void initObservers()
    {
    }

    private void initListeners()
    {
//        fab_show_addLights.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { container_addLight.setVisibility(View.VISIBLE); } });
//        fab_show_addFloors.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { container_addFloor.setVisibility(View.VISIBLE); } });
//
//        fab_generateTestData_lights.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 //Should listen to sharedPreferences instead
//                Floor f1 = new Floor(0, "RDC", "This is template data - please select an .SVG file");
//                Floor f2 = new Floor(1, "1st F", "This is template data - please select an .SVG file");
//                Floor f3 = new Floor(2, "2nd F", "This is template data - please select an .SVG file");
//                Light l1 = new Light.Builder(3, 2, f1, 0).setDescription("Light in the corridor #1").setDistance(20).build();
//                Light l2 = new Light.Builder(1, 2, f1, 0).setDescription("Light in Prof. Zhang's office").setDistance(24).build();
//                Light l3 = new Light.Builder(5, 3, f2, 0).setDescription("Light in the corridor #5").setDistance(40).build();
//
//                 //Template data
//                settingsViewModel.addLight(l1);
//                settingsViewModel.addLight(l2);
//                settingsViewModel.addLight(l3);
//                settingsViewModel.addFloor(f1);
//                settingsViewModel.addFloor(f2);
//                settingsViewModel.addFloor(f3);
//
//            }
//        });

    }
}
package com.vlcnavigation.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vlcnavigation.R;
import com.vlcnavigation.components.RecyclerViewMargin;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private RecyclerView recycler_lights;
    private TextView textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        Light l1 = new Light.Builder(3, 2).setDescription("Light in the corridor #1").setDistance(20).build();
        Light l2 = new Light.Builder(1, 2).setDescription("Light in Prof. Zhang's room").setDistance(24).build();
        Light l3 = new Light.Builder(5, 3).setDescription("Light in the corridor #5").setDistance(40).build();
        Timber.d("Lights:" + '\n' + l1.toString() + '\n' + l2.toString() + '\n' + l3.toString());

        recycler_lights.setAdapter(new LightAdapter(new ArrayList<Light>(Arrays.asList(l1, l2, l3))));


        return root;
    }

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
        recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_lights.setLayoutManager(recycler_layout);

        textView = root.findViewById(R.id.text_notifications);
        return root;
    }

    private void initObservers()
    {
        settingsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
    }

    private void initListeners()
    {

    }
}
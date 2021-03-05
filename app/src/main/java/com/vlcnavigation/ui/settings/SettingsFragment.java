package com.vlcnavigation.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    private FloatingActionButton fab_addLight;
    private TextInputEditText txt_newLightXPos, txt_newLightYPos, txt_newLightDescription;
    private TextInputLayout container_newLightXPos, container_newLightYPos;

    private LightAdapter lightAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();




        Light l1 = new Light.Builder(3, 2).setDescription("Light in the corridor #1").setDistance(20).build();
        Light l2 = new Light.Builder(1, 2).setDescription("Light in Prof. Zhang's office").setDistance(24).build();
        Light l3 = new Light.Builder(5, 3).setDescription("Light in the corridor #5").setDistance(40).build();

        // Template data
        settingsViewModel.addLight(l1);
        settingsViewModel.addLight(l2);
        settingsViewModel.addLight(l3);

        return root;
    }

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
//        recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_lights.setLayoutManager(recycler_layout);
        lightAdapter = new LightAdapter(settingsViewModel.getListOfLights().getValue());
        recycler_lights.setAdapter(lightAdapter);

        fab_addLight = root.findViewById(R.id.btn_addLight);

        txt_newLightDescription = root.findViewById(R.id.txt_addLight_description);
        txt_newLightXPos = root.findViewById(R.id.txt_addLight_xPos);
        txt_newLightYPos = root.findViewById(R.id.txt_addLight_yPos);
        container_newLightXPos = root.findViewById(R.id.container_addLight_xPos);
        container_newLightYPos = root.findViewById(R.id.container_addLight_yPos);

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
        fab_addLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txt_newLightYPos.getText().length() > 0 && txt_newLightXPos.getText().length() >0)
                {
                    // Add a new light
                    Light newLight = new Light.Builder(Double.parseDouble(txt_newLightXPos.getText().toString()), Double.parseDouble(txt_newLightYPos.getText().toString()))
                            .setDescription(txt_newLightDescription.getText().toString()).build();
                    settingsViewModel.addLight(newLight);

                    // Reset UI (Add button part)
                    txt_newLightDescription.getText().clear();
                    txt_newLightXPos.getText().clear();
                    txt_newLightYPos.getText().clear();
                    container_newLightXPos.setErrorEnabled(false);
                    container_newLightYPos.setErrorEnabled(false);

                    // UX
                    Toast.makeText(getContext(), String.format("Successfully added a new light!"), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    container_newLightXPos.setErrorEnabled(true);
                    container_newLightXPos.setError("Position X is required");
                    container_newLightYPos.setErrorEnabled(true);
                    container_newLightYPos.setError("Position Y is required");
                }
            }
        });

        container_newLightXPos.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    container_newLightXPos.setError("Position X is required");
                    container_newLightXPos.setErrorEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        container_newLightYPos.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    container_newLightYPos.setError("Position X is required");
                    container_newLightYPos.setErrorEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
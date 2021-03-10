package com.vlcnavigation.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.components.DotIndicatorDecoration;
import com.vlcnavigation.components.RecyclerViewMargin;
import com.vlcnavigation.module.svg2vector.SvgFetcher;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment { // implements DefaultLifecycleObserver {

    private SettingsViewModel settingsViewModel;

    private TextView textView; // Testing purposes, fetch file

    private RecyclerView recycler_lights; // Carousel with lights
    private RecyclerView recycler_floors; // Carousel with floors

    private FloatingActionButton fab_addLight;
    private FloatingActionButton fab_generateTestData_lights, fab_show_addLights, fab_show_addFloors;

    // Adding light
//    private TextInputEditText txt_newLightXPos, txt_newLightYPos, txt_newLightDescription;
    private TextInputLayout txtInputLayout_newLightXPos, txtInputLayout_newLightYPos;
    private TextInputLayout txtInputLayout_newLightLambda, txtInputLayout_newLightDescription, txtInputLayout_newLightFloor;

    private ConstraintLayout container_addLight, container_addFloor;
    private LinearLayout container_fabs;

    private LightAdapter lightAdapter;

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

        recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
        //  Values
        lightAdapter = new LightAdapter(settingsViewModel.getListOfLights().getValue());
        recycler_lights.setAdapter(lightAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_lights.setLayoutManager(recycler_layout);
        // Margin between items
        // recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        // Item position
         recycler_lights.addItemDecoration(new DotIndicatorDecoration());
        // Snapping on a viewholder // TODO: Maybe a stronger snap. See SnapHelperBuilder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_lights);

        fab_generateTestData_lights = root.findViewById(R.id.fab_generateTestData_lights);
        fab_addLight = root.findViewById(R.id.btn_addLight);
        fab_show_addFloors = root.findViewById(R.id.fab_show_addFloors);
        fab_show_addLights = root.findViewById(R.id.fab_show_addLights);

//        txt_newLightDescription = root.findViewById(R.id.txt_addLight_description);
//        txt_newLightXPos = root.findViewById(R.id.txt_addLight_xPos);
//        txt_newLightYPos = root.findViewById(R.id.txt_addLight_yPos);

        txtInputLayout_newLightXPos = root.findViewById(R.id.txtInputLayout_addLight_xPos);
        txtInputLayout_newLightYPos = root.findViewById(R.id.txtInputLayout_addLight_yPos);
        txtInputLayout_newLightLambda = root.findViewById(R.id.txtInputLayout_addLight_lambda);
        txtInputLayout_newLightDescription = root.findViewById(R.id.txtInputLayout_addLight_description);
        txtInputLayout_newLightFloor = root.findViewById(R.id.txtInputLayout_addLight_floor);

        container_addLight = root.findViewById(R.id.txtInputLayout_addLight);
        container_fabs = root.findViewById(R.id.txtInputLayout_add_buttons);

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
        fab_show_addLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container_addLight.setVisibility(View.VISIBLE);
//                container_fabs.setVisibility(View.GONE);
            }
        });

        fab_generateTestData_lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //Should listen to sharedPreferences instead
                // FIXME
                Light l1 = new Light.Builder(3, 2, "RDC", 0).setDescription("Light in the corridor #1").setDistance(20).build();
                Light l2 = new Light.Builder(1, 2, "1st F", 0).setDescription("Light in Prof. Zhang's office").setDistance(24).build();
                Light l3 = new Light.Builder(5, 3, "2nd F", 0).setDescription("Light in the corridor #5").setDistance(40).build();

                 //Template data
                settingsViewModel.addLight(l1);
                settingsViewModel.addLight(l2);
                settingsViewModel.addLight(l3);
            }
        });
        fab_addLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtInputLayout_newLightXPos.getEditText().getText().length() > 0 &&
                        txtInputLayout_newLightYPos.getEditText().getText().length() > 0 &&
                        txtInputLayout_newLightLambda.getEditText().getText().length() > 0)
                {
                    // Add a new light
                    //FIXME
                    Light newLight = new Light.Builder(
                            Double.parseDouble(txtInputLayout_newLightXPos.getEditText().getText().toString()),
                            Double.parseDouble(txtInputLayout_newLightYPos.getEditText().getText().toString()),
                            "",
                            Double.parseDouble(txtInputLayout_newLightLambda.getEditText().getText().toString()))
                            .setDescription(txtInputLayout_newLightDescription.getEditText().getText().toString()).build();
                    settingsViewModel.addLight(newLight);

                    // Reset UI (Add button part)
                    txtInputLayout_newLightDescription.getEditText().getText().clear();
                    txtInputLayout_newLightXPos.getEditText().getText().clear();
                    txtInputLayout_newLightYPos.getEditText().getText().clear();
                    txtInputLayout_newLightLambda.getEditText().getText().clear();
                    txtInputLayout_newLightFloor.getEditText().getText().clear();

                    txtInputLayout_newLightXPos.setErrorEnabled(false);
                    txtInputLayout_newLightYPos.setErrorEnabled(false);
                    txtInputLayout_newLightLambda.setErrorEnabled(false);
                    txtInputLayout_newLightFloor.setErrorEnabled(false);

                    // UX
                    Toast.makeText(getContext(), R.string.light_added, Toast.LENGTH_SHORT).show();
                    container_addLight.setVisibility(View.GONE);
//                    container_fabs.setVisibility(View.VISIBLE);
                }
                else
                {
                    if(txtInputLayout_newLightXPos.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightXPos.setErrorEnabled(true);
                        txtInputLayout_newLightXPos.setError(getResources().getString(R.string.x_null));
                    } else { }
                    if(txtInputLayout_newLightYPos.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightYPos.setErrorEnabled(true);
                        txtInputLayout_newLightYPos.setError(getResources().getString(R.string.y_null));
                    } else { }
                    if(txtInputLayout_newLightLambda.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightLambda.setErrorEnabled(true);
                        txtInputLayout_newLightLambda.setError(getResources().getString(R.string.lambda_null));
                    } else { }
                    // FIXME Floor
                }
            }
        });

        txtInputLayout_newLightXPos.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newLightXPos.setError(getResources().getString(R.string.x_null));
                    txtInputLayout_newLightXPos.setErrorEnabled(true);
                }
            }

        });
        txtInputLayout_newLightYPos.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newLightYPos.setError(getResources().getString(R.string.y_null));
                    txtInputLayout_newLightYPos.setErrorEnabled(true);
                }
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("%s light detected", settingsViewModel.getListOfLights().getValue().size());
                for (Light light : settingsViewModel.getListOfLights().getValue())
                {
                    Timber.d(light.toString());
                }

                startActivityForResult(SvgFetcher.lookForSvgIntent(), SvgFetcher.READ_SVG_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) { return; } else {
            Timber.d("Activity Result caught. Request code: %s. Result code: %s", requestCode, resultCode);
            switch (requestCode) {
                case SvgFetcher.READ_SVG_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        String FilePath = data.getData().getPath();
                        //FilePath is your file as a string
                        Timber.d(FilePath);
                    }
                    else { Timber.e("Could not find file"); }
                    break;
            }
        }
    }



}
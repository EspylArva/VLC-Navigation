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
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment { // implements DefaultLifecycleObserver {

    private SettingsViewModel settingsViewModel;

    private TextView textView; // Testing purposes, fetch file

    private RecyclerView recycler_lights; // Carousel with lights
    private RecyclerView recycler_floors; // Carousel with floors

    private LinearLayout container_fabs;
    private FloatingActionButton fab_generateTestData_lights, fab_show_addLights, fab_show_addFloors;

    private LightAdapter lightAdapter;
    private FloorAdapter floorAdapter;
    /**
     * Add Light Panel
     */
    private ConstraintLayout container_addLight;
    private TextInputLayout txtInputLayout_newLightXPos, txtInputLayout_newLightYPos;
    private TextInputLayout txtInputLayout_newLightLambda, txtInputLayout_newLightDescription, txtInputLayout_newLightFloor;
    private FloatingActionButton fab_addLight;
    private ImageView img_hide_addLight;

    /**
     * Add floor Panel
     */
    private ConstraintLayout container_addFloor;
    private TextInputLayout txtInputLayout_newFloorOrder, txtInputLayout_newFloorFilePath, txtInputLayout_newFloorDescription;
    private FloatingActionButton fab_addFloor;
    private ImageView img_hide_addFloor;
    private Menu menu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();
        
        setHasOptionsMenu(true);

        return root;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.bottom_nav_menu, menu);
        this.menu = menu;
//        PopupMenu popupMenu = new PopupMenu(app.getApplicationContext());
//        popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());
        super.onCreateOptionsMenu(menu, inflater);
    }
    

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        recycler_lights = setRecyclerLights(root);
        recycler_floors = setRecyclerFloors(root);

        fab_generateTestData_lights = root.findViewById(R.id.fab_generateTestData_lights);
        fab_show_addFloors = root.findViewById(R.id.fab_show_addFloors);
        fab_show_addLights = root.findViewById(R.id.fab_show_addLights);

        initAddLightPanel(root);
        initAddFloorPanel(root);

        container_fabs = root.findViewById(R.id.txtInputLayout_add_buttons);

        textView = root.findViewById(R.id.text_notifications);
        return root;
    }

    private RecyclerView setRecyclerFloors(View root) {
        RecyclerView recycler_floors = root.findViewById(R.id.recycler_floors);
        recycler_floors.setHasFixedSize(true);
        //  Values
        floorAdapter = new FloorAdapter(settingsViewModel.getListOfFloors().getValue());
        recycler_floors.setAdapter(floorAdapter);
        // Orientation
        LinearLayoutManager recycler_layout2 = new LinearLayoutManager(getContext());
        recycler_layout2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_floors.setLayoutManager(recycler_layout2);
        // Margin between items
        // recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        // Item position
        recycler_floors.addItemDecoration(new DotIndicatorDecoration());
        // Snapping on a viewholder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_floors);
        return recycler_floors;
    }
    private RecyclerView setRecyclerLights(View root) {
        RecyclerView recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
        //  Values
        lightAdapter = new LightAdapter(settingsViewModel.getListOfLights().getValue(), settingsViewModel.getListOfFloors().getValue());
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
        return recycler_lights;
    }

    private void initAddFloorPanel(View root) {
        container_addFloor = root.findViewById(R.id.container_addFloor);

        txtInputLayout_newFloorOrder = root.findViewById(R.id.txtInputLayout_addFloor_order);
        txtInputLayout_newFloorFilePath = root.findViewById(R.id.txtInputLayout_addFloor_resourcePath);
        txtInputLayout_newFloorDescription = root.findViewById(R.id.txtInputLayout_addFloor_floor);

        img_hide_addFloor = root.findViewById(R.id.img_hide_addFloor);
        fab_addFloor = root.findViewById(R.id.fab_addFloor);

        // Click Listeners
        img_hide_addFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFloorPanel();
                container_addFloor.setVisibility(View.GONE);
            }
        });
        fab_addFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtInputLayout_newFloorOrder.getEditText().length() > 0 && txtInputLayout_newFloorDescription.getEditText().getText().length() > 0 && txtInputLayout_newFloorFilePath.getEditText().getText().length() > 0)
                {
                    // Add a new floor
                    settingsViewModel.addFloor(new Floor(Integer.parseInt(txtInputLayout_newFloorOrder.getEditText().getText().toString()), txtInputLayout_newFloorDescription.getEditText().getText().toString(), txtInputLayout_newFloorFilePath.getEditText().getText().toString()));
                    // TEST/ FIXME
                    menu.add(txtInputLayout_newFloorDescription.getEditText().getText().toString());

                    // Reset UI (Add button part)
                    resetFloorPanel();

                    // UX
                    Toast.makeText(getContext(), R.string.floor_added, Toast.LENGTH_SHORT).show();
                    container_addFloor.setVisibility(View.GONE);
                }
                else
                {
                    if(txtInputLayout_newFloorOrder.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newFloorOrder.setErrorEnabled(true);
                        txtInputLayout_newFloorOrder.setError(getResources().getString(R.string.floor_order_null));
                    } else { }
                    if(txtInputLayout_newFloorDescription.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newFloorDescription.setErrorEnabled(true);
                        txtInputLayout_newFloorDescription.setError(getResources().getString(R.string.floor_description_null));
                    } else { }
                    if(txtInputLayout_newFloorFilePath.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newFloorFilePath.setErrorEnabled(true);
                        txtInputLayout_newFloorFilePath.setError(getResources().getString(R.string.floor_path_null));
                    } else { }
                }
            }

        });

        txtInputLayout_newFloorDescription.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newFloorDescription.setError(getResources().getString(R.string.floor_description_null));
                    txtInputLayout_newFloorDescription.setErrorEnabled(true);
                } else { txtInputLayout_newFloorDescription.setErrorEnabled(false); }
            }

        });

        txtInputLayout_newFloorFilePath.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newFloorFilePath.setError(getResources().getString(R.string.floor_path_null));
                    txtInputLayout_newFloorFilePath.setErrorEnabled(true);
                } else { txtInputLayout_newFloorFilePath.setErrorEnabled(false); }
            }

        });
    }
    private void resetFloorPanel() {
        txtInputLayout_newFloorDescription.getEditText().getText().clear();
        txtInputLayout_newFloorFilePath.getEditText().getText().clear();

        txtInputLayout_newFloorDescription.setErrorEnabled(false);
        txtInputLayout_newFloorFilePath.setErrorEnabled(false);
    }

    private void initAddLightPanel(View root) {
        container_addLight = root.findViewById(R.id.container_addLight);

        txtInputLayout_newLightXPos = root.findViewById(R.id.txtInputLayout_addLight_xPos);
        txtInputLayout_newLightYPos = root.findViewById(R.id.txtInputLayout_addLight_yPos);
        txtInputLayout_newLightLambda = root.findViewById(R.id.txtInputLayout_addLight_lambda);
        txtInputLayout_newLightDescription = root.findViewById(R.id.txtInputLayout_addLight_description);
        txtInputLayout_newLightFloor = root.findViewById(R.id.txtInputLayout_addLight_floor);

        img_hide_addLight = root.findViewById(R.id.img_hide_addLight);
        fab_addLight = root.findViewById(R.id.fab_addLight);

        // Click Listeners
        img_hide_addLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetLightPanel();
                container_addLight.setVisibility(View.GONE);
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
                    Light newLight = new Light.Builder(
                            Double.parseDouble(txtInputLayout_newLightXPos.getEditText().getText().toString()),
                            Double.parseDouble(txtInputLayout_newLightYPos.getEditText().getText().toString()),
                            null, // txtInputLayout_newLightFloor.getEditText().getText().toString(), // FIXME: REASON FOR ERRORS WHEN CLICKING ON THE TEXTVIEW
                            Double.parseDouble(txtInputLayout_newLightLambda.getEditText().getText().toString()))
                            .setDescription(txtInputLayout_newLightDescription.getEditText().getText().toString()).build();
                    settingsViewModel.addLight(newLight);

                    // Reset UI (Add button part)
                    resetLightPanel();

                    // UX
                    Toast.makeText(getContext(), R.string.light_added, Toast.LENGTH_SHORT).show();
                    container_addLight.setVisibility(View.GONE);
                }
                else
                {
                    if(txtInputLayout_newLightXPos.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightXPos.setErrorEnabled(true);
                        txtInputLayout_newLightXPos.setError(getResources().getString(R.string.light_x_null));
                    } else { }
                    if(txtInputLayout_newLightYPos.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightYPos.setErrorEnabled(true);
                        txtInputLayout_newLightYPos.setError(getResources().getString(R.string.light_y_null));
                    } else { }
                    if(txtInputLayout_newLightLambda.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightLambda.setErrorEnabled(true);
                        txtInputLayout_newLightLambda.setError(getResources().getString(R.string.light_lambda_null));
                    } else { }
                    if(txtInputLayout_newLightFloor.getEditText().getText().length() == 0)
                    {
                        txtInputLayout_newLightFloor.setErrorEnabled(true);
                        txtInputLayout_newLightFloor.setError(getResources().getString(R.string.light_floor_null));
                    } else { }
                }
            }
        });
        // Content changed listener
        txtInputLayout_newLightXPos.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newLightXPos.setError(getResources().getString(R.string.light_x_null));
                    txtInputLayout_newLightXPos.setErrorEnabled(true);
                } else { txtInputLayout_newLightXPos.setErrorEnabled(false); }
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
                    txtInputLayout_newLightYPos.setError(getResources().getString(R.string.light_y_null));
                    txtInputLayout_newLightYPos.setErrorEnabled(true);
                } else { txtInputLayout_newLightYPos.setErrorEnabled(false); }
            }
        });
        txtInputLayout_newLightLambda.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 ) {
                    txtInputLayout_newLightLambda.setError(getResources().getString(R.string.light_lambda_null));
                    txtInputLayout_newLightLambda.setErrorEnabled(true);
                } else { txtInputLayout_newLightLambda.setErrorEnabled(false); }
            }
        });
    }
    private void resetLightPanel() {
        txtInputLayout_newLightDescription.getEditText().getText().clear();
        txtInputLayout_newLightXPos.getEditText().getText().clear();
        txtInputLayout_newLightYPos.getEditText().getText().clear();
        txtInputLayout_newLightLambda.getEditText().getText().clear();
        txtInputLayout_newLightFloor.getEditText().getText().clear();

        txtInputLayout_newLightXPos.setErrorEnabled(false);
        txtInputLayout_newLightYPos.setErrorEnabled(false);
        txtInputLayout_newLightLambda.setErrorEnabled(false);
        txtInputLayout_newLightFloor.setErrorEnabled(false);
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
        fab_show_addLights.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { container_addLight.setVisibility(View.VISIBLE); } });
        fab_show_addFloors.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { container_addFloor.setVisibility(View.VISIBLE); } });

        fab_generateTestData_lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //Should listen to sharedPreferences instead
                Floor f1 = new Floor(-1, "RDC", "Tessst");
                Floor f2 = new Floor(-2, "1st F", "Tessst");
                Floor f3 = new Floor(-3, "2nd F", "Tessst");
                Light l1 = new Light.Builder(3, 2, f1, 0).setDescription("Light in the corridor #1").setDistance(20).build();
                Light l2 = new Light.Builder(1, 2, f1, 0).setDescription("Light in Prof. Zhang's office").setDistance(24).build();
                Light l3 = new Light.Builder(5, 3, f2, 0).setDescription("Light in the corridor #5").setDistance(40).build();

                 //Template data
                settingsViewModel.addLight(l1);
                settingsViewModel.addLight(l2);
                settingsViewModel.addLight(l3);
                settingsViewModel.addFloor(f1);
                settingsViewModel.addFloor(f2);
                settingsViewModel.addFloor(f3);

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
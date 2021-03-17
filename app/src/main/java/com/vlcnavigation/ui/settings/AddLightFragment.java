package com.vlcnavigation.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AddLightFragment extends Fragment {
    private TextInputLayout txtInputLayout_newLightXPos, txtInputLayout_newLightYPos;
    private TextInputLayout txtInputLayout_newLightLambda, txtInputLayout_newLightDescription, txtInputLayout_newLightFloor;
    private FloatingActionButton fab_addLight;

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        return root;
    }

    private void initListeners() {
        ListPopupWindow listPopupWindow = new ListPopupWindow(getContext(), null, R.attr.listPopupWindowStyle);
        listPopupWindow.setAnchorView(txtInputLayout_newLightFloor);
        List<Integer> floorOrders = new ArrayList<>(); settingsViewModel.getListOfFloors().getValue().forEach(floor -> floorOrders.add(floor.getOrder()));
        Timber.d("List: %s", floorOrders);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getContext(), R.layout.menu_layout_floor, floorOrders);
        listPopupWindow.setAdapter(adapter);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int newFloorOrder = floorOrders.get(position);
                txtInputLayout_newLightFloor.getEditText().setText(String.valueOf(newFloorOrder));
                listPopupWindow.dismiss();
            }
        });
        txtInputLayout_newLightFloor.getEditText().setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { listPopupWindow.show(); } });

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
                            settingsViewModel.findFloor(Integer.parseInt(txtInputLayout_newLightFloor.getEditText().getText().toString())),
                            Double.parseDouble(txtInputLayout_newLightLambda.getEditText().getText().toString()))
                            .setDescription(txtInputLayout_newLightDescription.getEditText().getText().toString()).build();
                    settingsViewModel.addLight(newLight);

                    // Reset UI (Add button part)
                    resetLightPanel();

                    // UX
                    Toast.makeText(getContext(), R.string.light_added, Toast.LENGTH_SHORT).show();
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

    private void initObservers() {
    }

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.tabitem_add_light, container, false);
        txtInputLayout_newLightXPos = root.findViewById(R.id.txtInputLayout_addLight_xPos);
        txtInputLayout_newLightYPos = root.findViewById(R.id.txtInputLayout_addLight_yPos);
        txtInputLayout_newLightLambda = root.findViewById(R.id.txtInputLayout_addLight_lambda);
        txtInputLayout_newLightDescription = root.findViewById(R.id.txtInputLayout_addLight_description);
        txtInputLayout_newLightFloor = root.findViewById(R.id.txtInputLayout_addLight_floor);

        fab_addLight = root.findViewById(R.id.fab_addLight);
        return root;
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
}

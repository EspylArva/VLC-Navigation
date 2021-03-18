package com.vlcnavigation.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgFetcher;
import com.vlcnavigation.module.trilateration.Floor;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class AddFloorFragment extends Fragment {
    private TextInputLayout txtInputLayout_newFloorOrder, txtInputLayout_newFloorFilePath, txtInputLayout_newFloorDescription;
    private FloatingActionButton fab_addFloor;
    private String trueUri;

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(getParentFragment() != null) { settingsViewModel = new ViewModelProvider(getParentFragment()).get(SettingsViewModel.class); }
        else { settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class); Timber.e("Failed getting the correct view model"); }
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        return root;
    }

    private void initListeners() {
        fab_addFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtInputLayout_newFloorOrder.getEditText().length() > 0 && txtInputLayout_newFloorDescription.getEditText().getText().length() > 0 && txtInputLayout_newFloorFilePath.getEditText().getText().length() > 0)
                {
                    Floor newFloor = new Floor(Integer.parseInt(txtInputLayout_newFloorOrder.getEditText().getText().toString()),
                            txtInputLayout_newFloorDescription.getEditText().getText().toString(),
//                            txtInputLayout_newFloorFilePath.getEditText().getText().toString())
                            trueUri);
                    if(settingsViewModel.getListOfFloors().getValue().contains(newFloor))
                    {
                        txtInputLayout_newFloorOrder.setErrorEnabled(true);
                        txtInputLayout_newFloorOrder.setError(getResources().getString(R.string.floor_already_exists));
                    }
                    else
                    {
                        // Add a new floor
                        settingsViewModel.addFloor(newFloor);

                        // Reset UI (Add button part)
                        resetFloorPanel();
                        ((SettingsFragment) getParentFragment()).notifyFloorRecycler();

                        // UX
                        Toast.makeText(getContext(), R.string.floor_added, Toast.LENGTH_SHORT).show();
                    }
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
        txtInputLayout_newFloorFilePath.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(SvgFetcher.lookForSvgIntent(),
                        SvgFetcher.ADD_SVG_REQUEST_CODE);
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

    private void initObservers() {
    }

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.tabitem_add_floor, container, false);
        txtInputLayout_newFloorOrder = root.findViewById(R.id.txtInputLayout_addFloor_order);
        txtInputLayout_newFloorFilePath = root.findViewById(R.id.txtInputLayout_addFloor_resourcePath);
        txtInputLayout_newFloorDescription = root.findViewById(R.id.txtInputLayout_addFloor_floor);

        fab_addFloor = root.findViewById(R.id.fab_addFloor);
        return root;
    }

    private void resetFloorPanel() {
        txtInputLayout_newFloorDescription.getEditText().getText().clear();
        txtInputLayout_newFloorFilePath.getEditText().getText().clear();

        txtInputLayout_newFloorDescription.setErrorEnabled(false);
        txtInputLayout_newFloorFilePath.setErrorEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            Timber.d("Activity Result caught. Request code: %s. Result code: %s", requestCode, resultCode);
            String filePath = data.getData().toString();

            if (requestCode == SvgFetcher.ADD_SVG_REQUEST_CODE) {
                Timber.d("From add floor: %s", filePath);
                trueUri = filePath;
                txtInputLayout_newFloorFilePath.getEditText().setText(filePath.split("%2F")[filePath.split("%2F").length - 1]);
            }
        } else { Timber.e("Could not find file"); }
    }

}

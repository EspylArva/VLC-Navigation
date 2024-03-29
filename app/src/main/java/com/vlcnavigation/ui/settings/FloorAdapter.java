package com.vlcnavigation.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgFetcher;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;
import com.vlcnavigation.module.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.vlcnavigation.module.svg2vector.SvgFetcher.READ_SVG_REQUEST_CODE;

//TODO: Javadoc
public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorHolder>{

    private final SettingsViewModel vm;
    private final FloorsLightsManagerFragment fragment;

    public FloorAdapter(SettingsViewModel vm, FloorsLightsManagerFragment fragment)
    {
        this.vm = vm;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public FloorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_floor_entry, parent, false);
        return new FloorHolder(v, vm, fragment);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorHolder holder, int position) {
        holder.setFloor();
        holder.refreshUI();
    }

    @Override
    public int getItemCount() {
        return vm.getListOfFloors().getValue().size();
    }

    public class FloorHolder extends RecyclerView.ViewHolder {

        private final SettingsViewModel vm;                 // Data
        private final FloorsLightsManagerFragment fragment; // Necessary to handle the callback
        private Floor floor;                                // Not necessary, but makes the code shorter

        // Views
        private TextInputLayout txtInputLayout_order, txtInputLayout_description, txtInputLayout_filePath;
        private AppCompatButton btn_deleteEntry;

        public FloorHolder(@NonNull View itemView, SettingsViewModel vm, FloorsLightsManagerFragment fragment) {
            super(itemView);
            this.vm = vm;
            this.fragment = fragment;
            initViews(itemView);        // Instantiate views
            initOnClickListeners();     // Click listeners. Currently: 2 (Remove button, File path TextInputEditText)
            initTextChangeListener();   // When a text field is changed, we want to save it to SP
        }

        public void setFloor() {
            this.floor = vm.getListOfFloors().getValue().get(getAdapterPosition());
        }


        private void initViews(View itemView) {
            this.txtInputLayout_order = itemView.findViewById(R.id.txtInputLayout_floor_order);
            this.txtInputLayout_description = itemView.findViewById(R.id.txtInputLayout_floor_description);
            this.txtInputLayout_filePath = itemView.findViewById(R.id.txtInputLayout_filePath);
            this.btn_deleteEntry = itemView.findViewById(R.id.btn_deleteFloorEntry);
        }

        public void refreshUI()
        {
            this.txtInputLayout_order.getEditText().setText(String.valueOf(floor.getOrder()));
            this.txtInputLayout_description.getEditText().setText(floor.getDescription());
            if(floor.getFilePath() != null && !floor.getFilePath().isEmpty())
            {
                this.txtInputLayout_filePath.getEditText().setText(floor.getFilePath().split("%2F")[floor.getFilePath().split("%2F").length-1]);
            }
        }

        private void initTextChangeListener(){

            txtInputLayout_order.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().endsWith("-") || s.toString().endsWith("+") || s.toString().endsWith(".")) {
                        txtInputLayout_order.setError(itemView.getContext().getResources().getString(R.string.floor_order_null));
                        txtInputLayout_order.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_order.setErrorEnabled(false);
                        int order = Integer.parseInt(s.toString());
                        if(vm.getListOfFloors().getValue().stream().filter(f -> f.getOrder() == order).count() == 0 || order == vm.getListOfFloors().getValue().get(getAdapterPosition()).getOrder())
                        {
                            vm.getListOfFloors().getValue().get(getAdapterPosition()).setOrder(order);
                            vm.saveFloors();
                        }
                        else
                        {
                            txtInputLayout_order.setErrorEnabled(true);
                            txtInputLayout_order.setError(itemView.getContext().getResources().getString(R.string.floor_already_exists));
                        }
                    }

                }
            });
            txtInputLayout_description.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        txtInputLayout_description.setError(itemView.getContext().getResources().getString(R.string.floor_description_null));
                        txtInputLayout_description.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_description.setErrorEnabled(false);
                        vm.getListOfFloors().getValue().get(getAdapterPosition()).setDescription(s.toString());
                        vm.saveFloors();
                    }
                }
            });
        }

        private void initOnClickListeners()
        {
            txtInputLayout_filePath.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.startActivityForResult(SvgFetcher.lookForSvgIntent(),
                            SvgFetcher.READ_SVG_REQUEST_CODE);
                }
            });

            btn_deleteEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position > -1)
                    {
                        Util.hideKeyboardFromView(v);
                        vm.removeFloorAt(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position,  vm.getListOfFloors().getValue().size());
                    }
                }
            });
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.vlcnavigation.module.svg2vector.SvgFetcher.READ_SVG_REQUEST_CODE;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorHolder>{

    private List<Floor> floors;
    private SettingsFragment fragment;
    public FloorAdapter(List<Floor> floors, SettingsFragment fragment)
    {
        this.floors = floors;
        this.fragment = fragment;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
//        if(vm == null)
//        {
//            vm = new ViewModelProvider((ViewModelStoreOwner)recyclerView.getContext()).get(SettingsViewModel.class)
//        }
    }

    @NonNull
    @Override
    public FloorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_floor_entry, parent, false);
        return new FloorHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorHolder holder, int position) {
        Floor floor = floors.get(position);

        // Set Holder values
        holder.setOrder(floor.getOrder());
        holder.setDescription(floor.getDescription());
        holder.setFilePath(floor.getFilePath());
        // Refresh UI
        holder.refreshUI();

        // Set Listeners
        holder.setRemoveButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if(position > -1)
                {
                    floors.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, floors.size());

                    holder.saveInSharedPreferences(floors);
                }
            }
        });
        holder.setPathClickListener(fragment);
        holder.setTextChangeListener(floors);

    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    public static class FloorHolder extends RecyclerView.ViewHolder {
        // Field values
        private String description, filePath;
        private int order;
        public void setOrder(int order) { this.order = order; }
        public void setDescription(String description) { this.description = description; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        // Views
        private TextInputLayout txtInputLayout_order, txtInputLayout_description, txtInputLayout_filePath;
        private ImageView img_deleteEntry;

        public FloorHolder(@NonNull View itemView) {
            super(itemView);

            initViews(itemView);
        }

        private void initViews(View itemView) {
            this.txtInputLayout_order = itemView.findViewById(R.id.txtInputLayout_floor_order);
            this.txtInputLayout_description = itemView.findViewById(R.id.txtInputLayout_floor_description);
            this.txtInputLayout_filePath = itemView.findViewById(R.id.txtInputLayout_filePath);
            this.img_deleteEntry = itemView.findViewById(R.id.img_deleteFloorEntry);
        }

        public void refreshUI()
        {
            this.txtInputLayout_order.getEditText().setText(String.valueOf(order));
            this.txtInputLayout_description.getEditText().setText(description);
            this.txtInputLayout_filePath.getEditText().setText(filePath);
        }

        public void setTextChangeListener(List<Floor> floors) {

            txtInputLayout_order.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
                        txtInputLayout_order.setError(itemView.getContext().getResources().getString(R.string.floor_order_null));
                        txtInputLayout_order.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_order.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        int order = Integer.parseInt(s.toString());
                        if(floors.stream().filter(f -> f.getOrder() == order).count() == 0 || order == floors.get(position).getOrder())
                        {
                            floors.get(position).setOrder(order);
                            saveInSharedPreferences(floors);
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
                        int position = getAdapterPosition();
                        floors.get(position).setDescription(s.toString());
                        saveInSharedPreferences(floors);
                    }
                }
            });
            txtInputLayout_filePath.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        txtInputLayout_filePath.setError(itemView.getContext().getResources().getString(R.string.floor_path_null));
                        txtInputLayout_filePath.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_filePath.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        floors.get(position).setFilePath(s.toString());
                        saveInSharedPreferences(floors);
                    }
                }
            });
        }

        public void setRemoveButton(View.OnClickListener onClickListener) { img_deleteEntry.setOnClickListener(onClickListener); }
        public void setPathClickListener(SettingsFragment fragment)
        {
            txtInputLayout_filePath.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.startActivityForResult(SvgFetcher.lookForSvgIntent(),
                            SvgFetcher.READ_SVG_REQUEST_CODE);
                }
            });
        }

        public void saveInSharedPreferences(List<Floor> floors)
        {
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
            String json = new Gson().toJson(floors);
            prefs.edit().putString(itemView.getContext().getResources().getString(R.string.sp_map), json).apply();
        }
    }
}

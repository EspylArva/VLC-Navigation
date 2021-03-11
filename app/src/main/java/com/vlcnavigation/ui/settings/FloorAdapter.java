package com.vlcnavigation.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorHolder>{

    private List<Floor> floors;
    public FloorAdapter(List<Floor> floors)
    {
        this.floors = floors;
    }

    @NonNull
    @Override
    public FloorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_floor_entry, parent, false);
        return new FloorAdapter.FloorHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorHolder holder, int position) {
        Floor floor = floors.get(position);

        // Set Holder values
        holder.setDescription(floor.getDescription());
        holder.setFilePath(floor.getFilePath());

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
        holder.setFloorMenuListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // switch your menu item and do something..
                String floor = item.getTitle().toString();
                int position = holder.getAdapterPosition();
                floors.get(position).setDescription(floor);

                holder.getTxtInputLayout_description().getEditText().setText(floor);
                holder.saveInSharedPreferences(floors);
                return true;
            }
        });
//        holder.setTextChangeListener(null, null); // FIXME

        // Refresh UI
        holder.refreshUI();
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    public class FloorHolder extends RecyclerView.ViewHolder {
        // Field values
        private String description, filePath;
        public void setDescription(String description) { this.description = description; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        // Views
        private TextInputLayout txtInputLayout_description, txtInputLayout_filePath;
        private ImageView img_deleteEntry;

        public TextInputLayout getTxtInputLayout_description() { return this.txtInputLayout_description; }
        public TextInputLayout getTxtInputLayout_filePath() { return this.txtInputLayout_filePath; }

        public FloorHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            this.txtInputLayout_description = itemView.findViewById(R.id.txtInputLayout_floor_description);
            this.txtInputLayout_filePath = itemView.findViewById(R.id.txtInputLayout_filePath);
            this.img_deleteEntry = itemView.findViewById(R.id.img_deleteFloorEntry);
        }

        public void refreshUI()
        {
            this.txtInputLayout_description.getEditText().setText(description);
            this.txtInputLayout_filePath.getEditText().setText(filePath);
        }

        public void setTextChangeListener(TextWatcher textWatcher_description, TextWatcher textWatcher_filePath) {
            Objects.requireNonNull(txtInputLayout_description.getEditText()).addTextChangedListener(textWatcher_description);
            Objects.requireNonNull(txtInputLayout_filePath.getEditText()).addTextChangedListener(textWatcher_filePath);
        }

        public void setFloorMenuListener(PopupMenu.OnMenuItemClickListener onMenuClick)
        {
            PopupMenu popupMenu = new PopupMenu(itemView.getContext(), txtInputLayout_description.getEditText());
            popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(onMenuClick);

            txtInputLayout_description.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show();
                }
            });
        }

        public void setRemoveButton(View.OnClickListener onClickListener) { img_deleteEntry.setOnClickListener(onClickListener); }

        public void saveInSharedPreferences(List<Floor> floors)
        {
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
            String json = new Gson().toJson(floors);
            prefs.edit().putString(itemView.getContext().getResources().getString(R.string.sp_map), json).apply();
        }
    }
}

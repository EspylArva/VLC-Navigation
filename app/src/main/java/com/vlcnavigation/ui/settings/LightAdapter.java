package com.vlcnavigation.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Light;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.LightHolder> {

    private List<Light> lights;
    public LightAdapter(List<Light> lights)
    {
        this.lights = lights;
        Timber.d("We're in the adapter");
    }

    @NonNull
    @Override
    public LightHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_light_entry, parent, false);
        Timber.d("Creating an entry");
        LightHolder vh = new LightHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull LightHolder holder, int position) {
        Light l = this.lights.get(position);
        Timber.d("Light given as param: %s", l.toString());

        holder.setLbl_light_description(l.getLabel());
        holder.setX(l.getPosX());
        holder.setY(l.getPosY());
        holder.setDistance(l.getDistance());

        holder.initListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if(position > -1)
                {
                    lights.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, lights.size());

                    holder.saveInSharedPreferences(lights);
                }
            }
        });

        holder.setTextChangeListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getContainer_posX().setError(holder.itemView.getContext().getResources().getString(R.string.x_null));
                            holder.getContainer_posX().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getContainer_posX().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).setPosX(Double.parseDouble(s.toString()));
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                },

                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getContainer_posY().setError(holder.itemView.getContext().getResources().getString(R.string.y_null));
                            holder.getContainer_posY().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getContainer_posY().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).setPosY(Double.parseDouble(s.toString()));
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                }
        );

        //FIXME: keyboard pushes the views
        //FIXME: Selecting other view resets the position of the recycler view

        holder.refreshUI();
    }

    @Override
    public int getItemCount() {
        return this.lights.size();
    }

    public static class LightHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView lbl_light_description;
        private TextInputLayout container_posX, container_posY;
        private TextInputEditText txt_posX, txt_posY;//, txt_distance;
        private TextView lbl_distance;
        private FloatingActionButton fab_deleteEntry;

        private double posX, posY, distance;

        public LightHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            this.lbl_light_description = itemView.findViewById(R.id.lbl_light_description);
            this.txt_posX = itemView.findViewById(R.id.txt_light_positionX);
            this.txt_posY = itemView.findViewById(R.id.txt_light_positionY);
//            this.txt_distance = itemView.findViewById(R.id.txt_light_distance);
            this.lbl_distance = itemView.findViewById(R.id.txt_light_distance);
            this.fab_deleteEntry = itemView.findViewById(R.id.fab_deleteEntry);

            this.container_posX = itemView.findViewById(R.id.container_light_x);
            this.container_posY = itemView.findViewById(R.id.container_light_y);
        }

        @Override
        public void onClick(View v)
        {
            if(v.equals(fab_deleteEntry))
            {
                // Remove
                int position = getAdapterPosition();

            } else { }
        }

        public void initListeners(View.OnClickListener listener)
        {
            this.fab_deleteEntry.setOnClickListener(listener);
        }

        public void refreshUI()
        {
            this.txt_posX.setText(String.valueOf(posX));
            this.txt_posY.setText(String.valueOf(posY));
//            this.txt_distance.setText(String.valueOf(distance));
            this.lbl_distance.setText(String.valueOf(distance));
        }

        public TextInputLayout getContainer_posX() { return this.container_posX; }
        public TextInputLayout getContainer_posY() { return this.container_posY; }

        public void setLbl_light_description(String description) { this.lbl_light_description.setText(description); }
        public void setX(Double x) { this.posX = x; }
        public void setY(Double y) { this.posY = y; }
        public void setDistance(double distance) { this.distance = distance; }

        public void setTextChangeListener(TextWatcher textWatcher_posX, TextWatcher textWatcher_posY) {
            Objects.requireNonNull(container_posX.getEditText()).addTextChangedListener(textWatcher_posX);
            Objects.requireNonNull(container_posY.getEditText()).addTextChangedListener(textWatcher_posY);

        }

        public void saveInSharedPreferences(List<Light> lights)
        {
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
            String json = new Gson().toJson(lights);
            prefs.edit().putString(itemView.getContext().getResources().getString(R.string.sp_lights), json).apply();
        }
    }


}

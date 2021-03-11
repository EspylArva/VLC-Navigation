package com.vlcnavigation.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import org.w3c.dom.Text;

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
        return new LightHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LightHolder holder, int position) {
        Light l = this.lights.get(position);

        // Set Holder values
        holder.setDescription(l.getLabel());
        holder.setX(l.getPosX()); holder.setY(l.getPosY());
        holder.setDistance(l.getDistance());
        holder.setLambda(l.getLambda());
        holder.setFloor(l.getFloor());

        // Set Listeners
        holder.setRemoveButton(new View.OnClickListener() {
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
        holder.setTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getTxtInputLayout_posX().setError(holder.itemView.getContext().getResources().getString(R.string.x_null));
                            holder.getTxtInputLayout_posX().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getTxtInputLayout_posX().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).setPosX(Double.parseDouble(s.toString()));
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                }, new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getTxtInputLayout_posY().setError(holder.itemView.getContext().getResources().getString(R.string.y_null));
                            holder.getTxtInputLayout_posY().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getTxtInputLayout_posY().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).setPosY(Double.parseDouble(s.toString()));
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                }, // X, Y Listener
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getTxtInputLayout_lambda().setError(holder.itemView.getContext().getResources().getString(R.string.y_null));
                            holder.getTxtInputLayout_lambda().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getTxtInputLayout_lambda().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).setLambda(Double.parseDouble(s.toString()));
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                }, new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0 ) {
                            holder.getTxtInputLayout_floor().setError(holder.itemView.getContext().getResources().getString(R.string.y_null));
                            holder.getTxtInputLayout_floor().setErrorEnabled(true);
                        }
                        else
                        {
                            holder.getTxtInputLayout_floor().setErrorEnabled(false);
                            int position = holder.getAdapterPosition();
                            lights.get(position).getFloor().setDescription(s.toString());
                            holder.saveInSharedPreferences(lights);
                        }
                    }
                }); // Lambda, Floor listener
        holder.setFloorMenuListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // switch your menu item and do something..
                String floor = item.getTitle().toString();
                int position = holder.getAdapterPosition();
                lights.get(position).getFloor().setDescription(floor);
                holder.getTxtInputLayout_floor().getEditText().setText(floor);
                holder.saveInSharedPreferences(lights);
                Timber.d("Floor: %s set on light #%s", floor, position);
                return true;
            }
        });

        // Refresh UI
        holder.refreshUI();

        //FIXME: keyboard pushes the views
        //FIXME: Selecting other view resets the position of the recycler view
    }

    @Override
    public int getItemCount() {
        return this.lights.size();
    }

    public static class LightHolder extends RecyclerView.ViewHolder
    {
        private TextInputLayout txtInputDescription;
        private TextInputLayout txtInputLayout_posX, txtInputLayout_posY, txtInputLayout_floor;
        private TextInputLayout txtInputLayout_lambda, txtInputLayout_distance;
        private ImageView img_deleteEntry;

        private double posX, posY, distance, lambda;
        private Floor floor;

        public LightHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            this.txtInputDescription = itemView.findViewById(R.id.txtInputLayout_light_description);
            this.txtInputLayout_posX = itemView.findViewById(R.id.txtInputLayout_light_x);
            this.txtInputLayout_posY = itemView.findViewById(R.id.txtInputLayout_light_y);
            this.txtInputLayout_floor = itemView.findViewById(R.id.txtInputLayout_light_floor);
            this.txtInputLayout_lambda = itemView.findViewById(R.id.txtInputLayout_light_lambda);
            this.txtInputLayout_distance = itemView.findViewById(R.id.txtInputLayout_light_distance);

            this.img_deleteEntry = itemView.findViewById(R.id.img_deleteEntry);
        }

        public void setFloorMenuListener(PopupMenu.OnMenuItemClickListener onMenuClick)
        {
            PopupMenu popupMenu = new PopupMenu(itemView.getContext(), txtInputLayout_floor.getEditText());
            popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(onMenuClick);

            txtInputLayout_floor.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show();
                }
            });
        }

        public void setRemoveButton(View.OnClickListener listener) { this.img_deleteEntry.setOnClickListener(listener); }

        public void refreshUI()
        {
            this.txtInputLayout_posX.getEditText().setText(String.valueOf(posX));
            this.txtInputLayout_posY.getEditText().setText(String.valueOf(posY));
            this.txtInputLayout_lambda.getEditText().setText(String.valueOf(lambda));
            this.txtInputLayout_distance.getEditText().setText(String.valueOf(distance));
//            this.txtInputLayout_floor.getEditText().setText(floor.getDescription());
        }

        public TextInputLayout getTxtInputLayout_posX()     { return this.txtInputLayout_posX; }
        public TextInputLayout getTxtInputLayout_posY()     { return this.txtInputLayout_posY; }
        public TextInputLayout getTxtInputLayout_lambda()   { return this.txtInputLayout_lambda; }
        public TextInputLayout getTxtInputLayout_floor()    { return this.txtInputLayout_floor; }

        public void setDescription(String description) { this.txtInputDescription.getEditText().setText(description); }
        public void setX(Double x) { this.posX = x; }
        public void setY(Double y) { this.posY = y; }
        public void setDistance(double distance) { this.distance = distance; }
        public void setLambda(double lambda) { this.lambda = lambda; }
        public void setFloor(Floor floor) { this.floor = floor; }

        public void setTextChangeListener(TextWatcher textWatcher_posX, TextWatcher textWatcher_posY, TextWatcher textWatcher_lambda, TextWatcher textWatcher_floor) {
            Objects.requireNonNull(txtInputLayout_posX.getEditText()).addTextChangedListener(textWatcher_posX);
            Objects.requireNonNull(txtInputLayout_posY.getEditText()).addTextChangedListener(textWatcher_posY);
            Objects.requireNonNull(txtInputLayout_lambda.getEditText()).addTextChangedListener(textWatcher_lambda);
            Objects.requireNonNull(txtInputLayout_floor.getEditText()).addTextChangedListener(textWatcher_floor);
        }

        public void saveInSharedPreferences(List<Light> lights)
        {
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
            String json = new Gson().toJson(lights);
            prefs.edit().putString(itemView.getContext().getResources().getString(R.string.sp_lights), json).apply();
        }

    }


}

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.LightHolder> {

    private List<Light> lights;
    private List<Floor> floors;
    public LightAdapter(List<Light> lights, List<Floor> floors)
    {
        this.lights = lights;
        this.floors = floors;
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
        // Refresh UI
        holder.refreshUI();

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
        holder.setTextChangeListener(lights); // Lambda, Floor listener
        holder.setFloorMenuListener(lights, floors, position);


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



        public void setRemoveButton(View.OnClickListener listener) { this.img_deleteEntry.setOnClickListener(listener); }

        public void refreshUI()
        {
            this.txtInputLayout_posX.getEditText().setText(String.valueOf(posX));
            this.txtInputLayout_posY.getEditText().setText(String.valueOf(posY));
            this.txtInputLayout_lambda.getEditText().setText(String.valueOf(lambda));
            this.txtInputLayout_distance.getEditText().setText(String.valueOf(distance));
            this.txtInputLayout_floor.getEditText().setText(String.valueOf(floor.getOrder()));
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

        public void setTextChangeListener(List<Light> lights) {
            txtInputLayout_posX.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
                        txtInputLayout_posX.setError(itemView.getContext().getResources().getString(R.string.light_x_null));
                        txtInputLayout_posX.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_posX.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        lights.get(position).setPosX(Double.parseDouble(s.toString()));
                        saveInSharedPreferences(lights);
                    }
                }
            });
            txtInputLayout_posY.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
                        txtInputLayout_posY.setError(itemView.getContext().getResources().getString(R.string.light_y_null));
                        txtInputLayout_posY.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_posY.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        lights.get(position).setPosY(Double.parseDouble(s.toString()));
                        saveInSharedPreferences(lights);
                    }
                }
            });
            txtInputLayout_lambda.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
                        txtInputLayout_lambda.setError(itemView.getContext().getResources().getString(R.string.light_lambda_null));
                        txtInputLayout_lambda.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_lambda.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        lights.get(position).setLambda(Double.parseDouble(s.toString()));
                        saveInSharedPreferences(lights);
                    }
                }
            });
            txtInputLayout_floor.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
                        txtInputLayout_floor.setError(itemView.getContext().getResources().getString(R.string.light_floor_null));
                        txtInputLayout_floor.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_floor.setErrorEnabled(false);
                        int position = getAdapterPosition();
                        lights.get(position).getFloor().setDescription(s.toString());
                        saveInSharedPreferences(lights);
                    }
                }
            });

        }

        public void saveInSharedPreferences(List<Light> lights)
        {
            SharedPreferences prefs = itemView.getContext().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
            String json = new Gson().toJson(lights);
            prefs.edit().putString(itemView.getContext().getResources().getString(R.string.sp_lights), json).apply();
        }

        public void setFloorMenuListener(List<Light> lights, List<Floor> floors, int modifiedLightIndex)
        {
            ListPopupWindow listPopupWindow = new ListPopupWindow(itemView.getContext(), null, R.attr.listPopupWindowStyle);
            listPopupWindow.setAnchorView(txtInputLayout_floor);

            List<Integer> floorOrders = new ArrayList<>(); floors.forEach(floor -> floorOrders.add(floor.getOrder()));
            ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(itemView.getContext(), R.layout.menu_layout_floor, floorOrders);
            listPopupWindow.setAdapter(adapter);

            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Dismiss popup.
                    int newFloorOrder = floorOrders.get(position);
//                    floors.get(floorPosition).setOrder(newOrder);
                    lights.get(modifiedLightIndex).getFloor().setOrder(newFloorOrder);

                    txtInputLayout_floor.getEditText().setText(String.valueOf(newFloorOrder));
                    saveInSharedPreferences(lights);

                    listPopupWindow.dismiss();
                }
            });
            txtInputLayout_floor.getEditText().setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { listPopupWindow.show(); } });
        }

    }


}

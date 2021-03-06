package com.vlcnavigation.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Light;

import java.util.List;
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
        holder.setX(l.getPosXY().first);
        holder.setY(l.getPosXY().second);
        holder.setDistance(l.getDistance());

        holder.initListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                lights.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, lights.size());
            }
        });

        //TODO: make sure modification on a light has repercussions
        //FIXME: keyboard pushes the views

        holder.refreshUI();
    }

    @Override
    public int getItemCount() {
        return this.lights.size();
    }

    public static class LightHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView lbl_light_description;
        TextInputEditText txt_posX, txt_posY;//, txt_distance;
        TextView lbl_distance;
        FloatingActionButton fab_deleteEntry;

        private double posX, posY, distance;

        public LightHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
//            initListeners();
        }

        private void initViews(View itemView) {
            this.lbl_light_description = itemView.findViewById(R.id.lbl_light_description);
            this.txt_posX = itemView.findViewById(R.id.txt_light_positionX);
            this.txt_posY = itemView.findViewById(R.id.txt_light_positionY);
//            this.txt_distance = itemView.findViewById(R.id.txt_light_distance);
            this.lbl_distance = itemView.findViewById(R.id.txt_light_distance);
            this.fab_deleteEntry = itemView.findViewById(R.id.fab_deleteEntry);
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

        public void setLbl_light_description(String description) { this.lbl_light_description.setText(description); }
        public void setX(Double x) { this.posX = x; }
        public void setY(Double y) { this.posY = y; }
        public void setDistance(double distance) { this.distance = distance; }
    }


}

package com.vlcnavigation.ui.livemap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import java.util.List;

import timber.log.Timber;

public class FloorOrderAdapter extends RecyclerView.Adapter<FloorOrderAdapter.StringHolder> {

    private List<Floor> list;
    private RecyclerView recyclerView;
    public FloorOrderAdapter(SettingsViewModel vm, RecyclerView recycler_floors) { this.list = vm.getListOfFloors().getValue(); this.recyclerView = recycler_floors; }

    @NonNull
    @Override
    public StringHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_layout_floor, parent, false);
        return new StringHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StringHolder holder, int position) {
        holder.setText(String.valueOf(list.get(position).getOrder()));
        holder.getTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class StringHolder extends RecyclerView.ViewHolder{
        private final TextView tv;
        public StringHolder(@NonNull View itemView) {
            super(itemView);
            this.tv = itemView.findViewById(R.id.simple_tv);
            Timber.d("%s", tv == null);


        }
        public void setText(String txt) { this.tv.setText(txt); }
        public TextView getTv() { return this.tv; }
    }
}

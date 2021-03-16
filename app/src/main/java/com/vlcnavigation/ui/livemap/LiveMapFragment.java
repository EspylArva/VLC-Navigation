package com.vlcnavigation.ui.livemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.vlcnavigation.R;
import com.vlcnavigation.components.DotIndicatorDecoration;

import timber.log.Timber;


public class LiveMapFragment extends Fragment {

//    private FloatingActionButton fab1, fab2, fab3;

    private LiveMapViewModel liveMapViewModel;
    private RecyclerView recycler_floors, recycler_availableFloors;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveMapViewModel = new ViewModelProvider(this).get(LiveMapViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        refreshUI();

//        try{
//            Trilateration.triangulate();
//        } catch (Exception ex){ Timber.e(ex);}

        return root;
    }

    /**
     *  Sets the position to the highest floor
     */
    private void refreshUI() {
        int position = recycler_floors.getAdapter().getItemCount() -1;
        recycler_floors.scrollToPosition(position);
        ((FloorOrderAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(position)).getTv().setBackgroundResource(R.drawable.border);
    }

    private void initListeners() {
        recycler_floors.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                for(int i=0; i<recycler_floors.getAdapter().getItemCount(); i++)
                {
                    FloorOrderAdapter.StringHolder holder = ((FloorOrderAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(i));
                    if(holder != null) {
                        if (i == ((LinearLayoutManager)recycler_floors.getLayoutManager()).findFirstVisibleItemPosition()) {
                            // FIXME : Get a better hint
                            holder.getTv().setBackgroundResource(R.drawable.border);
                        } else { holder.getTv().setBackgroundResource(0); } // reset style
                    }
                }
            }
        });
    }

    private void initObservers() { }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_live_map, container, false);

        recycler_floors = root.findViewById(R.id.recycler_display_floors);
        recycler_availableFloors = root.findViewById(R.id.recycler_available_floors);

        setRecyclerDisplayFloors();
        setRecyclerAvailableFloors();

        return root;
    }

    private void setRecyclerDisplayFloors() {
        recycler_floors.setHasFixedSize(true);
        //  Values
        FloorDisplayAdapter floorAdapter = new FloorDisplayAdapter(liveMapViewModel);
        recycler_floors.setAdapter(floorAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_layout.setReverseLayout(true);
        recycler_floors.setLayoutManager(recycler_layout);
        // Snapping on a viewholder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_floors);
    }

    private void setRecyclerAvailableFloors() {
        recycler_availableFloors.setHasFixedSize(true);
        //  Values
        FloorOrderAdapter floorAdapter = new FloorOrderAdapter(liveMapViewModel, recycler_floors);
        recycler_availableFloors.setAdapter(floorAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_layout.setReverseLayout(true);
        recycler_availableFloors.setLayoutManager(recycler_layout);
        // Snapping on a viewholder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_availableFloors);
    }
}
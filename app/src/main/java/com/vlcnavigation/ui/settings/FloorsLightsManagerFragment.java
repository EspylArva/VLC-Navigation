package com.vlcnavigation.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.vlcnavigation.R;
import com.vlcnavigation.components.DotIndicatorDecoration;
import com.vlcnavigation.module.svg2vector.SvgFetcher;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class FloorsLightsManagerFragment extends Fragment {

    SettingsViewModel settingsViewModel;
    private RecyclerView recycler_lights; // Carousel with lights
    private RecyclerView recycler_floors; // Carousel with floors

    public RecyclerView getLightRecycler() { return this.recycler_lights; }
    public RecyclerView getFloorRecycler() { return this.recycler_floors; }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(getParentFragment() != null) { settingsViewModel = new ViewModelProvider(getParentFragment()).get(SettingsViewModel.class); }
        else { settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class); }
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        return root;
    }

    private void initListeners() {
    }

    private void initObservers() {
    }

    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.tabitem_floors_and_lights, container, false);

        setRecyclerLights(root);
        setRecyclerFloors(root);
        return root;
    }

    private void setRecyclerFloors(View root) {
        recycler_floors = root.findViewById(R.id.recycler_floors);
        recycler_floors.setHasFixedSize(true);
        //  Values
        // FIXME
        FloorAdapter floorAdapter = new FloorAdapter(settingsViewModel, this); //settingsViewModel.getListOfFloors().getValue(), this);
//        floorAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeChanged(int positionStart, int itemCount) {
//                super.onItemRangeChanged(positionStart, itemCount);
//                Timber.d("onItemRangeChanged (floors). PositionStart: %s/%s", positionStart, itemCount);
//                for(int i = 0; i<itemCount; i++)
//                {
//                    Timber.d("Updating menu for index %s", i);
////                    ((LightAdapter.LightHolder)recycler_lights.findViewHolderForAdapterPosition(i)).initFloorMenuListener();
//                }
//
//                // AddFloorFragment
//            }
//        });

        recycler_floors.setAdapter(floorAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_floors.setLayoutManager(recycler_layout);
        // Margin between items
        // recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        // Item position
        recycler_floors.addItemDecoration(new DotIndicatorDecoration());
        // Snapping on a viewholder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_floors);
    }

    private void setRecyclerLights(View root) {
        recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
        //  Values
        LightAdapter lightAdapter = new LightAdapter(settingsViewModel); // settingsViewModel.getListOfLights().getValue(), settingsViewModel.getListOfFloors().getValue());
//        lightAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                Timber.d("onItemRangeInserted (lights). PositionStart: %s/%s", positionStart, itemCount);
//            }
//
//            @Override
//            public void onItemRangeRemoved(int positionStart, int itemCount) {
//                super.onItemRangeRemoved(positionStart, itemCount);
//                Timber.d("onItemRangeRemoved (lights). PositionStart: %s/%s", positionStart, itemCount);
//            }
//        });
        recycler_lights.setAdapter(lightAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_lights.setLayoutManager(recycler_layout);
        // Margin between items
        // recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        // Item position
        recycler_lights.addItemDecoration(new DotIndicatorDecoration());
        // Snapping on a viewholder // TODO: Maybe a stronger snap. See SnapHelperBuilder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_lights);
    }

    /**
     * Handle startActivityForResult from Adapter (RecyclerView) "Floor"
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            Timber.d("Activity Result caught. Request code: %s. Result code: %s", requestCode, resultCode);
            String filePath = data.getData().toString();

            if (requestCode == SvgFetcher.READ_SVG_REQUEST_CODE) {
                Timber.d(data.getData().normalizeScheme().getPath());

                int position = ((LinearLayoutManager) recycler_floors.getLayoutManager()).findFirstVisibleItemPosition();
                recycler_floors.scrollToPosition(position);
                FloorAdapter.FloorHolder holder = (FloorAdapter.FloorHolder) recycler_floors.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    settingsViewModel.getListOfFloors().getValue().get(position).setFilePath(filePath);
                    holder.refreshUI();
                }
            }
        } else { Timber.e("Could not find file"); }
    }

}

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

    /**
     * Initialises user input listeners: touch, click, drag...
     */
    private void initListeners() {
    }

    /**
     * Observe LiveData from the ViewModel.
     */
    private void initObservers() {
    }

    /**
     * Binds views to the XML layout
     */
    private View initViews(LayoutInflater inflater, ViewGroup container)
    {
        View root = inflater.inflate(R.layout.tabitem_floors_and_lights, container, false);

        setRecyclerLights(root);
        setRecyclerFloors(root);
        return root;
    }

    /**
     * Setting the recycler view used to display registered floors.
     * The settings are set to make the recycler view look like a carousel:
     * - views snap to display one ViewHolder
     * - indicator shows which view is currently displayed, and the number of items
     * - views are displayed horizontally
     */
    private void setRecyclerFloors(View root) {
        recycler_floors = root.findViewById(R.id.recycler_floors);
        recycler_floors.setHasFixedSize(true);
        //  Values
        FloorAdapter floorAdapter = new FloorAdapter(settingsViewModel, this);
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

    /**
     * Refresh data in the floor adapter
     */
    public void refreshFloorAdapter()
    {
        FloorAdapter floorAdapter = new FloorAdapter(settingsViewModel, this);
        recycler_floors.setAdapter(floorAdapter);
    }

    /**
     * Setting the recycler view used to display registered lights.
     * The settings are set to make the recycler view look like a carousel:
     * - views snap to display one ViewHolder
     * - indicator shows which view is currently displayed, and the number of items
     * - views are displayed horizontally
     */
    private void setRecyclerLights(View root) {
        recycler_lights = root.findViewById(R.id.recycler_lights);
        recycler_lights.setHasFixedSize(true);
        //  Values
        LightAdapter lightAdapter = new LightAdapter(settingsViewModel);
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
     * Refresh data in the light adapter
     */
    public void refreshLightAdapter()
    {
        LightAdapter lightAdapter = new LightAdapter(settingsViewModel);
        recycler_lights.setAdapter(lightAdapter);
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

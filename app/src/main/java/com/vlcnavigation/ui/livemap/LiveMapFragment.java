package com.vlcnavigation.ui.livemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.ui.settings.FloorAdapter;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import timber.log.Timber;


public class LiveMapFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private RecyclerView recycler_floors, recycler_availableFloors;
    private TextInputLayout lbl_floor_title;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // liveMapViewModel = new ViewModelProvider(this).get(LiveMapViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
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
     * FIXME
     */
    private void refreshUI() {
        // Sets the FloorPicker hint
        int position = recycler_floors.getAdapter().getItemCount() -1;

        recycler_floors.scrollToPosition(position); // FIXME: Should scroll to the position closest to 0 (RDC/Floor)
//        ((FloorHintAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(position)).getTv().setBackgroundResource(R.drawable.ic_item_highlighted);

        // Display lights. According to documentation, the color should be purple.
        displayLights();
        // Display users. According to documentation, the color should be orange.
        displayUsers();
    }

    /**
     * Display lights as a purple circle on the map. Lights are registered in the SettingsViewModel.
     * Lights' position should be refreshed on light edit and on floor selection change.
     */
    private void displayLights() {
        // Use color @color/purple_500
        int colorId = R.color.purple_500;
    }

    /**
     * Display users as an orange circle on the map. Lights are registered in the SettingsViewModel.
     * Users' position should be displayed only if they are on the selected floor, and should be refreshed once every second.
     */
    private void displayUsers() {
        int colorId = R.color.orange_500;
    }


    private void initListeners() {
        recycler_floors.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                for(int i=0; i<recycler_floors.getAdapter().getItemCount(); i++)
                {
                    FloorHintAdapter.StringHolder holder = ((FloorHintAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(i));
                    if(holder != null) {
                        if (i == ((LinearLayoutManager)recycler_floors.getLayoutManager()).findFirstVisibleItemPosition()) {
                            holder.getTv().setBackgroundResource(R.drawable.ic_item_highlighted);
                            setFloorDescription(settingsViewModel.getListOfFloors().getValue().get(i).getDescription());

                        } else { holder.getTv().setBackgroundResource(R.drawable.ic_circle); } // reset style
                    }
                    else
                    {
                        Timber.e("Holder is null");
                    }
                }
            }
        });

    }

    /**
     * Observe LiveData from the ViewModel.
     */
    private void initObservers() { }

    /**
     * FIXME
     * @param inflater
     * @param container
     * @return
     */
    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_live_map, container, false);

        recycler_floors = root.findViewById(R.id.recycler_display_floors);
        recycler_availableFloors = root.findViewById(R.id.recycler_available_floors);
        lbl_floor_title = root.findViewById(R.id.lbl_floor_title);

        setRecyclerDisplayFloors();
        setRecyclerAvailableFloors();

        return root;
    }


    /**
     * Settings for the indoor map.
     * Every ViewHolder is associated to a storey level.
     * Other parameters include the orientation of the recycler view and behaviours related to a carousel
     */
    private void setRecyclerDisplayFloors() {
        recycler_floors.setHasFixedSize(true);
        //  Values
        FloorDisplayAdapter floorAdapter = new FloorDisplayAdapter(settingsViewModel, this);
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

    /**
     * FIXME
     */
    private void setRecyclerAvailableFloors() {
        // FIXME: rework with settingsViewModel.getFloorLevels().getValue()
        recycler_availableFloors.setHasFixedSize(true);
        //  Values
        FloorHintAdapter floorAdapter = new FloorHintAdapter(settingsViewModel, recycler_floors);
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

    public void setFloorDescription(String description) {
        this.lbl_floor_title.getEditText().setText(description);
    }
}
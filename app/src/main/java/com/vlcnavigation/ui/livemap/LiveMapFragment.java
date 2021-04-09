package com.vlcnavigation.ui.livemap;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.module.trilateration.Light;
import com.vlcnavigation.module.utils.Util;
import com.vlcnavigation.ui.settings.FloorAdapter;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

//TODO: Javadoc
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


    private void refreshUI() {
        // Sets the FloorPicker hint
        int position = recycler_floors.getAdapter().getItemCount() -1;

        recycler_floors.scrollToPosition(position); // FIXME: Should scroll to the position closest to 0 (RDC/Floor)
//        ((FloorHintAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(position)).getTv().setBackgroundResource(R.drawable.ic_item_highlighted);

        // Display lights. According to documentation, the color should be purple.
//        displayLights();
        // Display users. According to documentation, the color should be orange.
        displayUsers();
    }

    /**
     * Display lights as a purple circle on the map. Lights are registered in the SettingsViewModel.
     * Lights' position should be refreshed on light edit and on floor selection change.
     */
    private void displayLights(int i) {
        // Use color @color/purple_500
        int colorId = R.color.purple_500;
        // To display a marker at position X,Y, we need to calculate the density of the screen

        float defaultMargin = getResources().getDimension(R.dimen.default_margin);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthOfMapDp = displayMetrics.widthPixels / displayMetrics.density - 2*defaultMargin;
        float heightOfMapDp = displayMetrics.heightPixels / displayMetrics.density; // FIXME: height should take into account available space, as well as bottom navigation bar

        Light l = settingsViewModel.getListOfLights().getValue().get(0);

//        Uri uri = Uri.parse(filePath);
//        Timber.d(uri.getPath());

        int widthOfMapPx = 0;
        int heightOfMapPx = 0;

        try {
            InputStream is = requireContext().getContentResolver().openInputStream(Uri.parse(l.getFloor().getFilePath()));
            Pair<Integer, Integer> mapSizePx = SvgSplitter.getMapSize(is);
            widthOfMapPx = mapSizePx.first;
            heightOfMapPx = mapSizePx.second;


            FloorDisplayAdapter.FloorDisplayHolder holder = ((FloorDisplayAdapter.FloorDisplayHolder)recycler_floors.findViewHolderForAdapterPosition(i));
            if(holder != null) {
                holder.makeMarker(mapSizePx, colorId);
            } else { Timber.d("Could not create marker. Holder is null"); }


            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Position of X = defaultMargin + (widthOfMapDp / widthOfMapPx) * posXPx
    }

    /**
     * Display users as an orange circle on the map. Lights are registered in the SettingsViewModel.
     * Users' position should be displayed only if they are on the selected floor, and should be refreshed once every second.
     */
    private void displayUsers() {
        int colorId = R.color.orange_500;
    }


    /**
     * Initialises user input listeners: touch, click, drag...
     */
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

                            displayLights(i);

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
     * Binds views to the XML layout
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
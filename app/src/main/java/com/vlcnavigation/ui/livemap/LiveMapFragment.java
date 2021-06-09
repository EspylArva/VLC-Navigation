package com.vlcnavigation.ui.livemap;

import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;
import com.vlcnavigation.module.utils.Util;
import com.vlcnavigation.ui.fft.FFTComputing;
import com.vlcnavigation.ui.fft.FFTFragment;
import com.vlcnavigation.ui.settings.FloorAdapter;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import timber.log.Timber;

//TODO: Javadoc
public class LiveMapFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private RecyclerView recycler_floors, recycler_availableFloors;
    private TextInputLayout lbl_floorTitle;
    private MaterialAutoCompleteTextView txt_roomSearchField;

    private final Handler handler = new Handler();
    private final int USER_POSITION_REFRESH_RATE = 500;
    private UserPositionAcquisition thread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // liveMapViewModel = new ViewModelProvider(this).get(LiveMapViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

//        recycler_floors.smoothScrollToPosition(1);

//        refreshUI();
        displayUsers();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(thread);

    }

    private void refreshUI() {
        // Sets the FloorPicker hint
        int position = recycler_floors.getAdapter().getItemCount() -1;
        recycler_floors.smoothScrollToPosition(position);
        position = settingsViewModel.findZeroFloor();
        recycler_floors.smoothScrollToPosition(position);
        Timber.d("%s", position);
    }

    /**
     * Display users as an orange circle on the map. Lights are registered in the SettingsViewModel.
     * Users' position should be displayed only if they are on the selected floor, and should be refreshed once every second.
     */
    private void displayUsers() {
        thread = new UserPositionAcquisition();
        handler.postDelayed(thread, USER_POSITION_REFRESH_RATE);
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
//                    Timber.e("Focused child: %s", recycler_availableFloors.findViewHolderForLayoutPosition().);

                    FloorHintAdapter.StringHolder holder = ((FloorHintAdapter.StringHolder)recycler_availableFloors.findViewHolderForAdapterPosition(i));
                    GradientDrawable whiteCircle = (GradientDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_circle, requireContext().getTheme());
                    if(holder != null) {
                        if (i == ((LinearLayoutManager) recycler_floors.getLayoutManager()).findFirstVisibleItemPosition()) {
                            // ?attr/colorPrimary
                            whiteCircle.setColor(Util.getAttrColor(getContext(), R.attr.colorPrimary));
                            holder.getTv().setBackground(whiteCircle);
                            setFloorDescription(settingsViewModel.getListOfFloors().getValue().get(i).getDescription());
//                            displayLights(i);
                        } else {
                            whiteCircle.setColor(Util.getAttrColor(getContext(), R.attr.colorSecondary));
                            holder.getTv().setBackgroundResource(R.drawable.ic_circle);
                        } // reset style
                    }
                }
            }
        });
        txt_roomSearchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Timber.d("Searching for: %s", s);
                try {
                    Floor f = settingsViewModel.findRoom(s.toString());
                    if(f != null) {
                        String log = String.format("Floor: %s (%s)", f.getDescription(), f.getOrder());
                        Timber.d(log);
                        Util.hideKeyboardFromView(getView());
                        txt_roomSearchField.dismissDropDown();
                        Snackbar.make(getContext(), getView(), log, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else { Timber.d("Room not found"); }
                } catch (IOException e) { Timber.e(e); }
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
        lbl_floorTitle = root.findViewById(R.id.lbl_floor_title);
        txt_roomSearchField = root.findViewById(R.id.txtInputLayout_roomSearchField);


        setRecyclerDisplayFloors();
        setRecyclerAvailableFloors();

//        recycler_floors.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        Timber.e("(STARTUP) Width: %s -- Height: %s", recycler_floors.getMeasuredWidth(), recycler_floors.getMeasuredHeight());

        try {
            List<String> rooms = settingsViewModel.getListOfRooms();
            ArrayAdapter<String> autocompletionAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, rooms);
            txt_roomSearchField.setAdapter(autocompletionAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        this.lbl_floorTitle.getEditText().setText(description);
    }

    private class UserPositionAcquisition implements Runnable
    {
        public void run() {
            // get the frequency
            double frequency = 210;
            if (MainActivity.fftBoolCompute){
                frequency = FFTFragment.firstFreqAverageValue;

            }

            Light closestLight = Light.getLightFromFrequency(frequency, frequency*0.2, settingsViewModel.getListOfLights().getValue());
            // display marker on the map


//            for(int i=0; i<recycler_floors.getAdapter().getItemCount(); i++) {
//                FloorDisplayAdapter.FloorDisplayHolder holder = ((FloorDisplayAdapter.FloorDisplayHolder) recycler_floors.findViewHolderForAdapterPosition(i));
            try {

                FloorDisplayAdapter.FloorDisplayHolder holder = (FloorDisplayAdapter.FloorDisplayHolder) recycler_floors.findContainingViewHolder( recycler_floors.getLayoutManager().getChildAt(0) );
                if (holder != null) {
                    if(closestLight == null){
//                        Random r = new Random();
//                        holder.moveMarker(holder.getMarker(), r.nextInt(500), r.nextInt(500), 100);
                        holder.moveMarker(holder.getMarker(), 0, 0, 100);
                    }
                    else if (holder.getFloor().getOrder() == closestLight.getFloor().getOrder()){
//                        holder.get
                        holder.moveMarker(holder.getMarker(), closestLight.getPosX(), closestLight.getPosY(), 100);
                        //display light name in FFT debug section
                        MainActivity.fftComputing.setCurrentLED(closestLight.getDescription());
                    }
                }
//            }
                handler.postDelayed(this, USER_POSITION_REFRESH_RATE); // recursive call
            }
            catch(NullPointerException e) { Timber.e(e); }

        }
    }
}
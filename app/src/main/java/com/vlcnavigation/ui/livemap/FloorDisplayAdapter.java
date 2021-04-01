package com.vlcnavigation.ui.livemap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.pixplicity.sharp.Sharp;
import com.vlcnavigation.R;
import com.vlcnavigation.module.jsonfilereader.JsonFileReader;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import timber.log.Timber;

public class FloorDisplayAdapter extends RecyclerView.Adapter<FloorDisplayAdapter.FloorDisplayHolder>{
//    private final LiveMapViewModel vm;
    private final SettingsViewModel vm;
    private final LiveMapFragment fragment;
    public FloorDisplayAdapter(SettingsViewModel vm, LiveMapFragment fragment ) { this.vm = vm; this.fragment = fragment; }

    @NonNull
    @Override
    public FloorDisplayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_floor_display, parent, false);
        return new FloorDisplayHolder(v, vm, fragment);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorDisplayHolder holder, int position) {
        holder.refreshUI();
    }

    @Override
    public int getItemCount() {
        return vm.getListOfFloors().getValue().size();
    }

    public class FloorDisplayHolder extends RecyclerView.ViewHolder {
        private final SettingsViewModel vm;
        private final LiveMapFragment fragment;
        // Views
        private ConstraintLayout container_map;
        public ConstraintLayout getContainer() { return this.container_map; }

        public FloorDisplayHolder(@NonNull View itemView, SettingsViewModel vm, LiveMapFragment fragment) {
            super(itemView);
            this.vm = vm;
            this.fragment = fragment;

            initViews(itemView);        // Instantiate views
            initOnClickListeners();     // Click listeners. Currently: 0
//            initTextChangeListener();   // When a text field is changed, we want to save it to SP
        }

//        private void initTextChangeListener() {
//        }

        private void initOnClickListeners() {
        }

        private void initViews(View itemView) {
            container_map = itemView.findViewById(R.id.container_map);

        }

        public void refreshUI()
        {
            String filePath = vm.getListOfFloors().getValue().get(getAdapterPosition()).getFilePath();
            Timber.d("Displaying the map for floor %s", vm.getListOfFloors().getValue().get(getAdapterPosition()).toString());

            if(filePath != null && !filePath.isEmpty())
            {
                try{
                    // Get the InputStream for the file
                    Uri uri = Uri.parse(filePath); Timber.d(uri.getPath());
                    InputStream is =  itemView.getContext().getContentResolver().openInputStream(Uri.parse(filePath));
                    Map<String, String> svgs = SvgSplitter.parse(is);
                    if(svgs != null && svgs.size() > 0) { svgs.entrySet().forEach(this::makeMap); }
                    is.close();
                }
                catch (IOException e1) { Timber.e(e1);}
                catch (SecurityException e2) { Timber.e(e2.getLocalizedMessage()); }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private void makeMap(Map.Entry<String, String> entry) {
            ImageView mapPart = new ImageView(itemView.getContext());
            mapPart.setId(View.generateViewId());

            mapPart.setDrawingCacheEnabled(true);
            mapPart.setClickable(false);
            Timber.d("%s", mapPart.isClickable());



            Timber.d("Id: %s", mapPart.getId());

            mapPart.setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            Drawable drawable = null;
            try {
                Timber.d("SVG: %s", entry.getValue());
                    InputStream is = new ByteArrayInputStream(entry.getValue().getBytes());
                    drawable = Sharp.loadInputStream(is).getDrawable();
                    is.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }

            mapPart.setImageDrawable(drawable);
            mapPart.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {  // FIXME: view is clickable despite line 129 ( mapPart.setClickable(false); )
                            if(event.getAction() == MotionEvent.ACTION_DOWN)
                            {
                                Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
                                int color = bmp.getPixel((int) event.getX(), (int) event.getY());
                                if (color != Color.TRANSPARENT) {

                                    Timber.d("Not transparent for %s (color: %s)", mapPart.getId(), color);
//                                    Snackbar.make(itemView.getContext(), view, String.format("Clicked on view #%s. Color: %s", mapPart.getId(), Color.valueOf(color)), BaseTransientBottomBar.LENGTH_SHORT).show();
                                    Snackbar.make(itemView.getContext(), view, entry.getKey(), BaseTransientBottomBar.LENGTH_SHORT).show();
                                    return true;
                                }
                                else
                                {
                                    return false;
                                }

                            } return false;
                        }
                    });
            container_map.addView(mapPart);
        }
    }
}

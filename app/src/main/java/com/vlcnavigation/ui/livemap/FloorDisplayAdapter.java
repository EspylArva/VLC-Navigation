package com.vlcnavigation.ui.livemap;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.pixplicity.sharp.Sharp;
import com.vlcnavigation.R;
import com.vlcnavigation.module.jsonfilereader.JsonFileReader;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.module.trilateration.Light;
import com.vlcnavigation.module.utils.Util;
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

//TODO: Javadoc
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
//        Timber.e("Making lights!");
//        holder.makeLights();
//        Timber.e("Finished making lights!");

        final ViewTreeObserver observer = holder.container_map.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.container_map.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                int containerWidth = holder.container_map.getWidth();
                int containerHeight = holder.container_map.getHeight();
                Timber.e("Width: %s - Height: %s", containerWidth, containerHeight);
                holder.makeLights(containerWidth, containerHeight);
            }
        });

    }

    @Override
    public int getItemCount() {
        return vm.getListOfFloors().getValue().size();
    }

    public class FloorDisplayHolder extends RecyclerView.ViewHolder {
        private final SettingsViewModel vm;
        private final LiveMapFragment fragment;
        // Views
        private RelativeLayout container_map;

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

        public void refreshUI() {
            String filePath = vm.getListOfFloors().getValue().get(getAdapterPosition()).getFilePath();
            Timber.d("Displaying the map for floor %s", vm.getListOfFloors().getValue().get(getAdapterPosition()).toString());

            if (filePath != null && !filePath.isEmpty()) {
                try {
                    // Get the InputStream for the file
                    Uri uri = Uri.parse(filePath);
                    Timber.d(uri.getPath());
                    InputStream is = itemView.getContext().getContentResolver().openInputStream(uri);
                    Map<String, String> svgs = SvgSplitter.parse(is);
                    if (svgs != null && svgs.size() > 0) {
                        svgs.entrySet().forEach(this::makeMap);
                    }
                    is.close();
                } catch (IOException e1) {
                    Timber.e(e1);
                } catch (SecurityException e2) {
                    Timber.e(e2.getLocalizedMessage());
                }
            }

//            makeLights();
        }

        @SuppressLint("ClickableViewAccessibility")
        private void makeMap(Map.Entry<String, String> entry) {
            ImageView mapPart = new ImageView(itemView.getContext());
            mapPart.setId(View.generateViewId());

            mapPart.setDrawingCacheEnabled(true);
            mapPart.setClickable(false);
            mapPart.setScaleType(ImageView.ScaleType.FIT_START);

            mapPart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            Drawable drawable = null;
            try {
                InputStream is = new ByteArrayInputStream(entry.getValue().getBytes());
                drawable = Sharp.loadInputStream(is).getDrawable();
                is.close();
            } catch (IOException e) {
                Timber.e("SVG: %s", entry.getValue());
            }

            mapPart.setImageDrawable(drawable);
            mapPart.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_DOWN)
                            {
                                Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
                                int color = bmp.getPixel((int) event.getX(), (int) event.getY());
                                if (color != Color.TRANSPARENT) {
                                    Timber.d("Not transparent for %s (color: %s)", mapPart.getId(), color);
                                    Snackbar.make(itemView.getContext(), view, entry.getKey(), BaseTransientBottomBar.LENGTH_SHORT).show();
                                    return true;
                                }
                                else { return false; }
                            } return false;
                        }
                    });
            container_map.addView(mapPart);
        }

        public void makeMarker(double posX, double posY, int width, int height, @ColorInt int color, int... markerSize) throws IOException {

            double dotSize = markerSize.length == 1 ? markerSize[0] : 100;
            String filePath = vm.getListOfFloors().getValue().get(getAdapterPosition()).getFilePath();
            InputStream is = itemView.getContext().getContentResolver().openInputStream(Uri.parse(filePath));
            Pair<Integer, Integer> mapSizePx = SvgSplitter.getMapSize(is);
            double density = ((double)width)/((double)mapSizePx.first);
            double leftMargin = (posX*density) - (dotSize/2);
            double topMargin = (posY*density) - (dotSize/2);
            is.close();


            Timber.d("Requesting a marker of size: %sx%s at position: %s:%s", dotSize, dotSize, posX, posY);
            Timber.d("%sx%s", width, height);                                       // 1008x794
//            Timber.d("Layout size (in px): %sx%s", container_map.getWidth(), container_map.getHeight());        // 1008x794
            Timber.d("Image size (in px): %sx%s", mapSizePx.first, mapSizePx.second);                           // 522x202
            Timber.d("Density: %s | Reworked positions: %s:%s", density, leftMargin, topMargin);


            // Making the marker
            ImageView marker = new ImageView(itemView.getContext());
            marker.setId(View.generateViewId());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);//LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); // 100, 100);
            params.leftMargin = (int) leftMargin;
            params.topMargin = (int) topMargin;

            GradientDrawable whiteCircle = (GradientDrawable)ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.ic_circle, itemView.getContext().getTheme());
            whiteCircle.setColor(ColorStateList.valueOf(color));
            marker.setBackground(whiteCircle);

            container_map.addView(marker, params);
        }

        private void makeLights(int width, int height)
        {
            int colorId = R.color.purple_500;
            int color = Util.modifyAlpha(ContextCompat.getColor(itemView.getContext(), colorId), 128);

            for(Light l : vm.getListOfLights().getValue())
            {
                if(l.isOnFloor(vm.getListOfFloors().getValue().get(getAdapterPosition())))
                {
                    try {
                        makeMarker(l.getPosX(), l.getPosY(), width, height, color, 100);
                    } catch (IOException e) { Timber.e(e); }
                }
            }
        }

    }
}

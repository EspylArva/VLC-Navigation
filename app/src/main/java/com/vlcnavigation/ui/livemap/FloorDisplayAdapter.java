package com.vlcnavigation.ui.livemap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pixplicity.sharp.Sharp;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.ui.settings.SettingsViewModel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import timber.log.Timber;

public class FloorDisplayAdapter extends RecyclerView.Adapter<FloorDisplayAdapter.FloorDisplayHolder>{
//    private final LiveMapViewModel vm;
    private final SettingsViewModel vm;
    public FloorDisplayAdapter(SettingsViewModel vm) { this.vm = vm; }

    @NonNull
    @Override
    public FloorDisplayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_floor_display, parent, false);
        return new FloorDisplayHolder(v, vm);
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
        // Views
        private ConstraintLayout container_map;
        public ConstraintLayout getContainer() { return this.container_map; }

        public FloorDisplayHolder(@NonNull View itemView, SettingsViewModel vm) {
            super(itemView);
            this.vm = vm;

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
                    InputStream is =  itemView.getContext().getContentResolver().openInputStream(Uri.parse(filePath));
                    List<String> svgs = SvgSplitter.parse(is);
                    if(svgs != null && svgs.size() > 0) { svgs.forEach(this::makeMap); }
                    is.close();
                } catch(IOException e) { Timber.e(e); }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private void makeMap(String str) {
            ImageView mapPart = new ImageView(itemView.getContext());
            mapPart.setId(View.generateViewId());

            mapPart.setDrawingCacheEnabled(true);
            mapPart.setClickable(false);
            Timber.d("%s", mapPart.isClickable());

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
                                    return true;
                                }
                                else
                                {
                                    return false;
                                }

                            } return false;
                        }
                    });

            Timber.d("Id: %s", mapPart.getId());

            mapPart.setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            Drawable drawable = null;
            if(str.equals("")) {
                drawable = Sharp.loadResource(itemView.getResources(), R.raw.isep_map).getDrawable();
            }
            else {
                try {
                    InputStream is = new ByteArrayInputStream(str.getBytes());
                    drawable = Sharp.loadInputStream(is).getDrawable();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mapPart.setImageDrawable(drawable);

//            mapPart.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Timber.d("Click here");
//                }
//            });
            container_map.addView(mapPart);
        }
    }
}

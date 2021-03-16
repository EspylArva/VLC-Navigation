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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import timber.log.Timber;

public class FloorDisplayAdapter extends RecyclerView.Adapter<FloorDisplayAdapter.FloorDisplayHolder>{
    private final LiveMapViewModel vm;
    public FloorDisplayAdapter(LiveMapViewModel vm) { this.vm = vm; }

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
        private final LiveMapViewModel vm;
        // Views
        private ConstraintLayout container_map;
        public ConstraintLayout getContainer() { return this.container_map; }

        public FloorDisplayHolder(@NonNull View itemView, LiveMapViewModel vm) {
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

        private InputStream getInputStreamFromUri(Uri uri) throws FileNotFoundException {
            return itemView.getContext().getContentResolver().openInputStream(uri);
        }

        public void refreshUI()
        {
            String filePath = vm.getListOfFloors().getValue().get(getAdapterPosition()).getFilePath();
            Timber.d("Displaying the map for floor %s", vm.getListOfFloors().getValue().get(getAdapterPosition()).toString());

            try{
                InputStream svg = getInputStreamFromUri(Uri.parse(filePath));
                List<String> svgs = SvgSplitter.parse(svg);
                if(svgs != null && svgs.size() > 0) {
                    for(String s : svgs) {
                        try{
                            makeMap(s);
                        } catch (IOException e) { Timber.e(e); }
                    }
                }
            } catch(IOException e) { Timber.e(e); }
        }

        @SuppressLint("ClickableViewAccessibility")
        private void makeMap(String str) throws IOException {
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
                                boolean transparent = true;
                                if (color != Color.TRANSPARENT) { transparent = false; }

                                Timber.d("Transparent for %s: %s (color: %s)", mapPart.getId(), transparent, color);
                                Timber.d("%s", view.isClickable());
                                return !transparent;
                            } return false;
                        }
                    });

            Timber.d("Id: %s", mapPart.getId());

            mapPart.setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            Drawable drawable;
            if(str.equals("")) {
                drawable = Sharp.loadResource(itemView.getResources(), R.raw.isep_map).getDrawable();
            }
            else {
                InputStream is = new ByteArrayInputStream(str.getBytes());
                drawable = Sharp.loadInputStream(is).getDrawable();
                is.close();
            }

            mapPart.setImageDrawable(drawable);

            mapPart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            container_map.addView(mapPart);
        }
    }
}

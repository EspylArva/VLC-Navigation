package com.vlcnavigation.ui.livemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vlcnavigation.R;

public class LiveMapFragment extends Fragment {

    private LiveMapViewModel liveMapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveMapViewModel =
                new ViewModelProvider(this).get(LiveMapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_live_map, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        liveMapViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
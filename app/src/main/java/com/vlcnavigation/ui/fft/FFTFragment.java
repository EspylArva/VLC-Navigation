package com.vlcnavigation.ui.fft;

import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.R;
import com.vlcnavigation.module.audiorecord.SignalView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;


public class FFTFragment extends Fragment {

    private FFTViewModel FFTViewModel;
    private SignalView signalView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FFTViewModel =
                new ViewModelProvider(this).get(FFTViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fft, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        FFTViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        signalView = root.findViewById(R.id.signalview);

//        signalView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                display();
//            }
//        });



        return root;
    }

    private void display() {
        final Handler handler = new Handler();
        final int delay = 100;
            handler.postDelayed(new Runnable() {
                public void run() {
                    signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);
                    handler.postDelayed(this, delay);
                }
            }, delay);

//        }
    }

    public SignalView getSignalView() { return this.signalView; }
}
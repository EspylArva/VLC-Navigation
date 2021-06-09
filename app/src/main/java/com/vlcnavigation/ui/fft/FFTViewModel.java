package com.vlcnavigation.ui.fft;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vlcnavigation.module.audiorecord.AudioRecorder;

public class FFTViewModel extends ViewModel {

    int buffersize = AudioRecorder.buffersize;
    public double liveFrequency = 0;
    double[] fftPeaks = new double[buffersize];
    double[] fftFrequencies = new double[buffersize];
    protected String currentLED;

    private MutableLiveData<String> tvLiveFreq;

    public FFTViewModel() {
        tvLiveFreq = new MutableLiveData<>();

    }

    public FFTViewModel(double liveFrequency){
        tvLiveFreq = new MutableLiveData<>();
        if (liveFrequency!=0.0) {
            tvLiveFreq.setValue(String.valueOf(liveFrequency));
        }
        else {
            tvLiveFreq.setValue("Empty");
        }
    }

    public LiveData<String> getText() {
        return tvLiveFreq;
    }
}
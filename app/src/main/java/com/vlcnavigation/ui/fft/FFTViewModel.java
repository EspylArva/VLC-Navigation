package com.vlcnavigation.ui.fft;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FFTViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FFTViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Fast Fourrier Transform fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
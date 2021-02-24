package com.vlcnavigation.ui.livemap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import timber.log.Timber;

public class LiveMapViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LiveMapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void setText(String newText) {
        Timber.d("Text set: %s", newText);
        mText.setValue(newText);
    }
}
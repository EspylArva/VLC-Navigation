package com.vlcnavigation.ui.testing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TestingViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;

    public TestingViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() { return this.mText; }
    public void setText(String txt) { this.mText.setValue(txt); }
}

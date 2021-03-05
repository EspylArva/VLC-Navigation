package com.vlcnavigation.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.List;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<Light>> mListOfLights;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mListOfLights = new MutableLiveData<>();


        mText.setValue("This is notifications fragment");
        mListOfLights.setValue(new ArrayList<Light>());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void addLight(Light newLight) { mListOfLights.getValue().add(newLight); }
    public LiveData<List<Light>> getListOfLights() { return mListOfLights; }
}
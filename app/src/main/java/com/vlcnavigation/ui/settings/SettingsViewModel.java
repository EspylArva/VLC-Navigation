package com.vlcnavigation.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

public class SettingsViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<Light>> mListOfLights;
    private SharedPreferences preferences;
    private Resources resources;

    public SettingsViewModel(@NonNull Application app) {
        super(app);
        mText = new MutableLiveData<>();
        mListOfLights = new MutableLiveData<>();


        mText.setValue("This is notifications fragment");
        mListOfLights.setValue(new ArrayList<Light>());

        resources = getApplication().getResources();
        preferences = getApplication().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);


        if(!preferences.getString(resources.getString(R.string.sp_lights), "").equals(""))
        {
            String lights = preferences.getString(resources.getString(R.string.sp_lights), "");
            Timber.d("SP: %s", lights);
            Type TYPE_LIST_OF_LIGHT = new TypeToken<ArrayList<Light>>() {}.getType();
            List<Light> savedLights = new Gson().fromJson(lights, TYPE_LIST_OF_LIGHT);
            if (savedLights != null && savedLights.size() > 0) {
                for (Light light : savedLights) {
                    Timber.d("This light was saved: %s", light.toString());
                    addLight(light);
                }
            }
        }
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void addLight(Light newLight) {
        mListOfLights.getValue().add(newLight);
        String json = new Gson().toJson(mListOfLights.getValue());
        preferences.edit().putString(resources.getString(R.string.sp_lights), json).apply();
    }
    public LiveData<List<Light>> getListOfLights() { return mListOfLights; }
}
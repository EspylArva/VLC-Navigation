package com.vlcnavigation.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import java.lang.reflect.Type;
import java.util.SortedSet;

import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<Light>> mListOfLights;
    private final MutableLiveData<SortedSet<Floor>> mListOfFloors;
    private final SharedPreferences preferences;
    private final Resources resources;

    public SettingsViewModel(@NonNull Application app) {
        super(app);
        mText = new MutableLiveData<>();
        mListOfLights = new MutableLiveData<>();
        mListOfFloors = new MutableLiveData<>();





        mText.setValue("This is notifications fragment");
        mListOfLights.setValue(new ArrayList<Light>());
//        mListOfFloors.setValue(new SortedSet<Floor>());

//        mListOfFloors.getValue().

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

        if(!preferences.getString(resources.getString(R.string.sp_map), "").equals(""))
        {
            String floors = preferences.getString(resources.getString(R.string.sp_map), "");
            Timber.d("SP: %s", floors);
            Type TYPE_LIST_OF_LIGHT = new TypeToken<ArrayList<Floor>>() {}.getType();
            List<Floor> savedFloors = new Gson().fromJson(floors, TYPE_LIST_OF_LIGHT);
            if (savedFloors != null && savedFloors.size() > 0) {
                for (Floor floor : savedFloors) {
                    Timber.d("This light was saved: %s", floor.toString());
                    addFloor(floor);
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

    public void addFloor(Floor floor){
        mListOfFloors.getValue().add(floor);
        String json = new Gson().toJson(mListOfFloors.getValue());
        preferences.edit().putString(resources.getString(R.string.sp_map), json).apply();
    }

    public LiveData<List<Light>> getListOfLights() { return mListOfLights; }
    public LiveData<List<Floor>> getListOfFloors() { return mListOfFloors; }
}
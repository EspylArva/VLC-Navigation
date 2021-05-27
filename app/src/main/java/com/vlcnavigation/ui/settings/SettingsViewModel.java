package com.vlcnavigation.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.jsonfilereader.JsonFileReader;
import com.vlcnavigation.module.svg2vector.SvgSplitter;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

//TODO: Javadoc
public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Light>> mListOfLights;
    private final MutableLiveData<List<Floor>> mListOfFloors;

    private final MutableLiveData<List<Integer>> mListOfFloorLevels;

    private final SharedPreferences preferences;
    private final Resources resources;

    private Map<Floor, List<String>> mapRooms;

    public SettingsViewModel(@NonNull Application app) {
        super(app);
        mListOfLights = new MutableLiveData<>();
        mListOfFloors = new MutableLiveData<>();
        mListOfFloorLevels = new MutableLiveData<>();

        mapRooms = new HashMap<Floor, List<String>>();

        mListOfLights.setValue(new ArrayList<Light>());
        mListOfFloors.setValue(new ArrayList<Floor>());
        mListOfFloorLevels.setValue(new ArrayList<Integer>());

        resources = getApplication().getResources();
        preferences = getApplication().getSharedPreferences(resources.getString(R.string.sp_base), Context.MODE_PRIVATE);

        importLightsFromSp();
        importFloorsFromSp();

        for(Floor f : mListOfFloors.getValue())
        {
            try
            {
                Uri uri = Uri.parse(f.getFilePath());
                InputStream is = getApplication().getContentResolver().openInputStream(uri);
                Map<String, String> svgs = SvgSplitter.parse(is);
                is.close();
                if (svgs != null && svgs.size() > 0) {
                    mapRooms.put(f, new ArrayList<>(svgs.keySet()));
                }
            }catch(IOException | SecurityException e){ Timber.e(e); }
        }


    }

    protected void importFloorsFromSp() {
        if(!preferences.getString(resources.getString(R.string.sp_floors), "").equals(""))
        {
            String sharedPrefsFloors = preferences.getString(resources.getString(R.string.sp_floors), "");
            Timber.d("%s: %s", resources.getString(R.string.sp_floors), sharedPrefsFloors);
            List<Floor> savedFloors = JsonFileReader.getFloorsFromJson(sharedPrefsFloors);
            if (savedFloors != null && savedFloors.size() > 0) {
                savedFloors.forEach(this::addFloor);
            }
        }
    }

    protected void importLightsFromSp() {
        if(!preferences.getString(resources.getString(R.string.sp_lights), "").equals(""))
        {
            String sharedPrefsLights = preferences.getString(resources.getString(R.string.sp_lights), "");
            Timber.d("%s: %s", resources.getString(R.string.sp_lights), sharedPrefsLights);
            List<Light> savedLights = JsonFileReader.getLightsFromJson(sharedPrefsLights);
            if (savedLights != null && savedLights.size() > 0) {
                savedLights.forEach(this::addLight);
            }
        }
    }


    protected void addLight(Light newLight) {
        mListOfLights.getValue().add(newLight);
        saveLights();
    }

    protected void addFloor(Floor floor) {
        if(!mListOfFloors.getValue().stream().anyMatch(f -> f.getOrder() == floor.getOrder()))
        {
            mListOfFloors.getValue().add(floor);
            mListOfFloors.getValue().sort(Floor::compareTo);
            saveFloors();

            mListOfFloorLevels.getValue().add(floor.getOrder());
            mListOfFloorLevels.getValue().sort(Integer::compareTo);
        }
    }

    public LiveData<List<Light>> getListOfLights() { return mListOfLights; }
    public LiveData<List<Floor>> getListOfFloors() { return mListOfFloors; }

    protected void removeFloorAt(int position) {
        mListOfFloors.getValue().remove(position);
        mListOfFloorLevels.getValue().remove(position);
        saveFloors();
    }

    protected void removeLightAt(int position) {
        mListOfLights.getValue().remove(position);
        saveLights();
    }

    protected void saveLights()
    {
        String json = new Gson().toJson(mListOfLights.getValue());
        preferences.edit().putString(resources.getString(R.string.sp_lights), json).apply();
    }

    protected void saveFloors() {
        String json = new Gson().toJson(mListOfFloors.getValue());
        preferences.edit().putString(resources.getString(R.string.sp_floors), json).apply();
    }

    protected Floor findFloor(int index) {
        if(mListOfFloors.getValue().stream().anyMatch(floor -> floor.getOrder() == index)) {
            return mListOfFloors.getValue().stream().filter(floor -> floor.getOrder() == index).findFirst().get();
        }
        else { Timber.e("CANT FIND FLOOR"); return null; }
    }

    public int findZeroFloor()
    {
        if(mListOfFloors.getValue().isEmpty() || mListOfFloors.getValue() == null) {
            return 0;
        }
        else {
            int current = Integer.MAX_VALUE;
            for(int i = 0; i<mListOfFloors.getValue().size(); i++)
            {
                if(Math.abs(mListOfFloors.getValue().get(i).getOrder()) < current) {
                    current = mListOfFloors.getValue().get(i).getOrder();
                }
            }
            return current;
        }
    }

    public boolean floorExists(Floor floor) {
        return mListOfFloors.getValue().stream().anyMatch(f -> f.getOrder() == floor.getOrder());
    }

    public Floor findRoom(String roomName) throws IOException {
        for(Map.Entry<Floor, List<String>> entry : mapRooms.entrySet())
        {
            if(entry.getValue().contains(roomName)) { return entry.getKey(); }
        }
        return null;
    }

    public LiveData<List<Integer>> getListOfFloorLevels() {
        return mListOfFloorLevels;
    }

    public List<String> getListOfRooms() throws IOException {
        List<String> rooms = new ArrayList<String>();
        mapRooms.values().forEach(rooms::addAll);
        return rooms;
    }
}
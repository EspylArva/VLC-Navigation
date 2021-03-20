//package com.vlcnavigation.ui.livemap;
//
//import android.app.Application;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.res.Resources;
//
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.vlcnavigation.R;
//import com.vlcnavigation.module.trilateration.Floor;
//import com.vlcnavigation.module.trilateration.Light;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.List;
//
//import timber.log.Timber;
//
//public class LiveMapViewModel extends AndroidViewModel {
//
//    private final Resources resources;
//    private final SharedPreferences preferences;
//
//    private final MutableLiveData<List<Light>> mListOfLights;
//    private final MutableLiveData<List<Floor>> mListOfFloors;
//
//
//    public LiveMapViewModel(Application app) {
//        super(app);
//        mListOfLights = new MutableLiveData<>();
//        mListOfFloors = new MutableLiveData<>();
//
//        mListOfLights.setValue(new ArrayList<Light>());
//        mListOfFloors.setValue(new ArrayList<Floor>());
//
//
//        resources = getApplication().getResources();
//        preferences = getApplication().getSharedPreferences("com.vlcnavigation", Context.MODE_PRIVATE);
//
//
//        if(!preferences.getString(resources.getString(R.string.sp_lights), "").equals(""))
//        {
//            String lights = preferences.getString(resources.getString(R.string.sp_lights), "");
//            Timber.d("SP: %s", lights);
//            Type TYPE_LIST_OF_LIGHT = new TypeToken<ArrayList<Light>>() {}.getType();
//            List<Light> savedLights = new Gson().fromJson(lights, TYPE_LIST_OF_LIGHT);
//            if (savedLights != null && savedLights.size() > 0) {
//                for (Light light : savedLights) {
//                    Timber.d("This light was saved: %s", light.toString());
//                    addLight(light);
//                }
//            }
//        }
//
//        if(!preferences.getString(resources.getString(R.string.sp_floors), "").equals(""))
//        {
//            String floors = preferences.getString(resources.getString(R.string.sp_floors), "");
//            Timber.d("SP: %s", floors);
//            Type TYPE_LIST_OF_LIGHT = new TypeToken<ArrayList<Floor>>() {}.getType();
//            List<Floor> savedFloors = new Gson().fromJson(floors, TYPE_LIST_OF_LIGHT);
//            if (savedFloors != null && savedFloors.size() > 0) {
//                for (Floor floor : savedFloors) {
//                    Timber.d("This light was saved: %s", floor.toString());
//                    addFloor(floor);
//                }
//            }
//        }
//    }
//
//    protected void addLight(Light newLight) {
//        mListOfLights.getValue().add(newLight);
//        saveLights();
//    }
//
//    protected void addFloor(Floor floor){
//        if(!mListOfFloors.getValue().stream().anyMatch(f -> f.getOrder() == floor.getOrder()))
//        {
//            mListOfFloors.getValue().add(floor);
//            mListOfFloors.getValue().sort(Floor::compareTo);
//            saveFloors();
//        }
//    }
//
//    public LiveData<List<Light>> getListOfLights() { return mListOfLights; }
//    public LiveData<List<Floor>> getListOfFloors() { return mListOfFloors; }
//
//    protected void removeFloorAt(int position) {
//        mListOfFloors.getValue().remove(position);
//        saveFloors();
//    }
//
//    protected void removeLightAt(int position) {
//        mListOfLights.getValue().remove(position);
//        saveLights();
//    }
//
//    protected void saveLights()
//    {
//        String json = new Gson().toJson(mListOfLights.getValue());
//        preferences.edit().putString(resources.getString(R.string.sp_lights), json).apply();
//    }
//
//    protected void saveFloors()
//    {
//        String json = new Gson().toJson(mListOfFloors.getValue());
//        preferences.edit().putString(resources.getString(R.string.sp_floors), json).apply();
//    }
//
//    protected Floor findFloor(int index) {
//        if(mListOfFloors.getValue().stream().anyMatch(floor -> floor.getOrder() == index))
//        {
//            return mListOfFloors.getValue().stream().filter(floor -> floor.getOrder() == index).findFirst().get();
//        }
//        else { Timber.e("CANT FIND FLOOR"); return null; }
//    }
//}
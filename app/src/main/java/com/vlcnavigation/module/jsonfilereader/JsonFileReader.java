package com.vlcnavigation.module.jsonfilereader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class JsonFileReader {

    private static final Type TYPE_LIST_OF_LIGHT = new TypeToken<ArrayList<Light>>() {}.getType();
    private static final Type TYPE_LIST_OF_FLOOR = new TypeToken<ArrayList<Floor>>() {}.getType();
    private static final Type TYPE_DATA_OBJECT = JsonFileReader.DataObject.class;

    public static final int READ_JSON_REQUEST_CODE = 305;

    /**
     * Opens an activity to let the user select a JSON file.
     * Code associated with this request should be READ_JSON_REQUEST_CODE (305)
     * The result of Activity should be handled in SettingsFragment onActivityResult
     *
     * @return Intent to open through startActivityForResult.
     */
    public static Intent lookForJsonIntent() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Json is not a valid MIME type according to Android: https://stackoverflow.com/questions/58055318/how-filter-json-files-with-intent-action-open-document
        // This filters out images, videos, audios but is not a perfect filter
        //        intent.setType("application/octet-stream"); // TODO: Filter JSON files only
        intent.setType("application/*");
        return intent;
    }

    /**
     * Reads the JSON file which should have been loaded using lookForJsonIntent.
     * Unmarshalls the content of the JSON file into JsonFileReader.DataObject object (container for List of Light and List of Floor)
     * and saves the list of Light and list of Floor into sharedPreferences at
     * CAUTION: DOES NOT CLOSE THE INPUTSTREAM 'is'
     *
     * @param is InputStream of the JSON file
     * @param context Context which asked for the import. Necessary to access Resources and SharedPreferences, to save the new data
     * @throws IOException
     */
    public static void importDataFromFile(InputStream is, Context context) throws IOException {

        // Build the string from the file
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) { sb.append(line).append("\n"); }

        // Close InputStream and BufferedReader to avoid memory loss
        reader.close();

        Timber.d("Read that from the file: \n %s", sb.toString());

        Gson gson = new Gson();
        Resources resources = context.getResources();
        SharedPreferences preferences = context.getSharedPreferences(resources.getString(R.string.sp_base), Context.MODE_PRIVATE);
        DataObject data = gson.fromJson(sb.toString(), TYPE_DATA_OBJECT);

        preferences.edit().putString(resources.getString(R.string.sp_lights), gson.toJson(data.getLights())).apply();
        preferences.edit().putString(resources.getString(R.string.sp_floors), gson.toJson(data.getFloors())).apply();
    }

    /**
     * Unmarshalls a List of Light from a JSON formatted String
     * @param lightJson JSON formatted String representing a list of Light
     * @return list of Light unmarshalled
     */
    public static List<Light> getLightsFromJson(String lightJson) {
        return new Gson().fromJson(lightJson, TYPE_LIST_OF_LIGHT);
    }

    /**
     * Unmarshalls a List of Floor from a JSON formatted String
     * @param floorJson JSON formatted String representing a list of Floor
     * @return list of Floor unmarshalled
     */
    public static List<Floor> getFloorsFromJson(String floorJson) {
        return new Gson().fromJson(floorJson, TYPE_LIST_OF_FLOOR);
    }


    // Needed for unmarshalling
    private class DataObject {
        private List<Light> lights;
        private List<Floor> floors;
        public List<Light> getLights() { return lights; }
        public List<Floor> getFloors() { return floors; }
    }




}

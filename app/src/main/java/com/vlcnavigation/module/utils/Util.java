package com.vlcnavigation.module.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Util {

    /**
     * Hides the software keyboard from an Activity
     *
     * @param activity Activity we need to hide the software keyboard from
     */
    public static void hideKeyboard(Activity activity)
    {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Hides the software keyboard from a non-activity part.
     * This is called from the RecyclerAdapter
     *
     * @param v View we need to hide the software keyboard from
     */
    public static void hideKeyboardFromView(View v)
    {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}

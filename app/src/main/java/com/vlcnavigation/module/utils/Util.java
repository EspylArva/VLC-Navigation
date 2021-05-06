package com.vlcnavigation.module.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

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

    /**
     * Converts a size in pixel to a size in density-independent pixel
     *
     * @param px size in pixels
     * @param context Context
     * @return size in density-independent pixels
     */
    public static int pxToDp(int px, Context context)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px * scale + 0.5f);
    }

    @ColorInt
    public static int modifyAlpha(@ColorInt int color, int alpha)
    {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

}

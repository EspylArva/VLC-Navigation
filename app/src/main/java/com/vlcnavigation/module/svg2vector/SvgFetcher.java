package com.vlcnavigation.module.svg2vector;

import android.app.Activity;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;


public class SvgFetcher {
    public static final int READ_SVG_REQUEST_CODE = 303;

    public static Intent lookForSvgIntent() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra("requestCode", READ_SVG_REQUEST_CODE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");

        return intent;
    }
}

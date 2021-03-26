package com.vlcnavigation.module.svg2vector;

import android.content.Intent;

public class SvgFetcher {
    public static final int READ_SVG_REQUEST_CODE = 303;
    public static final int ADD_SVG_REQUEST_CODE = 304;

    /**
     * Opens an activity to let the user select a SVG file.
     * Code associated with this request should be
     *     - READ_SVG_REQUEST_CODE (303) if it is called from FloorAdapter.FloorHolder and handled in FloorsLightsManagerFragment
     *     - ADD_SVG_REQUEST_CODE (304) if it is called from AddFloorFragment and handled in AddFloorFragment
     *
     * @return Intent to open through startActivityForResult.
     */
    public static Intent lookForSvgIntent() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");
        return intent;
    }
}

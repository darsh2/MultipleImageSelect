package com.darsh.multipleimageselect.helpers;

/**
 * Created by Darshan on 5/26/2015.
 */
public class Constants {
    public static final int REQUEST_CODE = 2000;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;

    public static final String INTENT_EXTRA_ALBUM = "album";
    public static final String INTENT_EXTRA_IMAGES = "images";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final int DEFAULT_LIMIT = 10;

    //Maximum number of images that can be selected at a time
    public static int limit;

    //String to display when number of selected images limit is exceeded
    public static String toastDisplayLimitExceed = String.format("Can select maximum of %d images", limit);
}

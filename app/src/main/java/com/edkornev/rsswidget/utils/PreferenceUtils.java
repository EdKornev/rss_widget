package com.edkornev.rsswidget.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kornev on 05/12/16.
 */
public class PreferenceUtils {

    public static final String KEY_SETTINGS = "settings";
    public static final String KEY_SETTINGS_RSS_LINK = "settings_rss_link";
    public static final String KEY_SETTINGS_POSTS = "settings_posts";

    public static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(KEY_SETTINGS, Context.MODE_PRIVATE);
    }
}

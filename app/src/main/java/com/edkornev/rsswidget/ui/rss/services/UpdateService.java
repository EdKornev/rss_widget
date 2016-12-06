package com.edkornev.rsswidget.ui.rss.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import com.edkornev.rsswidget.utils.ParseUtils;
import com.edkornev.rsswidget.utils.PostsUtils;
import com.edkornev.rsswidget.utils.PreferenceUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by kornev on 05/12/16.
 */
public class UpdateService extends IntentService {

    private static final String TAG = UpdateService.class.getSimpleName();

    public UpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        loadRss();
    }

    private void loadRss() {
        String rssLink = PreferenceUtils.getPref(getApplicationContext()).getString(PreferenceUtils.KEY_SETTINGS_RSS_LINK, "");

        if (rssLink.isEmpty()) {
            return;
        }

        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(rssLink);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            InputStream in = urlConnection.getInputStream();

            parse(in);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            List<ParseUtils.EntryRssLink> posts = ParseUtils.readFeed(parser);
            PostsUtils.getInstance().setPosts(posts);
        } finally {
            in.close();
        }
    }


}
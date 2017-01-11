package com.edkornev.rsswidget.ui.rss.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.Xml;

import com.edkornev.rsswidget.ui.rss.providers.RssWidgetProvider;
import com.edkornev.rsswidget.utils.ParseUtils;
import com.edkornev.rsswidget.utils.PostsUtils;
import com.edkornev.rsswidget.utils.PreferenceUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kornev on 05/12/16.
 */
public class UpdateService extends IntentService {

    private static final String TAG = UpdateService.class.getSimpleName();

    private Set<String> mAppWidgetIds;

    public UpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mAppWidgetIds = PreferenceUtils.getPref(this).getStringSet(PreferenceUtils.KEY_SETTINGS_WIDGET_IDS, new HashSet<String>());

        for (String id : mAppWidgetIds) {
            loadRss(id);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent service = new Intent(this, UpdateService.class);

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MINUTE, 1);

            // Более правильный вариант, но android позволит только раз 15 минут делать http запрос
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            // life hack который будет рабоать всегда, но "грязный" вариант, т.к. в таком случае doze mode просто не будет включаться
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
        }
    }

    private void loadRss(String id) {
        String rssLink = PreferenceUtils.getPref(getApplicationContext()).getString(id, "");

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
            urlConnection.setConnectTimeout(25 * 1000); // 25 seconds
            urlConnection.setReadTimeout(25 * 1000); // 25 seconds

            InputStream in = urlConnection.getInputStream();

            parse(id, in);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void parse(String id, InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            List<ParseUtils.EntryRssLink> posts = ParseUtils.readFeed(parser);
            PostsUtils.getInstance().setPosts(Integer.valueOf(id), posts);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            RssWidgetProvider.updateWidget(getApplicationContext(), appWidgetManager, Integer.valueOf(id));
        } finally {
            in.close();
        }
    }


}

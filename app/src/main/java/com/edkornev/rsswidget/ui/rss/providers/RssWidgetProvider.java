package com.edkornev.rsswidget.ui.rss.providers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.edkornev.rsswidget.R;
import com.edkornev.rsswidget.ui.rss.services.UpdateService;
import com.edkornev.rsswidget.utils.ParseUtils;
import com.edkornev.rsswidget.utils.PostsUtils;
import com.edkornev.rsswidget.utils.PreferenceUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kornev on 05/12/16.
 */
public class RssWidgetProvider extends AppWidgetProvider {

    private static final String TAG = RssWidgetProvider.class.getCanonicalName();

    public final static String ACTION_NEXT_POST = "com.edkornev.rsswidget.app.next_post";
    public final static String ACTION_PREV_POST = "com.edkornev.rsswidget.app.prev_post";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Set<String> ids = new HashSet<>();

        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);

            ids.add(String.valueOf(id));
        }

        PreferenceUtils
                .getPref(context)
                .edit()
                .putStringSet(PreferenceUtils.KEY_SETTINGS_WIDGET_IDS, ids)
                .apply();

        startService(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        Set<String> ids = PreferenceUtils.getPref(context).getStringSet(PreferenceUtils.KEY_SETTINGS_WIDGET_IDS, new HashSet<String>());

        for (int id : appWidgetIds) {
            ids.remove(String.valueOf(id));

            PreferenceUtils
                    .getPref(context)
                    .edit()
                    .putString(String.valueOf(id), "")
                    .apply();

            PostsUtils.getInstance().remove(id);
        }

        PreferenceUtils
                .getPref(context)
                .edit()
                .putStringSet(PreferenceUtils.KEY_SETTINGS_WIDGET_IDS, ids)
                .apply();

        startService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        Intent service = new Intent(context, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        int appWidgetId;

        if (intent.getAction().equalsIgnoreCase(ACTION_NEXT_POST)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                PostsUtils.getInstance().getNextPost(appWidgetId);
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        } else if (intent.getAction().equalsIgnoreCase(ACTION_PREV_POST)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                PostsUtils.getInstance().getPrevPost(appWidgetId);
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        }
    }

    private void startService(Context context) {
        Intent service = new Intent(context, UpdateService.class);

        boolean isAlarmRun = (PendingIntent.getService(context, 0, service, PendingIntent.FLAG_NO_CREATE) != null);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);
        long frequency =  60 * 1000; // 1 minute

        if (!isAlarmRun) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Более правильный вариант, но android позволит только раз 15 минут делать http запрос
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                // life hack который будет работать всегда, но "грязный" вариант, т.к. в таком случае doze mode просто не будет включаться
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
            } else {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
            }
        } else {
            alarmManager.cancel(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Более правильный вариант, но android позволит только раз 15 минут делать http запрос
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                // life hack который будет рабоать всегда, но "грязный" вариант, т.к. в таком случае doze mode просто не будет включаться
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
            } else {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
            }
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        ParseUtils.EntryRssLink rssItem = PostsUtils.getInstance().getCurrentPost(widgetID);

        if (rssItem == null) {
            return;
        }

        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_rss);
        if (rssItem.getTitle() != null) {
            widgetView.setTextViewText(R.id.tv_title, rssItem.getTitle());
        }

        if (rssItem.getDescription() != null) {
            widgetView.setTextViewText(R.id.tv_description, rssItem.getDescription().trim());
        }

        // next listener
        Intent nextIntent = new Intent(context, RssWidgetProvider.class);
        nextIntent.setAction(ACTION_NEXT_POST);
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, nextIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tv_gt, pIntent);

        // prev listener
        Intent prevIntent = new Intent(context, RssWidgetProvider.class);
        prevIntent.setAction(ACTION_PREV_POST);
        prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        pIntent = PendingIntent.getBroadcast(context, widgetID, prevIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tv_lt, pIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }
}

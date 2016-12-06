package com.edkornev.rsswidget.ui.rss.providers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.edkornev.rsswidget.R;
import com.edkornev.rsswidget.ui.rss.services.UpdateService;
import com.edkornev.rsswidget.utils.ParseUtils;
import com.edkornev.rsswidget.utils.PostsUtils;

import java.util.Calendar;

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

        startService(context, appWidgetIds);

        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
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
                PostsUtils.getInstance().getNextPost();
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        } else if (intent.getAction().equalsIgnoreCase(ACTION_PREV_POST)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                PostsUtils.getInstance().getprevPost();
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        }
    }

    private void startService(Context context, int[] appWidgetIds) {
        Intent service = new Intent(context, UpdateService.class);
        service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);
        long frequency =  60 * 1000; // 1 minute

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        ParseUtils.EntryRssLink rssItem = PostsUtils.getInstance().getCurrentPost();

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

package com.edkornev.rsswidget.ui.rss.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.edkornev.rsswidget.R;
import com.edkornev.rsswidget.utils.ParseUtils;
import com.edkornev.rsswidget.utils.PostsUtils;

/**
 * Created by kornev on 05/12/16.
 */
public class RssWidgetProvider extends AppWidgetProvider {

    final static String ACTION_NEXT_POST = "com.edkornev.rsswidget.app.next_post";
    final static String ACTION_PREV_POST = "com.edkornev.rsswidget.app.prev_post";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

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
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        if (intent.getAction().equalsIgnoreCase(ACTION_NEXT_POST)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                PostsUtils.getInstance().getNextPost();
                updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
            }
        } else if (intent.getAction().equalsIgnoreCase(ACTION_PREV_POST)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                PostsUtils.getInstance().getprevPost();
                updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
            }
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        ParseUtils.EntryRssLink rssItem = PostsUtils.getInstance().getCurrentPost();

        if (rssItem == null) {
            return;
        }

        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_rss);
        widgetView.setTextViewText(R.id.tv_title, rssItem.title);

        // next listener
        Intent nextIntent = new Intent(context, RssWidgetProvider.class);
        nextIntent.setAction(ACTION_NEXT_POST);
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetID });

        PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, nextIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tv_gt, pIntent);

        // prev listener
        Intent prevIntent = new Intent(context, RssWidgetProvider.class);
        prevIntent.setAction(ACTION_PREV_POST);
        prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetID });

        pIntent = PendingIntent.getBroadcast(context, widgetID, prevIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tv_lt, pIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }
}

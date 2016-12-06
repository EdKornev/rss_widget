package com.edkornev.rsswidget;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.edkornev.rsswidget.ui.rss.services.UpdateService;

import java.util.Calendar;

/**
 * Created by kornev on 05/12/16.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent service = new Intent(getApplicationContext(), UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, service, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 15);
        long frequency = 60 * 60 * 1000; // 1 minute

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
    }
}

package com.edkornev.rsswidget.ui.settings.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.edkornev.rsswidget.R;
import com.edkornev.rsswidget.utils.PreferenceUtils;

/**
 * Created by kornev on 05/12/16.
 */
public class SettingsActivity extends Activity implements View.OnClickListener {

    private static final String KEY_INTENT_FLAG = "isRssSaved";

    private EditText mETRSSLink;
    private boolean mIsRssSaved = false;

    private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getIntent() != null) {
            mWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
            setResult(RESULT_CANCELED, intent);
        }

        String savedRssLink = PreferenceUtils.getPref(this).getString(String.valueOf(mWidgetId), "");

        mETRSSLink = (EditText) findViewById(R.id.et_rss_link);
        mETRSSLink.setText(savedRssLink);

        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mIsRssSaved) {
            if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(KEY_INTENT_FLAG, mIsRssSaved);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mIsRssSaved = savedInstanceState.getBoolean(KEY_INTENT_FLAG, false);
    }

    @Override
    public void onClick(View view) {
        String rssLink = mETRSSLink.getText().toString().trim();

        if (rssLink.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_field_rss_link, Toast.LENGTH_LONG).show();
            return;
        }

        PreferenceUtils.getPref(this)
                .edit()
                .putString(String.valueOf(mWidgetId), rssLink)
                .apply();

        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
            setResult(RESULT_OK, intent);

            mIsRssSaved = true;

            finish();
        }
    }
}

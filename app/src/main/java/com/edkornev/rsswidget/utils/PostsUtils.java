package com.edkornev.rsswidget.utils;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kornev on 06/12/16.
 */
public class PostsUtils {

    private static PostsUtils mInstance;

    private SparseArray<WidgetModel> mData = new SparseArray<>();

    private PostsUtils() {
    }

    public static PostsUtils getInstance() {
        if (mInstance == null) {
            synchronized (PostsUtils.class) {
                if (mInstance == null) {
                    mInstance = new PostsUtils();
                }
            }
        }

        return mInstance;
    }

    public ParseUtils.EntryRssLink getCurrentPost(int widgetId) {
        WidgetModel model = mData.get(widgetId);

        if (model == null || model.mPosts.size() == 0) {
            return null;
        } else if (model.mCurrentPosition >= model.mPosts.size()) {
            model.mCurrentPosition = 0;
        }

        return model.mPosts.get(model.mCurrentPosition);
    }

    public ParseUtils.EntryRssLink getNextPost(int widgetId) {
        WidgetModel model = mData.get(widgetId);

        if (model == null || model.mPosts.size() == 0) {
            return null;
        }

        model.mCurrentPosition += 1;

        if (model.mCurrentPosition >= model.mPosts.size()) {
            model.mCurrentPosition = 0;
        }

        return model.mPosts.get(model.mCurrentPosition);
    }

    public ParseUtils.EntryRssLink getprevPost(int widgetId) {
        WidgetModel model = mData.get(widgetId);

        if (model == null || model.mPosts.size() == 0) {
            return null;
        }

        model.mCurrentPosition -= 1;

        if (model.mCurrentPosition < 0) {
            model.mCurrentPosition = model.mPosts.size() - 1;
        }

        return model.mPosts.get(model.mCurrentPosition);
    }

    public void setPosts(int widgetId, List<ParseUtils.EntryRssLink> posts) {
        WidgetModel model = new WidgetModel();
        model.mPosts = posts;

        this.mData.append(widgetId, model);
    }

    public void remove(int widgetId) {
        this.mData.remove(widgetId);
    }

    private class WidgetModel {
        private List<ParseUtils.EntryRssLink> mPosts = new ArrayList<>();
        private int mCurrentPosition = 0;
    }
}

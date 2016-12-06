package com.edkornev.rsswidget.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kornev on 06/12/16.
 */
public class PostsUtils {

    private static PostsUtils mInstance;

    private List<ParseUtils.EntryRssLink> mPosts = new ArrayList<>();
    private int mCurrentPosition = 0;

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

    public ParseUtils.EntryRssLink getCurrentPost() {
        if (mPosts.size() == 0) {
            return null;
        } else if (mCurrentPosition >= mPosts.size()) {
            mCurrentPosition = 0;
        }

        return mPosts.get(mCurrentPosition);
    }

    public ParseUtils.EntryRssLink getNextPost() {
        if (mPosts.size() == 0) {
            return null;
        } else if (++mCurrentPosition >= mPosts.size()) {
            mCurrentPosition = 0;
        }

        return mPosts.get(mCurrentPosition);
    }

    public ParseUtils.EntryRssLink getprevPost() {
        if (mPosts.size() == 0) {
            return null;
        } else if (--mCurrentPosition < 0) {
            mCurrentPosition = mPosts.size() - 1;
        }

        return mPosts.get(mCurrentPosition);
    }

    public void setPosts(List<ParseUtils.EntryRssLink> posts) {
        this.mPosts = posts;
    }
}

package com.edkornev.rsswidget.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kornev on 06/12/16.
 */
public class ParseUtils {

    private static final String NS = null;

    public static List<EntryRssLink> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<EntryRssLink> result = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, NS, "rss");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("channel")) {
                result.addAll(readChannel(parser));
            } else {
                skip(parser);
            }
        }

        return result;
    }

    private static List<EntryRssLink> readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<EntryRssLink> result = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("item")) {
                result.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }

        return result;
    }


    private static EntryRssLink readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readText(parser);
            } else if (name.equals("link")) {
                link = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new EntryRssLink(title, link);
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static class EntryRssLink {
        public final String title;
        public final String link;

        private EntryRssLink(String title, String link) {
            this.title = title;
            this.link = link;
        }
    }
}

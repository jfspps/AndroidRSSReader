package com.example.top10newsfeeds;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

// parses the top ten apps
public class ParseApplications {

    public static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;    // in a XML tag or not?
        String textValue = "";      // stores current tag value

        try {
            // setup an XML Pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  // factory method design pattern (builds the required instances automatically) ?
            factory.setNamespaceAware(true);
            XmlPullParser xmlPullParser = factory.newPullParser();

            // read from the downloaded XML string, xmlData
            xmlPullParser.setInput(new StringReader(xmlData));

            // respond to different parts of the XML document, triggered by events
            int eventType = xmlPullParser.getEventType();
            // continue until the end of the XML
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: Starting tag for " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xmlPullParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: Ending tag for " + tagName);
                        if (inEntry) {
                            // note how we skip any metadata until the program finds the first <entry> tag
                            // as good practice: with XML files, "entry".methodName is never null whereas tagName.methodName might be
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
            for (FeedEntry app : applications){
                Log.d(TAG, "*********************************");
                Log.d(TAG, app.toString());
            }

        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}

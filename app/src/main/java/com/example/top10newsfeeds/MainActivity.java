package com.example.top10newsfeeds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
    }

    // called to inflate the menu objects
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // pass feeds_menu.xml (found in res directory)
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        return true;
    }

    // handles menu events, with MenuItem passed being the menu item clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String feedUrl;

        switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                break;
            default:
                // needed to execute the default action should the menu (or submenu present) not return a valid value outside the switch block
                return super.onOptionsItemSelected(item);
        }

        downloadUrl(feedUrl);
        return true;
    }

    private void downloadUrl(String feedUrl) {
        Log.d(TAG, "downloadUrl: starting ASyncTask");
        DownloadData downloadData = new DownloadData();

        // update as needed
        downloadData.execute(feedUrl);

        Log.d(TAG, "downloadUrl: done");
    }


    private class DownloadData extends AsyncTask<String, Void, String>{
        // ASyncTask: pass a String, no need for progress bar (hence void) and result return type
        // i.e. pass a URL string, no need for progress bar and return an XML feed

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            // parseApplications holds the arraylist needed
            // the adapter manages the resources sent to a view, and recycles them when required
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(
//                    MainActivity.this, R.layout.list_item, parseApplications.getApplications()
//            );

            // use the custom adapter
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
                listApps.setAdapter(feedAdapter);

            // link the listView, listApps, with the adapter (feedAdapter or default arrayAdapter)
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null){
                //loge persists after compilation, unlike logd
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath){
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);                                                 // URL exception possible, handled first
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();    // IOException possible
                int response = connection.getResponseCode();                                // IOException possible
                Log.d(TAG, "downloadXML: The response code was: " + response);

                // these calls could be chained together
                InputStream inputStream = connection.getInputStream();                      // IOException possible
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                int charsRead;
                char[] inputBuffer = new char[500];     // this can be increased
                while (true){
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0){
                        break;
                    }
                    if (charsRead > 0){
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();     // inputStream and its reader closed at the same time

                return xmlResult.toString();
            } catch (MalformedURLException e){
                Log.e(TAG, "downloadXML: Invalid URL; " + e.getMessage());
            } catch (IOException e){
                Log.e(TAG, "downloadXML: IO exception reading data; " + e.getMessage());
            } catch (SecurityException e){
                Log.e(TAG, "downloadXML: security exception: need permission? " + e.getMessage());
//                e.printStackTrace();
            }

            return null;
        }
    }
}
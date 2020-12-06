package com.example.top10newsfeeds;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: starting ASyncTask");
        DownloadData downloadData = new DownloadData();

        // update as needed
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");

        Log.d(TAG, "onCreate: done");
    }

    private class DownloadData extends AsyncTask<String, Void, String>{
        // ASyncTask: pass a String, no need for progress bar (hence void) and result return type
        // i.e. pass a URL string, no need for progress bar and return an XML feed

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
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
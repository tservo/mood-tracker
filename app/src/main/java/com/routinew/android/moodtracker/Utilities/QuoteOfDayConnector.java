package com.routinew.android.moodtracker.Utilities;

import android.content.Context;
import android.net.Uri;

import com.facebook.stetho.json.annotation.JsonValue;
import com.routinew.android.moodtracker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import timber.log.Timber;

/**
 * in order to demonstrate an AsyncTask query to a network server, I am borrowing from 
 * Popular Movies Stage 2 TMDB Connector code
 */
public class QuoteOfDayConnector {
    private static final String QOD_HOSTNAME = "quotes.rest";
    private static final String QOD_URL = "http://"+QOD_HOSTNAME+"/qod.json";


    public static String getJsonPayload() {
        if (isOffline()) return null;

        return getNetworkResponse(createQuoteURL());
    }

    /**
     * {
     *   "success": {
     *     "total": 1
     *   },
     *   "contents": {
     *     "quotes": [
     *       {
     *         "quote": "Do not worry if you have built your castles in the air. They are where they should be. Now put the foundations under them.",
     *         "length": "122",
     *         "author": "Henry David Thoreau",
     *         "tags": [
     *           "dreams",
     *           "inspire",
     *           "worry"
     *         ],
     *         "category": "inspire",
     *         "date": "2016-11-21",
     *         "title": "Inspiring Quote of the day",
     *         "background": "https://theysaidso.com/img/bgs/man_on_the_mountain.jpg",
     *         "id": "mYpH8syTM8rf8KFORoAJmQeF"
     *       }
     *     ]
     *   }
     * }
     * @return
     */
    public static String getBackgroundURL(Context context) {
        String json = getJsonPayload();
        if (null == json) return null; // no json, bail


        String quote;
        String backgroundURL;

        try {
            JSONObject root = new JSONObject(json);
            JSONObject quoteObject = root.getJSONObject("contents")
                    .getJSONArray("quotes")
                    .getJSONObject(0);
            quote = quoteObject.optString("quote", context.getString(R.string.NO_QUOTE_FOUND));
            backgroundURL = quoteObject.optString("background");

        } catch (JSONException e) {
            Timber.w(e,"unable to parse %s",json);
            return null;
        }

        return backgroundURL;
    }

    /**
     * Find out if we have a reliable connection to Quote of Day
     * from https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
     * @return can we connect?
     */
    private static boolean isOffline() {
        try {
            int timeoutMs = 1500;
            final int HTTP = 80;
            Socket sock = new Socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(QOD_HOSTNAME,HTTP);

            sock.connect(inetSocketAddress,timeoutMs);
            sock.close();
            return false;
        } catch (IOException e) {
            Timber.w(e,"Network unreachable");
            return true;
        }
    }

    /**
     *
     * Helper method to create the Uri needed to access the appropriate information.
     * @return the created url
     */
    private static URL createQuoteURL() {
        Uri uri = Uri.parse(QOD_URL);

        Timber.d(uri.toString());

        URL url=null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    /**
     * Get the response from the server as a string
     * this code is from T02.04 of the ud851-Exercises
     * @param url the URL to read
     * @return the response as a string
     */
    private static String getNetworkResponse(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

            Timber.d("before calling %s",url.toString());

            // attempt to get the stream of data and put it into a string.
            if (isOffline()) throw new IOException("Network is unreachable");
            InputStream in = urlConnection.getInputStream();
            Timber.d("calling %s",url.toString());

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                String json = scanner.next();
                Timber.d(json);
                return json;
            } else {
                return null;
            }
        } catch(IOException e) {
            e.printStackTrace();
            Timber.w(e.toString());
            return null;
        } finally {
            if (null != urlConnection) urlConnection.disconnect();
        }
    }
}

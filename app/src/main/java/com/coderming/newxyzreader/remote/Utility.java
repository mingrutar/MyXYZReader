package com.coderming.newxyzreader.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by linna on 7/15/2016.
 */
public class Utility {
    static SimpleDateFormat dateFormater = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

    public static String formatAuthorDate(String author, long eDate) {
        Date date = new Date(eDate);
        return String.format("%s, by %s", dateFormater.format(date), author);
    }

    public static String formatPublishDate(long eDate) {
        Date date = new Date(eDate);
        return dateFormater.format(date);
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return ((activeNetwork!=null) && activeNetwork.isConnectedOrConnecting());
    }
}

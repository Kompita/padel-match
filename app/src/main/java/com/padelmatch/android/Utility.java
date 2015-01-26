package com.padelmatch.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Utility {

    public static String getPreferredLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_login_key),
                context.getString(R.string.pref_login_default));
    }

    public static boolean getDisplayNotifications(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
        return displayNotifications;
    }

    public static boolean getShowOnlyOpened(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String showOnlyOpenedKey = context.getString(R.string.pref_show_only_opened_key);
        boolean showOnlyOpened = prefs.getBoolean(showOnlyOpenedKey,
                Boolean.parseBoolean(context.getString(R.string.pref_show_only_opened_default)));
        return showOnlyOpened;
    }

    /**
     * Helper method to provide the icon resource for the listview
     * @param numberOfPlayers
     * @param iPlay
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForMatchStatus(int numberOfPlayers,boolean iPlay) {
        if (numberOfPlayers ==0) {
            return R.drawable.ic_0players;
        } else if (numberOfPlayers == 1 && !iPlay) {
            return R.drawable.ic_1players;
        } else if (numberOfPlayers == 1 && iPlay) {
            return R.drawable.ic_1player_with_me;
        }else if (numberOfPlayers == 2 && !iPlay) {
            return R.drawable.ic_2players;
        } else if (numberOfPlayers == 2 && iPlay) {
            return R.drawable.ic_2players_with_me;
        }else if (numberOfPlayers == 3 && !iPlay) {
            return R.drawable.ic_3players;
        } else if (numberOfPlayers == 3 && iPlay) {
            return R.drawable.ic_3players_with_me;
        }else if (numberOfPlayers == 4 && !iPlay) {
            return R.drawable.ic_4players;
        } else if (numberOfPlayers == 4 && iPlay) {
            return R.drawable.ic_4players_with_me;
        }
        return -1;
    }

    public static String formatDate(Context context,Long longDate) {
        Date inputDate = new Date(longDate);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat(context.getString(R.string.date_format));
        return monthDayFormat.format(inputDate);
    }

    public static String formatTime(Context context,Long longTime) {
        Date inputDate = new Date(longTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat(context.getString(R.string.time_format));
        return timeFormat.format(inputDate);
    }

}

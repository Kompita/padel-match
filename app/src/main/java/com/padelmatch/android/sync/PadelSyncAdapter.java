package com.padelmatch.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.padelmatch.android.HomeActivity;
import com.padelmatch.android.R;
import com.padelmatch.android.Utility;
import com.padelmatch.android.data.PadelContract.MatchesEntry;
import com.padelmatch.android.data.PadelContract.PlayerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PadelSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PadelSyncAdapter.class.getName();
    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    //public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_INTERVAL = 2;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final int PADEL_NOTIFICATION_ID = 3005;


    public PadelSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String loginParam = Utility.getPreferredLogin(getContext());

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;
        try {
            final String FORECAST_BASE_URL =
                    "http://padelmatch-dev.azurewebsites.net/api/";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL+"getAvailableMatches?").buildUpon()
                    .appendQueryParameter("login", loginParam)
                    .appendQueryParameter("nd", Integer.toString(10))
                    .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error########### ", e);
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            JSONArray matchesArray = new JSONArray(forecastJsonStr);
            // Insert the new weather information into the database
            List<ContentValues> parsedMatchesList = new ArrayList<ContentValues>(matchesArray.length());
            List<ContentValues> parsedPlayersList = new ArrayList<ContentValues>();

            for(int i = 0; i < matchesArray.length(); i++) {
                // Get the JSON object representing the match
                JSONObject matchJSONObject = matchesArray.getJSONObject(i);
                JSONArray playersJSONArray = matchJSONObject.has("players")?matchJSONObject.getJSONArray("players"):null;

                ContentValues matchesValues = new ContentValues();
                matchesValues.put(MatchesEntry.COLUMN_ID, matchJSONObject.getLong("idMatch"));
                matchesValues.put(MatchesEntry.COLUMN_NAME, matchJSONObject.getString("name"));
                matchesValues.put(MatchesEntry.COLUMN_NUMBER_OF_PLAYERS, playersJSONArray!=null?playersJSONArray.length():0);
                matchesValues.put(MatchesEntry.COLUMN_DATE, matchJSONObject.getLong("date"));
                matchesValues.put(MatchesEntry.COLUMN_TIME, matchJSONObject.getLong("time"));
                matchesValues.put(MatchesEntry.COLUMN_DURATION_IN_MINUTES, matchJSONObject.getLong("durationInMinutes"));
                if(matchJSONObject.has("padelCourt")){
                    JSONObject padelCourtJSONObject = matchJSONObject.getJSONObject("padelCourt");
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_NAME, padelCourtJSONObject.getString("name"));
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_LOCATION, padelCourtJSONObject.has("location")?padelCourtJSONObject.getString("location"):null);
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_ADITIONAL_NOTES, padelCourtJSONObject.has("aditionalNotes")?padelCourtJSONObject.getString("aditionalNotes"):null);
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_LATITUDE, padelCourtJSONObject.has("latitude")?padelCourtJSONObject.getDouble("latitude"):null);
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_LONGITUDE,padelCourtJSONObject.has("longitude")?padelCourtJSONObject.getDouble("longitude"):null);


                }else{
                    matchesValues.put(MatchesEntry.COLUMN_PADELCOURT_NAME, matchJSONObject.has("courtDescription")?matchJSONObject.getString("courtDescription"):null);

                }
                matchesValues.put(MatchesEntry.COLUMN_DURATION_IN_MINUTES, matchJSONObject.has("durationInMinutes")?matchJSONObject.getLong("durationInMinutes"):null);
                matchesValues.put(MatchesEntry.COLUMN_PRICE_PER_PERSON, matchJSONObject.has("pricePerPerson")?matchJSONObject.getString("pricePerPerson"):null);
                matchesValues.put(MatchesEntry.COLUMN_ADDITIONAL_INFO, matchJSONObject.has("additionalInfo")?matchJSONObject.getString("additionalInfo"):null);

                boolean iPlay = false;
                for(int j=0;playersJSONArray!=null && j<playersJSONArray.length();j++){
                    JSONObject playerJSONObject = playersJSONArray.getJSONObject(j);
                    ContentValues playerValues = new ContentValues();
                    playerValues.put(PlayerEntry.COLUMN_FK_MATCH, matchJSONObject.getLong("idMatch"));
                    //I only use sqlite as cache, so there could be several instances of the same player on the database.
                    //To handle this, instead of the real id, a autogenerated id is used
                    //playerValues.put(PlayerEntry.COLUMN_ID,playerJSONObject.getLong("idPlayer"));
                    playerValues.put(PlayerEntry.COLUMN_LOGIN,playerJSONObject.getString("login"));
                    playerValues.put(PlayerEntry.COLUMN_REAL_NAME,playerJSONObject.getString("realName"));
                    parsedPlayersList.add(playerValues);
                    if(playerJSONObject.getString("login").equals(loginParam)){
                        iPlay=true;
                    }
                }
                matchesValues.put(MatchesEntry.COLUMN_IPLAY, iPlay);
                parsedMatchesList.add(matchesValues);

            }
            if ( parsedMatchesList.size() > 0 ) {
                //Get the saved matches to control if a match is new or not
                Map<Long,Boolean> mapOfId = new HashMap<Long,Boolean>();
                List<ContentValues> newMatches = new ArrayList<ContentValues>();
                Cursor oldMatches = getContext().getContentResolver().query(MatchesEntry.buildMatchUri(),new String[]{MatchesEntry._ID},null,null,null);
                while(oldMatches.moveToNext()){
                    mapOfId.put(oldMatches.getLong(0),true);
                }
                for(int i=0;i<parsedMatchesList.size();i++){
                    if(mapOfId.get(parsedMatchesList.get(i).get(MatchesEntry.COLUMN_ID))==null && parsedMatchesList.get(i).getAsLong(MatchesEntry.COLUMN_NUMBER_OF_PLAYERS)<4 ){
                        newMatches.add(parsedMatchesList.get(i));
                    }
                }

                getContext().getContentResolver().delete(MatchesEntry.buildMatchUri(),null,null);
                getContext().getContentResolver().delete(PlayerEntry.buildPlayerUri(), null, null);
                getContext().getContentResolver().bulkInsert(MatchesEntry.CONTENT_URI, parsedMatchesList.toArray(new ContentValues[parsedMatchesList.size()]));
                getContext().getContentResolver().bulkInsert(PlayerEntry.CONTENT_URI, parsedPlayersList.toArray(new ContentValues[parsedPlayersList.size()]));

                //Notify about matches
                notifyNewMatches(newMatches);

            }
            Log.d(LOG_TAG, "FetchPadelMatchesTask Complete. " + parsedMatchesList.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return;
    }


    private void notifyNewMatches(List<ContentValues> newMatches) {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day

        boolean displayNotifications = Utility.getDisplayNotifications(context);
        if ( displayNotifications && newMatches.size()>0) {
            //If there are new matches I show them to the user
            // NotificationCompatBuilder is a very convenient way to build backward-compatible
            // notifications.  Just throw in some data.
            String loginParam = Utility.getPreferredLogin(getContext());
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getContext())
                            .setSmallIcon(R.drawable.ic_small_notification)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                            .setContentTitle(context.getString(R.string.app_name) + "."+ newMatches.size() +" " + getContext().getString(R.string.notification_label))
                            .setContentText(getContext().getString(R.string.notification_text));

            // Make something interesting happen when the user clicks on the notification.
            // In this case, opening the app is sufficient.
            Intent resultIntent = new Intent(context, HomeActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
            mNotificationManager.notify(PADEL_NOTIFICATION_ID, mBuilder.build());

        }

    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        PadelSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}

package com.padelmatch.android;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.padelmatch.android.data.PadelContract;
import com.padelmatch.android.data.PadelContract.PlayerEntry;
import com.padelmatch.android.sync.PadelSyncAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MatchDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MatchDetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #PadelMatchApp";

    private String mMatchInfo = null;

    private ShareActionProvider mShareActionProvider;
    private Long mMatchId;

    private String posLat;
    private String posLong;
    private boolean iPlay=false;
    private Long matchId;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {

            PadelContract.MatchesEntry._ID,
            PadelContract.MatchesEntry.COLUMN_NAME,
            PadelContract.MatchesEntry.COLUMN_DATE,
            PadelContract.MatchesEntry.COLUMN_TIME,
            PadelContract.MatchesEntry.COLUMN_DURATION_IN_MINUTES,
            PadelContract.MatchesEntry.COLUMN_PADELCOURT_NAME,
            PadelContract.MatchesEntry.COLUMN_IPLAY,
            PadelContract.MatchesEntry.COLUMN_NUMBER_OF_PLAYERS,
            PadelContract.MatchesEntry.COLUMN_ADDITIONAL_INFO,
            PadelContract.MatchesEntry.COLUMN_PADELCOURT_ADITIONAL_NOTES,
            PadelContract.MatchesEntry.COLUMN_PRICE_PER_PERSON,
            PadelContract.MatchesEntry.COLUMN_PADELCOURT_LATITUDE,
            PadelContract.MatchesEntry.COLUMN_PADELCOURT_LONGITUDE

    };

    private static final String[] PLAYER_COLUMNS = {

            PlayerEntry._ID,
            PlayerEntry.COLUMN_LOGIN,
            PlayerEntry.COLUMN_REAL_NAME


    };

    public static final int COL_MATCH_ID = 0;
    public static final int COL_MATCH_NAME = 1;
    public static final int COL_DATE = 2;
    public static final int COL_TIME=3;
    public static final int COL_DURATION_IN_MINUTES=4;
    public static final int COL_PADELCOURT_NAME=5;
    public static final int COL_IPLAY=6;
    public static final int COL_NUMBER_OF_PLAYERS = 7;
    public static final int COL_ADDITIONAL_INFO = 8;
    public static final int COL_PADELCOURT_ADITIONAL_NOTES=9;
    public static final int COL_PRICE_PER_PERSON=10;
    public static final int COL_PADELCOURT_LATITUDE=11;
    public static final int COL_PADELCOURT_LONGITUDE=12;

    public static final int COL_PLAYERL_ID =0;
    public static final int COL_PLAYER_LOGIN=1;
    public static final int COL_PLAYER_REAL_NAM=2;

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mAdditionalInfoView;
    private TextView mDateView;
    private TextView mTimeView;
    private TextView mDurationView;
    private TextView mTextPlayersView;
    private TextView mPlayersView;
    private TextView mLocationNameView;
    private TextView mLocationAdditionalNotesView;
    private TextView mPricePerPersonView;
    private Button mButton;



    public MatchDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMatchId = arguments.getLong(DetailActivity.MATCH_ID_KEY);
        }
        View rootView = inflater.inflate(R.layout.fragment_match_detail, container, false);
        mIconView= (ImageView) rootView.findViewById(R.id.detail_icon);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        mAdditionalInfoView = (TextView) rootView.findViewById(R.id.detail_additional_info_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mTimeView = (TextView) rootView.findViewById(R.id.detail_time_textview);
        mDurationView = (TextView) rootView.findViewById(R.id.detail_duration_textview);
        mTextPlayersView = (TextView) rootView.findViewById(R.id.detail_textplayers_textview);
        mPlayersView = (TextView) rootView.findViewById(R.id.detail_players_textview);
        mLocationNameView = (TextView) rootView.findViewById(R.id.detail_location_name_textview);
        mLocationAdditionalNotesView = (TextView) rootView.findViewById(R.id.detail_location_additional_notes_textview);
        mPricePerPersonView = (TextView) rootView.findViewById(R.id.detail_price_per_person_textview);
        mButton = (Button) rootView.findViewById(R.id.action_button);



        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMatchInfo != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMatchInfo + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.MATCH_ID_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = PadelContract.MatchesEntry.COLUMN_ID + " ASC";


        Uri weatherForLocationUri = PadelContract.MatchesEntry.buildMatchUriByID(mMatchId);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            mNameView.setText(data.getString(COL_MATCH_NAME));
            mDateView.setText(Utility.formatDate(getActivity(), data.getLong(COL_DATE)));
            mTimeView.setText(Utility.formatTime(getActivity(), data.getLong(COL_TIME)));
            iPlay=(data.getInt(COL_IPLAY)==1);
            matchId=data.getLong(COL_MATCH_ID);
            mIconView.setImageResource(Utility.getIconResourceForMatchStatus(
                     data.getInt(COL_NUMBER_OF_PLAYERS),
                     iPlay));

            mAdditionalInfoView.setText(data.getString(COL_ADDITIONAL_INFO));


            mDurationView.setText(data.getString(COL_DURATION_IN_MINUTES) +" " +getActivity().getString(R.string.match_minutes));
            mTextPlayersView.setText(getActivity().getString(R.string.players_label));
            //mPlayersView
            mLocationNameView.setText(data.getString(COL_PADELCOURT_NAME));
            mLocationAdditionalNotesView.setText(data.getString(COL_PADELCOURT_ADITIONAL_NOTES));
            mPricePerPersonView.setText(data.getString(COL_PRICE_PER_PERSON));

            Cursor playerCursor = getActivity().getContentResolver().query(PlayerEntry.buildPlayerUriByMatchId(data.getLong(COL_MATCH_ID)),PLAYER_COLUMNS,null,null,null);
            String playersData = "";
            while(playerCursor.moveToNext()){
                if(!playersData.equals("")){
                    playersData+="\n";
                }
                playersData+=playerCursor.getString(COL_PLAYER_LOGIN);
            }
            mPlayersView.setText(playersData);

            if(iPlay){
                mButton.setText(getActivity().getString(R.string.unsubscribe_label));
            }else{
                mButton.setText(getActivity().getString(R.string.subscribe_label));
            }
            mButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(iPlay) {
                        new SubscribeAction(getActivity()).execute(matchId, 1l);
                    }else{
                        new SubscribeAction(getActivity()).execute(matchId, 0l);
                    }

                }
            });

            posLat=data.getString(COL_PADELCOURT_LATITUDE);
            posLong=data.getString(COL_PADELCOURT_LONGITUDE);

            mMatchInfo=data.getString(COL_MATCH_NAME) + " " + data.getString(COL_MATCH_NAME) + ". "
                    + Utility.formatDate(getActivity(), data.getLong(COL_DATE)) + " - "
                    +Utility.formatTime(getActivity(), data.getLong(COL_TIME));            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }


    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (posLat != null && !posLat.equals("") && posLong != null && !posLong.equals("")){
            Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
            }
        }
    }
}

class SubscribeAction extends AsyncTask<Long, Integer, Long> {

    private static final String LOG_TAG = SubscribeAction.class.getSimpleName();

    Context context;
    public SubscribeAction(Context context) {
        this.context = context;
    }

    protected Long doInBackground(Long... parameters) {
        String loginParam = Utility.getPreferredLogin(context);
        String action="subscribe";
        if(parameters[1]==1l){
            action="unsubscribe";
        }
        final String FORECAST_BASE_URL =
                "http://padelmatch-dev.azurewebsites.net/api/";
        Uri builtUri = Uri.parse(FORECAST_BASE_URL+action+"?").buildUpon()
                .appendQueryParameter("login", loginParam)
                .appendQueryParameter("idMatch", Long.toString(parameters[0]))
                .build();

        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            Log.i(LOG_TAG,buffer.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
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
        return null;
    }



    protected void onPostExecute(Long result) {
        PadelSyncAdapter.syncImmediately(context);
    }
}



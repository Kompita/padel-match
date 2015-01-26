package com.padelmatch.android;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.padelmatch.android.data.PadelContract;
import com.padelmatch.android.data.PadelContract.MatchesEntry;
import com.padelmatch.android.sync.PadelSyncAdapter;

import java.util.Date;

public class MatchesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String LOG_TAG = MatchesListFragment.class.getName();
    private MatchesListAdapter mMatchesListAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int FORECAST_LOADER = 0;
    private boolean showOnlyOpened = false;

    private static final String[] FORECAST_COLUMNS = {

            MatchesEntry._ID,
            MatchesEntry.COLUMN_NAME,
            MatchesEntry.COLUMN_DATE,
            MatchesEntry.COLUMN_TIME,
            MatchesEntry.COLUMN_DURATION_IN_MINUTES,
            MatchesEntry.COLUMN_PADELCOURT_NAME,
            MatchesEntry.COLUMN_IPLAY,
            MatchesEntry.COLUMN_NUMBER_OF_PLAYERS
    };

    public static final int COL_MATCH_ID = 0;
    public static final int COL_MATCH_NAME = 1;
    public static final int COL_DATE = 2;
    public static final int COL_TIME=3;
    public static final int COL_DURATION_IN_MINUTES=4;
    public static final int COL_PADELCOURT_NAME=5;
    public static final int COL_IPLAY=6;
    public static final int COL_NUMBER_OF_PLAYERS = 7;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.matches_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMatchesListAdapter = new MatchesListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_matches_list, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_padelmatches);
        mListView.setAdapter(mMatchesListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mMatchesListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getLong(COL_MATCH_ID));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }


        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private void updateWeather() {
        PadelSyncAdapter.syncImmediately(getActivity());
    }



    @Override
    public void onResume() {
        super.onResume();
        if (showOnlyOpened != Utility.getShowOnlyOpened(getActivity())) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.

        String startDate = PadelContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = MatchesEntry.COLUMN_ID + " ASC";

        showOnlyOpened = Utility.getShowOnlyOpened(getActivity());
        Uri weatherForLocationUri = MatchesEntry.buildMatchUriByType(showOnlyOpened?MatchesEntry.TYPE_ONLY_OPENED:MatchesEntry.TYPE_ALL);

        Log.i(LOG_TAG,"******************creandoloader**********");
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
        mMatchesListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMatchesListAdapter.swapCursor(null);
    }






    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Long matchId);
    }




}
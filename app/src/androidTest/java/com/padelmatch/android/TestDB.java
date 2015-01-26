package com.padelmatch.android;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.padelmatch.android.data.PadelContract;
import com.padelmatch.android.data.PadelDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by jmurciego on 30/12/14.
 */
public class TestDB extends AndroidTestCase {

    public static final String LOG_TAG = TestDB.class.getSimpleName();

    static final String TEST_MATCH_ID= "1";
    static final String TEST_TIME= "1900";
    static final String TEST_DATE = "20141205";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(PadelDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new PadelDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }



    public void testInsertReadDb() {

        PadelDbHelper dbHelper = new PadelDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createPadelMatch();

        long matchID;
        matchID = db.insert(PadelContract.MatchesEntry.TABLE_NAME, null, testValues);

        Log.d(LOG_TAG, "New row id: " + matchID);

        // Verify we got a row back.
        assertTrue(matchID != -1);


        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                PadelContract.MatchesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues player = assignPlayers(matchID);

        long weatherRowId = db.insert(PadelContract.PlayerEntry.TABLE_NAME, null, player);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                PadelContract.PlayerEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(weatherCursor, player);

        dbHelper.close();
    }

    static ContentValues createPadelMatch() {
        ContentValues matchValues = new ContentValues();
        matchValues.put(PadelContract.MatchesEntry.COLUMN_ID, TEST_MATCH_ID);
        matchValues.put(PadelContract.MatchesEntry.COLUMN_NAME, "Padel en Santurce");
        return matchValues;
    }

    static ContentValues assignPlayers(Long matchID) {
        ContentValues testValues = new ContentValues();
        testValues.put(PadelContract.PlayerEntry.COLUMN_LOGIN,"Pepe");
        testValues.put(PadelContract.PlayerEntry.COLUMN_ID,1);
        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        Log.d(LOG_TAG,valueCursor.toString());
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}

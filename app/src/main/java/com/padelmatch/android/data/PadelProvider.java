package com.padelmatch.android.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.padelmatch.android.data.PadelContract.MatchesEntry;
import com.padelmatch.android.data.PadelContract.PlayerEntry;
/**
 * Defines table and column names for the padel database.
 */

public class PadelProvider extends ContentProvider {

    public static final String LOG_TAG = PadelProvider.class.getName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PadelDbHelper mOpenHelper;

    private static final int MATCHES=100;
    private static final int PLAYERS=200;
    private static final int MATCH_BY_ID=250;
    private static final int MATCHES_BY_TYPE = 300;
    private static final int PLAYERS_BY_MATCH = 400;



    private static final String byTypeSelectionTemplate =
             MatchesEntry.TABLE_NAME+
                    "." + MatchesEntry.COLUMN_IPLAY + " =  1 OR "
            +MatchesEntry.TABLE_NAME+
                    "." + MatchesEntry.COLUMN_NUMBER_OF_PLAYERS + " < 4";


    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PadelContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.


        matcher.addURI(authority, PadelContract.PATH_MATCHES , MATCHES);
        matcher.addURI(authority, PadelContract.PATH_PLAYER , PLAYERS);
        matcher.addURI(authority, PadelContract.PATH_MATCHES+"/#" , MATCH_BY_ID);
        matcher.addURI(authority, PadelContract.PATH_MATCHES+"/type/*" , MATCHES_BY_TYPE);
        matcher.addURI(authority, PadelContract.PATH_PLAYER+"/match/#" , PLAYERS_BY_MATCH);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new PadelDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor= null;
        switch (sUriMatcher.match(uri)) {
            // "matches/*/*"
            case MATCHES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchesEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PLAYERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PlayerEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCH_BY_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchesEntry.TABLE_NAME,
                        projection,
                        MatchesEntry._ID + " = " + MatchesEntry.getIdFromUri(uri) ,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCHES_BY_TYPE: {
                String type = MatchesEntry.getTypeFromUri(uri);
                String byTypeSelection = null;
                if(type.equals(MatchesEntry.TYPE_ONLY_OPENED)){
                    byTypeSelection=byTypeSelectionTemplate;
                }
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchesEntry.TABLE_NAME,
                        projection,
                        byTypeSelection,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PLAYERS_BY_MATCH: {
                String matchId = PlayerEntry.getMatchFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PlayerEntry.TABLE_NAME,
                        projection,
                        PlayerEntry.COLUMN_FK_MATCH +"="+matchId,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MATCHES_BY_TYPE:
                return MatchesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MATCHES: {
                long _id = db.insert(MatchesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MatchesEntry.buildMatchUriByID(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLAYERS: {
                long _id = db.insert(PlayerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PlayerEntry.buildPlayerUriByID(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case MATCHES:
                rowsDeleted = db.delete(
                        MatchesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PLAYERS:
                rowsDeleted = db.delete(
                        PlayerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MATCHES:
                rowsUpdated = db.update(MatchesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PLAYERS:
                rowsUpdated = db.update(PlayerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MatchesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }



}

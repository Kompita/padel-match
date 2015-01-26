package com.padelmatch.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PadelDbHelper extends SQLiteOpenHelper {

    private final String LOG_TAG = PadelDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 10;

    public static final String DATABASE_NAME = "padel.db";

    public PadelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MATCHES_TABLE = "CREATE TABLE " + PadelContract.MatchesEntry.TABLE_NAME + " (" +
                PadelContract.MatchesEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                PadelContract.MatchesEntry.COLUMN_NAME + " TEXT NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_TIME + " INTEGER NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_DURATION_IN_MINUTES + " INTEGER NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_PADELCOURT_NAME + " TEXT NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_PADELCOURT_LOCATION + " TEXT," +
                PadelContract.MatchesEntry.COLUMN_PADELCOURT_ADITIONAL_NOTES + " TEXT," +
                PadelContract.MatchesEntry.COLUMN_PADELCOURT_LATITUDE + " REAL," +
                PadelContract.MatchesEntry.COLUMN_PADELCOURT_LONGITUDE + " REAL," +
                PadelContract.MatchesEntry.COLUMN_PRICE_PER_PERSON + " TEXT," +
                PadelContract.MatchesEntry.COLUMN_ADDITIONAL_INFO + " TEXT," +
                PadelContract.MatchesEntry.COLUMN_IPLAY + " INTEGER NOT NULL," +
                PadelContract.MatchesEntry.COLUMN_NUMBER_OF_PLAYERS + " INTEGER NOT NULL);";


        final String SQL_CREATE_PLAYERS_TABLE = "CREATE TABLE " + PadelContract.PlayerEntry.TABLE_NAME + " (" +
                PadelContract.PlayerEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                PadelContract.PlayerEntry.COLUMN_LOGIN + " TEXT NOT NULL,"+
                PadelContract.PlayerEntry.COLUMN_REAL_NAME + " TEXT NOT NULL,"+
                PadelContract.PlayerEntry.COLUMN_FK_MATCH + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_MATCHES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PadelContract.PlayerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PadelContract.MatchesEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}

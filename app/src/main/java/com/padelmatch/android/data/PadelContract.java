package com.padelmatch.android.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PadelContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.padelmatch.android";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MATCHES = "match";
    public static final String PATH_PLAYER = "player";

    public static final String DATE_FORMAT = "yyyyMMdd";


    public static final class MatchesEntry implements BaseColumns {

        public static final String TYPE_ALL = "ALL";
        public static final String TYPE_ONLY_OPENED = "OPENED";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCHES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;


        public static final String TABLE_NAME = "matches";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_DURATION_IN_MINUTES = "durationInMinutes";
        public static final String COLUMN_PADELCOURT_NAME = "padelcourt_name";
        public static final String COLUMN_PADELCOURT_LOCATION="padelcourt_location";
        public static final String COLUMN_PADELCOURT_ADITIONAL_NOTES="padelcourt_aditional_notes";
        public static final String COLUMN_PADELCOURT_LATITUDE ="padelcourt_latitude";
        public static final String COLUMN_PADELCOURT_LONGITUDE="padelcourt_longitude";
        public static final String COLUMN_PRICE_PER_PERSON="price_per_person";
        public static final String COLUMN_ADDITIONAL_INFO ="additional_info";


        public static final String COLUMN_NUMBER_OF_PLAYERS = "number_of_players";
        public static final String COLUMN_IPLAY = "iplay";



        public static Uri buildMatchUri() {
            return CONTENT_URI;
        }

        public static Uri buildMatchUriByID(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMatchUriByType(
                String type) {
            return CONTENT_URI.buildUpon().appendPath("type").appendPath(type).build();
        }

        public static String getTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class PlayerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PLAYER;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PLAYER;

        public static final String TABLE_NAME = "players";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_REAL_NAME = "realName";
        public static final String COLUMN_FK_MATCH = "fk_match";

        public static Uri buildPlayerUri() {
            return CONTENT_URI;
        }

        public static Uri buildPlayerUriByID(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPlayerUriByMatchId(long id) {
            return CONTENT_URI.buildUpon().appendPath("match").appendPath(Long.toString(id)).build();
        }

        public static String getMatchFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

    public static String getDbDateString(Date date){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

}

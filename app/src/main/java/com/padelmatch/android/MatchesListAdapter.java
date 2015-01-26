package com.padelmatch.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class MatchesListAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;


    public static class ViewHolder {
        public final TextView nameView;
        public final TextView dateView;
        public final TextView timeView;
        public final TextView durationView;
        public final TextView locationView;
        public final ImageView iconView;
        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.list_item_matches_name_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_matches_date_textview);
            timeView = (TextView) view.findViewById(R.id.list_item_matches_time_textview);
            durationView = (TextView) view.findViewById(R.id.list_item_matches_duration_in_minutes_textview);
            locationView = (TextView) view.findViewById(R.id.list_item_matches_location_textview);
            iconView = (ImageView) view.findViewById(R.id.list_item_matches_icon_textview);

        }
    }

    public MatchesListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate( R.layout.list_item_match, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameView.setText(cursor.getString(MatchesListFragment.COL_MATCH_NAME));
        viewHolder.dateView.setText(Utility.formatDate(context, cursor.getLong(MatchesListFragment.COL_DATE)));
        viewHolder.timeView.setText(Utility.formatTime(context, cursor.getLong(MatchesListFragment.COL_TIME)));
        viewHolder.durationView.setText(cursor.getString(MatchesListFragment.COL_DURATION_IN_MINUTES) +" " +context.getString(R.string.match_minutes));
        viewHolder.locationView.setText(cursor.getString(MatchesListFragment.COL_PADELCOURT_NAME));

        viewHolder.iconView.setImageResource(Utility.getIconResourceForMatchStatus(
                cursor.getInt(MatchesListFragment.COL_NUMBER_OF_PLAYERS),
                cursor.getInt(MatchesListFragment.COL_IPLAY)==1));

        if(cursor.getInt(MatchesListFragment.COL_NUMBER_OF_PLAYERS) == 4 &&
        cursor.getInt(MatchesListFragment.COL_IPLAY)==0){
            view.setBackgroundColor(context.getResources().getColor(R.color.padelmatch_deactivated));
        }else{

            view.setBackgroundColor(context.getResources().getColor(R.color.background_material_light));
        }
    }


    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


}

package com.example.ian.deffsound.songgroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ian.deffsound.R;

import java.util.ArrayList;

/**
 * Created by Ian on 12/4/2016.
 */
public class SongGroupAdaptor extends BaseAdapter {
        private ArrayList<SongGroup> songGroups;
        private LayoutInflater songInf;

        public SongGroupAdaptor(Context c, ArrayList<SongGroup> songGroups) {
            this.songGroups = songGroups;
            songInf = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return songGroups.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //map to song layout
            LinearLayout songLay =
                    (LinearLayout)songInf.inflate(R.layout.songgroup, parent, false);
            TextView groupView = (TextView) songLay.findViewById(R.id.group_title);
            TextView trackView = (TextView) songLay.findViewById(R.id.group_tracks);
            //get song using position
            SongGroup currGroup = songGroups.get(position);
            //get title and artist Strings
            groupView.setText(currGroup.getTitle());
            int trackCount = currGroup.getTotalTracks();
            trackView.setText( trackCount +
                    (trackCount > 1 ? " songs":" song"));
            //set position as tag
            songLay.setTag(position);
            return songLay;
        }

}

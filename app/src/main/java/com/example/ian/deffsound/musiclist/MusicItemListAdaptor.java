package com.example.ian.deffsound.musiclist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ian.deffsound.R;
import com.example.ian.deffsound.musiclist.ArtistItem;
import com.example.ian.deffsound.musiclist.MusicItem;

import java.util.ArrayList;

/**
 * Created by Ian on 12/4/2016.
 */
public class MusicItemListAdaptor extends BaseAdapter {
        private ArrayList<MusicItem> musicItemList;
        private LayoutInflater songInf;

        public MusicItemListAdaptor(Context c, ArrayList<MusicItem> musicItemList) {
            this.musicItemList = musicItemList;
            songInf = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return musicItemList.size();
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
            MusicItem currGroup = musicItemList.get(position);
            View musicItemView = null;
            switch(currGroup.getCategory()) {
                case ARTISTS:
                    musicItemView = getArtistItemView((ArtistItem)musicItemList.get(position), parent);
                    break;
            }
            //set position as tag
            musicItemView.setTag(position);
            return musicItemView;
        }

        private View getArtistItemView(ArtistItem artist, ViewGroup parent) {
            //map to artist layout
            LinearLayout artistItemLay =
                    (LinearLayout)songInf.inflate(R.layout.artist_item, parent, false);
            TextView titleView = (TextView) artistItemLay.findViewById(R.id.artist_title);
            TextView nAlbumView = (TextView) artistItemLay.findViewById(R.id.artist_n_albums);

            titleView.setText(artist.getTitle());
            nAlbumView.setText(artist.getAlbumCount() + " album(s)");

            return artistItemLay;
        }

}

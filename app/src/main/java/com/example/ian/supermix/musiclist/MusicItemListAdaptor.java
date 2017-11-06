package com.example.ian.supermix.musiclist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ian.supermix.MainActivity;
import com.example.ian.supermix.MusicService;
import com.example.ian.supermix.R;
import com.example.ian.supermix.songqueue.NowPlayingController;
import com.example.ian.supermix.songview.Song;

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
        MusicItem item = musicItemList.get(position);
        View musicItemView = null;
        switch(item.getCategory()) {
            case ARTIST:
                musicItemView = getArtistItemView((ArtistItem)item, parent);
                break;
            case ALBUM:
                musicItemView = getAlbumItemView((AlbumItem)item, parent);
                break;
            case SONG:
                musicItemView = getSongItemView((SongItem)item, parent);
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
        String albumLabel = artist.getAlbumCount() > 1 ? " albums" : " album";
        nAlbumView.setText(artist.getAlbumCount() + albumLabel);

        return artistItemLay;
    }

    private View getAlbumItemView(AlbumItem album, ViewGroup parent) {
        //map to album layout
        LinearLayout albumItemLay =
                (LinearLayout)songInf.inflate(R.layout.album_item, parent, false);
        TextView titleView = (TextView) albumItemLay.findViewById(R.id.album_title);
        TextView nTrackView = (TextView) albumItemLay.findViewById(R.id.album_n_songs);
        TextView yearView = (TextView) albumItemLay.findViewById(R.id.album_year);

        Bitmap bm = BitmapFactory.decodeFile(album.getAlbumArt());
        ImageView albumArt = (ImageView) albumItemLay.findViewById(R.id.album_art);
        albumArt.setImageBitmap(bm);

        titleView.setText(album.getTitle());
        String songLabel = album.getTrackCount() > 1 ? " songs" : " song";
        nTrackView.setText(album.getTrackCount() + songLabel);
        yearView.setText(album.getYear());

        return albumItemLay;
    }

    private View getSongItemView(SongItem song, ViewGroup parent) {
        //map to song layout
        LinearLayout songItemLay =
                (LinearLayout)songInf.inflate(R.layout.song_item, parent, false);
        TextView titleView = (TextView) songItemLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) songItemLay.findViewById(R.id.song_artist);
        TextView trackLengthView = (TextView) songItemLay.findViewById(R.id.song_length);

        // check if view matches current song
        NowPlayingController controller = NowPlayingController.getInstance();
        if (controller.isActivePlaylistSet()) {
            Song currentSong = controller.getAvailableSong();
            if (currentSong != null &&
                    song.getTitle().equals(currentSong.getTitle()) &&
                    song.getAlbum().equals(currentSong.getAlbum()) &&
                    song.getArtist().equals(currentSong.getArtist())) {
                titleView.setTextColor(parent.getResources().getColor(R.color.colorPrimary));
                trackLengthView.setTextColor(parent.getResources().getColor(R.color.colorPrimary));
                artistView.setTextColor(parent.getResources().getColor(R.color.colorPrimary));
            }
        }

        titleView.setText(song.getTitle());
        artistView.setText(song.getArtist());
        trackLengthView.setText(song.getTrackLength());

        return songItemLay;
    }

}

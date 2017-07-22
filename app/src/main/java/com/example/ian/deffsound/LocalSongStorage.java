package com.example.ian.deffsound;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.ian.deffsound.musiclist.AlbumItem;
import com.example.ian.deffsound.musiclist.ArtistItem;
import com.example.ian.deffsound.musiclist.MusicItem;
import com.example.ian.deffsound.musiclist.SongItem;
import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;

/**
 * Created by ian on 10/01/17.
 */

public class LocalSongStorage {

    public static ArrayList<Song> retrieveAllSongsPlaylist(Context context) {
        return getSongPlaylist(context, "", null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
    }

    public static ArrayList<Song> retrieveAlbumPlaylist(Context context, String album) {
        return getSongPlaylist(context, " AND " + MediaStore.Audio.Media.ALBUM + "=?",
                new String[]{album}, MediaStore.Audio.Media.TRACK);
    }

    public static ArrayList<MusicItem> retrieveAllArtistsDirectory(Context context) {
        return getArtistDirectory(context, "", null, MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC");
    }

    public static ArrayList<MusicItem> retrieveAllAlbumsDirectory(Context context) {
        return getAlbumDirectory(context, "", null, MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC");
    }

    public static ArrayList<MusicItem> retrieveAllSongsDirectory(Context context) {
        return getSongDirectory(context, "", null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
    }

    public static ArrayList<MusicItem> retrieveAllAlbumsFromArtistDirectory(Context context, String artist) {
        return getAlbumDirectory(context, MediaStore.Audio.Albums.ARTIST + " LIKE ?",
                new String[]{artist + "%"},
                MediaStore.Audio.Albums.FIRST_YEAR  + " DESC, " + MediaStore.Audio.Media.ALBUM);
    }

    public static ArrayList<MusicItem> retrieveSongsInAlbumDirectory(Context context, String album) {
        return getSongDirectory(context, " AND " + MediaStore.Audio.Media.ALBUM + "=?",
                new String[]{album}, MediaStore.Audio.Media.TRACK);
    }

    public static ArrayList<MusicItem> retrieveSongsFromArtistDirectory(Context context, String artist) {
        return getSongDirectory(context, " AND " + MediaStore.Audio.Media.ARTIST + "=?",
                new String[]{artist}, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
    }

    private static ArrayList<MusicItem> getSongDirectory(Context context, String where, String[] whereParams, String orderBy) {
        ArrayList<MusicItem> songList = new ArrayList<MusicItem>();

        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0" + where;
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int trackLengthColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrackLength = musicCursor.getInt(trackLengthColumn);

                songList.add(new SongItem(thisTitle, thisArtist, thisAlbum, thisTrackLength));
            } while (musicCursor.moveToNext());
        }
        return songList;
    }

    private static ArrayList<MusicItem> getArtistDirectory(Context context, String selection, String[] selectionArgs, String orderBy) {
        ArrayList<MusicItem> artistList = new ArrayList<MusicItem>();
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        //TODO decide whether orderBy is ever needed for artists
        orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisAlbums = musicCursor.getString(albumColumn);
                if(artistList.size() > 0 &&
                        thisTitle.contains("feat.")) continue;
                artistList.add( new ArtistItem(thisTitle, Integer.valueOf(thisAlbums)) );
            } while (musicCursor.moveToNext());
        }
        //musicItemList = artistList;
        return artistList;
    }

    private static ArrayList<MusicItem> getAlbumDirectory(Context context, String where, String[] whereParams, String orderBy) {
        ArrayList<MusicItem> albumList = new ArrayList<MusicItem>();
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, where, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int yearColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
            int albumArtColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisTrack = musicCursor.getString(trackColumn);
                String thisYear = musicCursor.getString(yearColumn);
                String thisAlbumArt = musicCursor.getString(albumArtColumn);

                albumList.add(new AlbumItem(thisTitle, Integer.valueOf(thisTrack), thisYear, thisAlbumArt));
            } while (musicCursor.moveToNext());
        }
        //musicItemList = albumList;
        return albumList;
    }

    private static ArrayList<Song> getSongPlaylist(Context context, String where, String[] whereParams, String orderBy) {
        ArrayList<Song> playlist = new ArrayList<Song>();

        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0" + where;
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(trackColumn);

                playlist.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, thisTrack));
            } while (musicCursor.moveToNext());
        }
        return playlist;
    }
}

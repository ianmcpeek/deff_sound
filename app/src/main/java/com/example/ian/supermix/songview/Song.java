package com.example.ian.supermix.songview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ian McPeek on 8/28/2016.
 */

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int track;
    private long duration;
    private long albumId;

    public Song(long id, String title, String artist, String album, int track, long duration, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.track = track;
        this.duration = duration;
        this.albumId = albumId;
    }

    public long getId(){return id;}
    public String getTitle(){return title;}
    public String getArtist() {return artist;}
    public String getAlbum() {return album;}
    public int getTrack() {return track;}
    public long getDuration() {return duration;}
    public long getAlbumId() {return albumId;}
}

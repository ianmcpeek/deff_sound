package com.example.ian.deffsound.songview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ian McPeek on 8/28/2016.
 */

public class Song implements Parcelable{
    private long id;
    private String title;
    private String artist;
    private String album;
    private int track;
    private String albumArt;
    private String albumArtist;

    public Song(long id, String title, String artist, String album, int track, String albumArt, String albumArtist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.track = track;
        this.albumArt = albumArt;
        this.albumArtist = albumArtist;
    }

    public long getId(){return id;}
    public String getTitle(){return title;}
    public String getArtist() {return artist;}
    public String getAlbum() {return album;}
    public int getTrack() {return track;}
    public String getAlbumArt(){return albumArt;}
    public String getAlbumArtist(){return albumArtist;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeInt(track);
        dest.writeString(albumArt);
        dest.writeString(albumArtist);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

        @Override
        public Song createFromParcel(Parcel in) {
            Song song = new Song(in.readLong(), in.readString(),
                    in.readString(), in.readString(), in.readInt(), in.readString(), in.readString());
            return song;
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}

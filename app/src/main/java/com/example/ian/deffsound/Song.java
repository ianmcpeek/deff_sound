package com.example.ian.deffsound;

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

    public Song(long id, String title, String artist, String album) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public long getId(){return id;}
    public String getTitle(){return title;}
    public String getArtist() {return artist;}
    public String getAlbum() {return album;}

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
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

        @Override
        public Song createFromParcel(Parcel in) {
            Song song = new Song(in.readLong(), in.readString(), in.readString(), in.readString());
            return song;
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}

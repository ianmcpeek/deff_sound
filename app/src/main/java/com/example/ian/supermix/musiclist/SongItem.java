package com.example.ian.supermix.musiclist;

import com.example.ian.supermix.MusicDirectoryType;

/**
 * Created by ian on 06/01/17.
 */

public class SongItem implements MusicItem {

    private String title;
    private String artistTitle;
    private String albumTitle;
    private String trackLength;

    public SongItem(String title, String artistTitle, String albumTitle, int trackLength) {
        this.title = title;
        this.artistTitle = artistTitle;
        this.albumTitle = albumTitle;
        this.trackLength = formatTrackLength(trackLength);
    }

    @Override
    public boolean isSong() {
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public MusicDirectoryType getCategory() { return MusicDirectoryType.SONG; }

    public String getArtist() {
            return artistTitle;
        }

    public String getAlbum() {return albumTitle; }

    //may modify to return formatted string
    public String getTrackLength() { return trackLength; }

    private String formatTrackLength(int length) {
        String trackLengthStr = "";
        int maxTime = length;
        int sec = (length / 1000) % 60;
        int min = (length / 1000 / 60) % 60;
        trackLengthStr += min < 10 ? "0" + min : min;
        trackLengthStr += ":";
        trackLengthStr += sec < 10 ? "0" + sec : sec;

        return trackLengthStr;
    }

}


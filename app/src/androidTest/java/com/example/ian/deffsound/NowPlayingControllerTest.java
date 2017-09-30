package com.example.ian.deffsound;

import android.test.suitebuilder.annotation.SmallTest;

import com.example.ian.deffsound.songqueue.NowPlayingController;
import com.example.ian.deffsound.songview.Song;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by ian on 9/30/2017.
 */
public class NowPlayingControllerTest extends TestCase {
    private NowPlayingController nowPlayingController;

    public void setUp() throws Exception {
        super.setUp();
        nowPlayingController = new NowPlayingController();
    }

    @SmallTest
    public void testGetAvailableSong() {
        assertNull(nowPlayingController.getAvailableSong());

        ArrayList<Song> playlist = new ArrayList();
        playlist.add(new Song(0, "Test Title", "Test Artist", "Test Album", 0));
        nowPlayingController.setActivePlaylist(playlist, 0);

        assertEquals("Test Title", nowPlayingController.getAvailableSong().getTitle());
    }

}
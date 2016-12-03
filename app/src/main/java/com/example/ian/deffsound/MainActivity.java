package com.example.ian.deffsound;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends ActionBarActivity{

//    private MusicService musicService;
//    private Intent playIntent;
//    private boolean musicBound = false;
    private ArrayList<Song> songList;
    private ListView songView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Song>();
        songView = (ListView)findViewById(R.id.song_list);
        //retrieve data
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                int rVal = a.getArtist().compareTo(b.getArtist());
                if (rVal == 0) {
                    rVal = a.getAlbum().compareTo(b.getAlbum());
                    if (rVal == 0) {
                        return a.getTitle().compareTo(b.getTitle());

                    }
                }
                return rVal;
            }
        });
        SongAdaptor songAdt = new SongAdaptor(this, songList);
        songView.setAdapter(songAdt);
    }

    @Override
    protected void onDestroy() {
//        stopService(playIntent);
//        musicService = null;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if(playIntent == null) {
//            playIntent = new Intent(this, MusicService.class);
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                //start nowplaying activity
//                Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
//                MainActivity.this.startActivity(intent);
                break;
            case R.id.action_end:
//                stopService(playIntent);
//                musicService = null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    private ServiceConnection musicConnection =
//            new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
//            //get service
//            musicService = binder.getService();
//            //pass list
//            musicService.setSongs(songList);
//            musicBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
//        }
//    };

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            String path =
                    musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            } while (musicCursor.moveToNext());
        }
    }

    public void songPicked(View view) {
        //start nowplaying activity
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        //bundle song picked into NowPlayingActivity
        intent.putExtra("songPicked", Integer.parseInt(view.getTag().toString()));
        intent.putParcelableArrayListExtra("songList", songList);
        MainActivity.this.startActivity(intent);
    }
}

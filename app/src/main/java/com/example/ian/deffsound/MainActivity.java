package com.example.ian.deffsound;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.ian.deffsound.musiclist.MusicItem;
import com.example.ian.deffsound.musiclist.MusicItemListAdaptor;
import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements NavigationWidget.OnFragmentInteractionListener,
    NowPlayingWidget.OnFragmentInteractionListener{

//    private MusicService musicService;
//    private Intent playIntent;
    private boolean musicBound = false;
    private MusicService musicService;
    private ArrayList<Song> playlist;
    private ArrayList<MusicItem> musicItemList;
    private ListView musicListView;
    //used to track user breadcrumbs

    private MusicDirectoryQueryHistory queryHistory;


    //private Stack<MusicDirectoryQuery> history;
    //private MusicDirectoryQuery currentSnapShot =
     //       new MusicDirectoryQuery(MusicDirectoryType.SONG, "", null, MediaStore.Audio.Media.TITLE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playlist = null;
        musicItemList = new ArrayList<MusicItem>();
        musicListView = (ListView)findViewById(R.id.song_list);
        //retrieve data

        //initialize stack and set to default list when history is empty
        MusicDirectoryQuery defaultQuery = new MusicDirectoryQuery(MusicDirectoryType.SONG, null);
        queryHistory = new MusicDirectoryQueryHistory(defaultQuery);

        //set & display default directory
        musicItemList = retrieveDirectoryFromStorage(defaultQuery);
        displayDirectory();
        //eventually stack should read from a temp file first before setting screen


        //really weird bug where a delay needs to be set to display widget,
        //but the delay is extremely small and unnoticable

        // -------- Handler for displaying now playing widget -------
        final Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent completed = new Intent("SONG_PREPARED");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(completed);
                if(musicBound && musicService.isQueueSet())
                    displayNowPlayingFragment();
            }
        };
        h.postDelayed(r, 10);
    }

    @Override
    public void onBackPressed() {
        if(queryHistory.isEmpty()) {
            super.onBackPressed();
        } else stepOutOfDirectory();
    }

    private void displayCurrentDirectoryTitle() {
        MusicDirectoryQuery query = queryHistory.getCurrentDirectoryQuery();
        if(query == null) return;
        // if(query.hasQueryTitle())
    }

    public void displayDirectory() {
        //set list view
        MusicItemListAdaptor adt = new MusicItemListAdaptor(this, musicItemList);
        musicListView.setAdapter(adt);
        displayCurrentDirectoryTitle();
    }

    private ArrayList<MusicItem> retrieveDirectoryFromStorage(MusicDirectoryQuery query) {
        if(query == null) return null;
        ArrayList<MusicItem> directory = null;
        switch(query.getMusicDirectoryType()) {
            case ARTIST:
                directory = LocalSongStorage.retrieveAllArtistsDirectory(MainActivity.this);
                break;
            case ALBUM:
                if(query.hasQueryTitle())
                    directory = LocalSongStorage.retrieveAllAlbumsFromArtistDirectory(MainActivity.this, query.getTitle());
                else
                    directory = LocalSongStorage.retrieveAllAlbumsDirectory(MainActivity.this);
                break;
            case SONG:
                if(query.hasQueryTitle()) {
                    directory = LocalSongStorage.retrieveSongsInAlbumDirectory(MainActivity.this, query.getTitle());
                    playlist = LocalSongStorage.retrieveAlbumPlaylist(MainActivity.this, query.getTitle());
                } else {
                    directory = LocalSongStorage.retrieveAllSongsDirectory(MainActivity.this);
                    playlist = LocalSongStorage.retrieveAllSongsPlaylist(MainActivity.this);
                }
                break;
        }
        return directory;
    }

    private void stepIntoDirectory(MusicDirectoryQuery query) {
        musicItemList = retrieveDirectoryFromStorage(query);
        queryHistory.addToHistory(query);
        displayDirectory();
    }

    private void stepOutOfDirectory() {
        MusicDirectoryQuery query = queryHistory.removeFromHistory();
        musicItemList = retrieveDirectoryFromStorage(query);
        displayDirectory();
    }

    @Override
    protected void onDestroy() {
        if(musicBound) {
            Intent playIntent = new Intent(this, MusicService.class);
            unbindService(musicConnection);
            //stopService(playIntent);
        }
//        musicService = null;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!musicBound) {
            Intent playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, 0);
            startService(playIntent);
            //startService(playIntent);
        }
            //displayNowPlayingFragment if musicbound
    }

    private void displayNowPlayingFragment() {
        FrameLayout lay = (FrameLayout) findViewById(R.id.nowPlayingWidget);
        lay.setVisibility(View.VISIBLE);

        //modify size to allow fragment space
        ListView lv = (ListView) findViewById(R.id.song_list);
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 430, getResources().getDisplayMetrics());
        lv.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent completed = new Intent("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(completed);

        if(musicBound && musicService.isQueueSet())
            displayNowPlayingFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(musicBound) {
//            unbindService(musicConnection);
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

    private ServiceConnection musicConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                    //get service
                    musicService = binder.getService();
                    //pass list
                    musicBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                }
            };

    public void musicItemPicked(View view) {
        if(musicItemList == null) return;
        int pos = Integer.valueOf(view.getTag().toString());
        MusicItem item = musicItemList.get(pos);
        //check if song and playlist set
        if(item.isSong()) {
            if (playlist != null) startNowPlaying(pos);
        } else if(item.getCategory() == MusicDirectoryType.ARTIST) {
            stepIntoDirectory(new MusicDirectoryQuery(MusicDirectoryType.ALBUM, item.getTitle()));
        } else if(item.getCategory() == MusicDirectoryType.ALBUM) {
            stepIntoDirectory(new MusicDirectoryQuery(MusicDirectoryType.SONG, item.getTitle()));
        }
    }

    private void startNowPlaying(int songIndex) {
        //start nowplaying activity
        //TODO modify to check if already musicBound
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        //bundle song picked into NowPlayingActivity
        intent.putExtra("songPicked", songIndex);
        intent.putParcelableArrayListExtra("playlist", playlist);
        musicService.setQueue(new SongQueue(
                songIndex, playlist));
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(String data) {
        Log.e("FRAGMENT", "FRAGMENT INTERACTION: " + data);
        if(data.compareTo("artist") == 0) {
            stepIntoDirectory(new MusicDirectoryQuery(MusicDirectoryType.ARTIST, null));
        } else if(data.compareTo("album") == 0) {
            stepIntoDirectory(new MusicDirectoryQuery(MusicDirectoryType.ALBUM, null));
        } else if(data.compareTo("song") == 0) {
            stepIntoDirectory(new MusicDirectoryQuery(MusicDirectoryType.SONG, null));
        } else if(data.compareTo("expand") == 0) {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            //bundle song picked into NowPlayingActivity
            MainActivity.this.startActivity(intent);
        } else if(data.compareTo("play song") == 0) {
            if(musicBound && !musicService.isPlaying()) musicService.play();
        } else if(data.compareTo("pause song") == 0) {
            if(musicBound && musicService.isPlaying()) musicService.pausePlayer();
        }
    }

    public boolean isMusicBound() {return  musicBound;}
    public MusicService getMusicService() {return musicService;}

}

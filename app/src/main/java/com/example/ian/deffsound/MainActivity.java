package com.example.ian.deffsound;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.ian.deffsound.musiclist.AlbumItem;
import com.example.ian.deffsound.musiclist.ArtistItem;
import com.example.ian.deffsound.musiclist.MusicItem;
import com.example.ian.deffsound.musiclist.SongItem;
import com.example.ian.deffsound.musiclist.MusicItemListAdaptor;
import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.StringTokenizer;


public class MainActivity extends ActionBarActivity implements NavigationWidget.OnFragmentInteractionListener,
    NowPlayingWidget.OnFragmentInteractionListener{

//    private MusicService musicService;
//    private Intent playIntent;
    private boolean musicBound = false;
    private MusicService musicService;
    private ArrayList<Song> playlist;
    private ArrayList<MusicItem> musicItemList;
    private ListView musicListView;
    //used to track user breadcrumbs
    private Stack<HistorySnapShot> history;
    private HistorySnapShot currentSnapShot =
            new HistorySnapShot(MusicCategory.SONG, "", null, MediaStore.Audio.Media.TITLE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playlist = new ArrayList<Song>();
        musicItemList = new ArrayList<MusicItem>();
        musicListView = (ListView)findViewById(R.id.song_list);
        //retrieve data

        //initialize stack and set to default list when history is empty
        history = new Stack<>();
        swapMusicItemList(getSongList("", null, MediaStore.Audio.Media.TITLE));
        //eventually stack should read from a temp file first before setting screen
    }

    @Override
    public void onBackPressed() {
        if(history.isEmpty()) {
            super.onBackPressed();
        } else popMusicItemListFromHistory();
    }

    private void pushMusicItemListToHistory(HistorySnapShot snapshot) {
        //check if snapshot identical to previous entry
        if(currentSnapShot.equals(snapshot)) return;
        history.push(currentSnapShot);
        currentSnapShot = snapshot;
        displayListTitle(snapshot);
    }

    private void popMusicItemListFromHistory() {
        if(history.isEmpty()) return;
        HistorySnapShot snapshot = history.pop();
        querySongs(snapshot);
        currentSnapShot = snapshot;
        displayListTitle(snapshot);
    }

    private void displayListTitle(HistorySnapShot snapshot) {
        String title = Arrays.toString(snapshot.getSelectionArgs());
        if(snapshot.getSelectionArgs()!= null) {
            title = title.substring(1, title.length() - 1);
            getSupportActionBar().setTitle(title);
        } else
            getSupportActionBar().setTitle(snapshot.getSnapshotTitle());
    }

    private void querySongs (HistorySnapShot snapshot) {
        switch (snapshot.getCategory()) {
            case ARTIST:
                swapMusicItemList(getArtists(snapshot.getSelection(),
                        snapshot.getSelectionArgs(), snapshot.getOrderBy()));
                break;
            case ALBUM:
                swapMusicItemList(getAlbums(snapshot.getSelection(),
                        snapshot.getSelectionArgs(), snapshot.getOrderBy()));
                break;
            case SONG:
                swapMusicItemList(getSongList(snapshot.getSelection(),
                        snapshot.getSelectionArgs(), snapshot.getOrderBy()));
                break;
        }
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
        } else if(musicService.isPlaying()) {
            FrameLayout lay = (FrameLayout) findViewById(R.id.nowPlayingWidget);
            lay.setVisibility(View.VISIBLE);

            //modify size to allow fragment space
            ListView lv = (ListView) findViewById(R.id.song_list);
            ViewGroup.LayoutParams params = lv.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 430, getResources().getDisplayMetrics());
            lv.setLayoutParams(params);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent completed = new Intent("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(completed);

//        //check if history needs to be updated
//        if(!history.isEmpty() && !currentSnapShot.equals(history.peek())) {
//            popMusicItemListFromHistory();
//            querySongs(currentSnapShot);
//        }
//        if(currentSnapShot.getSelectionArgs()!= null)
//            getSupportActionBar().setTitle(Arrays.toString(currentSnapShot.getSelectionArgs()));
//        else
//            getSupportActionBar().setTitle(currentSnapShot.getSnapshotTitle());
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

    public ArrayList<MusicItem> getSongList(String where, String[] whereParams, String orderBy) {
        //when displaying a list of songs, also save a playlist
        ArrayList<MusicItem> songList = new ArrayList<MusicItem>();
        playlist = new ArrayList<Song>();

        ContentResolver musicResolver = getContentResolver();
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
            int trackLengthColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(trackColumn);
                int thisTrackLength = musicCursor.getInt(trackLengthColumn);

                songList.add(new SongItem(thisTitle, thisArtist, thisAlbum, thisTrackLength));
                playlist.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, thisTrack));
            } while (musicCursor.moveToNext());
        }
        return songList;
    }

    public ArrayList<MusicItem> getArtists(String selection, String[] selectionArgs, String orderBy) {
        ArrayList<MusicItem> artistList = new ArrayList<MusicItem>();
        ContentResolver musicResolver = getContentResolver();
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
                        thisTitle.contains(artistList.get(
                                artistList.size() - 1).getTitle())) continue;
                artistList.add( new ArtistItem(thisTitle, Integer.valueOf(thisAlbums)) );
            } while (musicCursor.moveToNext());
        }
        //musicItemList = artistList;
        return artistList;
    }

    public ArrayList<MusicItem> getAlbums(String where, String[] whereParams, String orderBy) {
        ArrayList<MusicItem> albumList = new ArrayList<MusicItem>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, where, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int yearColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisTrack = musicCursor.getString(trackColumn);
                String thisYear = musicCursor.getString(yearColumn);
                albumList.add(new AlbumItem(thisTitle, Integer.valueOf(thisTrack), thisYear));
            } while (musicCursor.moveToNext());
        }
        //musicItemList = albumList;
        return albumList;
    }

    public void musicItemPicked(View view) {
        if(musicItemList == null) return;
        int pos = Integer.valueOf(view.getTag().toString());
        MusicItem item = musicItemList.get(pos);
        //check if song and playlist set
        if(item.isSong()) {
            if (playlist != null) startNowPlaying(pos);
        } else if(item.getCategory() == MusicCategory.ARTIST) {
            performMusicListSwap(MusicCategory.ALBUM, MediaStore.Audio.Albums.ARTIST + " LIKE ?",
                    new String[]{item.getTitle()},
                    MediaStore.Audio.Albums.FIRST_YEAR  + " DESC, " + MediaStore.Audio.Media.ALBUM);
        } else if(item.getCategory() == MusicCategory.ALBUM) {
            performMusicListSwap(MusicCategory.SONG, " AND " + MediaStore.Audio.Media.ALBUM + "=?",
                    new String[]{item.getTitle()}, MediaStore.Audio.Media.TRACK);
        }
    }

    private void performMusicListSwap(MusicCategory cat, String select, String[] selectArgs, String order) {
        HistorySnapShot snap = new HistorySnapShot(cat, select, selectArgs, order);
        pushMusicItemListToHistory(snap);
        querySongs(snap);
    }

    private void swapMusicItemList(ArrayList<MusicItem> newMusicItemList) {
        musicItemList = newMusicItemList;
        MusicItemListAdaptor adt = new MusicItemListAdaptor(this, musicItemList);
        musicListView.setAdapter(adt);
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
            performMusicListSwap(MusicCategory.ARTIST, "", null, MediaStore.Audio.Media.ARTIST);
        } else if(data.compareTo("album") == 0) {
            performMusicListSwap(MusicCategory.ALBUM, "", null, MediaStore.Audio.Media.ALBUM);
        } else if(data.compareTo("song") == 0) {
            performMusicListSwap(MusicCategory.SONG, "", null, MediaStore.Audio.Media.TITLE);
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

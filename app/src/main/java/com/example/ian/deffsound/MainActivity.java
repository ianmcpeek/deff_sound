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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ian.deffsound.songgroup.SongGroup;
import com.example.ian.deffsound.songgroup.SongGroupAdaptor;
import com.example.ian.deffsound.songview.Song;
import com.example.ian.deffsound.songview.SongAdaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends ActionBarActivity implements NavigationWidget.OnFragmentInteractionListener,
    NowPlayingWidget.OnFragmentInteractionListener{

//    private MusicService musicService;
//    private Intent playIntent;
    private boolean musicBound = false;
    private MusicService musicService;
    private ArrayList<Song> songList;
    private ArrayList<SongGroup> songGroupList;
    private ListView songView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Song>();
        songGroupList = new ArrayList<SongGroup>();
        songView = (ListView)findViewById(R.id.song_list);
        //retrieve data
        setListToSong();
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

    public ArrayList<Song> getSongList(String where, String[] whereParams, String orderBy) {

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0" + where;
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, orderBy);
        ArrayList<Song> songList = new ArrayList<Song>();

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
           // int albumArtColumn  = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            //int albumArtistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisTrack = musicCursor.getString(trackColumn);
                String thisAlbumArt = null;
                String thisAlbumArtist = null;
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum,
                        Integer.valueOf(thisTrack), thisAlbumArt, thisAlbumArtist));
            } while (musicCursor.moveToNext());
        }
        return songList;
    }

    public ArrayList<SongGroup> getArtists() {
        ArrayList<SongGroup> artistList = new ArrayList<SongGroup>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        //String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, orderBy);

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
                artistList.add(new SongGroup(thisId, thisTitle, null,
                        Integer.valueOf(thisAlbums), SongGroup.GroupType.ARTIST));
            } while (musicCursor.moveToNext());
        }
        songGroupList = artistList;
        return artistList;
    }

    public ArrayList<SongGroup> getAlbums(String where, String[] whereParams, String orderBy) {
        ArrayList<SongGroup> albumList = new ArrayList<SongGroup>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        //String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String[] selectionArgs = whereParams;
        Cursor musicCursor = musicResolver.query(musicUri, null, where, selectionArgs, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisTrack = musicCursor.getString(trackColumn);
                albumList.add(new SongGroup(thisId, thisTitle, null,
                        Integer.valueOf(thisTrack), SongGroup.GroupType.ALBUM));
            } while (musicCursor.moveToNext());
        }
        songGroupList = albumList;
        return albumList;
    }

    public void songPicked(View view) {
        //start nowplaying activity
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        //bundle song picked into NowPlayingActivity
        intent.putExtra("songPicked", Integer.parseInt(view.getTag().toString()));
        intent.putParcelableArrayListExtra("songList", songList);
        musicService.setQueue(new SongQueue(
                Integer.parseInt(view.getTag().toString()), songList));
        MainActivity.this.startActivity(intent);
//        if(musicBound) {
//            unbindService(musicConnection);
//        }
    }

    public void groupPicked(View view) {
        //Toast.makeText(getApplicationContext(), "Group picked: " + view.getTag().toString(), Toast.LENGTH_SHORT).show();
        if(songGroupList == null) return;
        SongGroup group = songGroupList.get(Integer.valueOf(view.getTag().toString()));
        switch (group.getType()) {
            case ARTIST:
                songGroupList = getAlbums(MediaStore.Audio.Albums.ARTIST + " LIKE ?",
                        new String[]{group.getTitle()},
                        MediaStore.Audio.Albums.FIRST_YEAR  + " DESC, " + MediaStore.Audio.Media.ALBUM);
                SongGroupAdaptor adt = new SongGroupAdaptor(this, songGroupList);
                songView.setAdapter(adt);
                break;
            case ALBUM:
                songList = getSongList(" AND " + MediaStore.Audio.Media.ALBUM + "=?",
                        new String[]{group.getTitle()},
                        MediaStore.Audio.Media.TRACK);
                SongAdaptor songadt = new SongAdaptor(this, songList);
                songView.setAdapter(songadt);
                break;
        }
    }


    @Override
    public void onFragmentInteraction(String data) {
        Log.e("FRAGMENT", "FRAGMENT INTERACTION: " + data);
        if(data.compareTo("artist") == 0) {
            setListToArtist();
        } else if(data.compareTo("song") == 0) {
            setListToSong();
        } else if(data.compareTo("album") == 0) {
            setListToAlbum();
        } else if(data.compareTo("expand") == 0) {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            //bundle song picked into NowPlayingActivity
            MainActivity.this.startActivity(intent);
        } else if(data.compareTo("play song") == 0) {
            if(!musicService.isPlaying()) musicService.play();
        } else if(data.compareTo("pause song") == 0) {
            if(musicService.isPlaying()) musicService.pausePlayer();
        }
    }

    public void setListToArtist() {
        SongGroupAdaptor adt = new SongGroupAdaptor(this, getArtists());
        songView.setAdapter(adt);
    }

    public void setListToSong() {
        songList = getSongList("", null, MediaStore.Audio.Media.TITLE);
        SongAdaptor songAdt = new SongAdaptor(this, songList);
        songView.setAdapter(songAdt);
    }

    public void setListToAlbum() {
        SongGroupAdaptor adt = new SongGroupAdaptor(this,
                 getAlbums("", null, MediaStore.Audio.Albums.ALBUM));
        songView.setAdapter(adt);
    }

    public boolean isMusicBound() {return  musicBound;}
    public MusicService getMusicService() {return musicService;}
}

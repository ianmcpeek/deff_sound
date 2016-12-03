package com.example.ian.deffsound;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class NowPlayingActivity extends AppCompatActivity {
    private MusicService musicService;
    private MusicServiceReciever reciever;
    private Intent playIntent;
    private SongQueue queue;
    private boolean musicBound = false;

    //widget controls
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        Intent intent = getIntent();
        ArrayList<Song> songList = intent.getParcelableArrayListExtra("songList");
        queue = new SongQueue(intent.getIntExtra("songPicked", 0),
                songList);
        reciever = new MusicServiceReciever();

        // ------- Grab Seekbar -------
        mSeekBar = (SeekBar) this.findViewById(R.id.seekBar);
        //make sure connection is established before wiring up seekbar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // for skipping through the song when holding skip bar
                if (fromUser) musicService.seekTo(progress);
                updateSeekBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateSeekBar() {
        mSeekBar.setProgress(musicService.getPosition());
    }

    @Override
         protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter =
                new IntentFilter("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).registerReceiver(reciever, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        //get int from queue
        //if not set, needs to set a timer or async task to play song
        if(musicService == null) Log.e("MUSIC SERVICE",
                "music service not set");
        //pass this tag into a new activity
        //musicService.setSong(getIntent().getIntExtra("songPicked", 0));
        //musicService.playSong();
        setScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
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
                    musicService.setQueue(queue);
                    musicBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                }
            };

    public void playSong(View view) {
        if(musicService == null) {
            Log.e("MUSIC SERVICE",
                    "music service not set");
            return;
        }
        ImageView playBtn = (ImageView)findViewById(R.id.playBtn);
        if(musicService.isPlaying()) {
            musicService.pausePlayer();
            playBtn.setImageResource(R.drawable.play);
        } else {
            musicService.play();
            playBtn.setImageResource(R.drawable.pause);
        }
    }
    private void setPause() {
        ImageView playBtn = (ImageView)findViewById(R.id.playBtn);
        playBtn.setImageResource(R.drawable.play);
    }

    public void nextSong(View view) {
        if(musicService == null) {
            Log.e("MUSIC SERVICE",
                    "music service not set");
            return;
        }
        if(!musicService.next()) setPause();
        else setScreen();
    }

    public void prevSong(View view) {
        if(musicService == null) {
            Log.e("MUSIC SERVICE",
                    "music service not set");
            return;
        }
       if( !musicService.prev()) setPause();
       else setScreen();
    }

    private void setScreen() {

        TextView artistView = (TextView) findViewById(R.id.artistTxt);
        TextView albumView = (TextView) findViewById(R.id.albumTxt);
        TextView songView = (TextView) findViewById(R.id.songTxt);
        artistView.setText(queue.getCurrentSong().getArtist());
        albumView.setText(queue.getCurrentSong().getAlbum());
        songView.setText(queue.getCurrentSong().getTitle());


    }

    private String timeFormat(int milliseconds) {
        int seconds = (milliseconds/1000) % 60;
        int minutes = (milliseconds/1000/60) % 60;
        //do time formating "00:07"
        //add check for hours
        return minutes + " : " + seconds;
    }

    private class MusicServiceReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            setScreen();
//            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
//            seekBar.setProgress(0);
//            seekBar.setMax(musicService.getDuration());
            TextView songDurationView = (TextView) findViewById(R.id.trackEndTxt);
            songDurationView.setText(timeFormat(musicService.getDuration()));
        }
    }
}

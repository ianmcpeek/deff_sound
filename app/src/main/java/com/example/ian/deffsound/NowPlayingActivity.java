package com.example.ian.deffsound;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;


public class NowPlayingActivity extends AppCompatActivity {
    private MusicService musicService;
    private MusicServiceReciever reciever;
    private Handler songProgressHandler;
    private Runnable songProgressRunnable;
    private boolean musicBound = false;

    //widget controls
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        Intent intent = getIntent();
        reciever = new MusicServiceReciever();

        ImageView downBtn = (ImageView) findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                songProgressHandler.removeCallbacks(songProgressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songProgressHandler.postDelayed(songProgressRunnable, 0);
            }
        });


        // -------- Create Handler For Updating Song Progress -------
        songProgressHandler = new Handler();
        songProgressRunnable = new Runnable() {
            @Override
            public void run() {
                int pos = musicService.getPosition();
                TextView songProgressView = (TextView) findViewById(R.id.trackProgressTxt);
                songProgressView.setText(timeFormat(pos));

                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
                seekBar.setProgress(pos);

                songProgressHandler.postDelayed(this, 1000);
            }
        };
    }

    private void updateSeekBar() {
        mSeekBar.setProgress(musicService.getPosition());
    }

    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        //musicService = null;
        if(musicBound)
            unbindService(musicConnection);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter =
                new IntentFilter("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).registerReceiver(reciever, filter);
        if(musicBound) {
            // && musicService.isPlaying()
            Log.e("HANDLER", "trying to play song before set");
            songProgressHandler.postDelayed(songProgressRunnable, 0);
            Intent completed = new Intent("SONG_PREPARED");
            LocalBroadcastManager.getInstance(this).sendBroadcast(completed);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
        songProgressHandler.removeCallbacks(songProgressRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!musicBound) {
            Intent playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(musicBound)
//            unbindService(musicConnection);
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
                    //musicService.setQueue(queue);
                    musicBound = true;
                    //setScreen();
                    reciever.onReceive(null, null);
                    setPlayerControls();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                }
            };


    //SONG PLAYER CONTROLS
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

    public void shuffleSongs(View view) {
        boolean shuffleOn = musicService.shuffle();
        ImageView shuffleBtn = (ImageView)findViewById(R.id.shuffleBtn);
        if(shuffleOn) {
            shuffleBtn.setImageResource(R.drawable.shuffle);
        } else {
            shuffleBtn.setImageResource(R.drawable.shuffle_off);
        }
    }

    public void repeatSongs(View view) {
       int repeatMode = musicService.repeat();
       ImageView repeatBtn = (ImageView)findViewById(R.id.repeatBtn);
       switch (repeatMode) {
           case 1:
               repeatBtn.setImageResource(R.drawable.repeat_queue);
               break;
           case 2:
               repeatBtn.setImageResource(R.drawable.repeat_song);
               break;
           default:
               repeatBtn.setImageResource(R.drawable.repeat_off);
               break;
       }
    }

    private void setPause() {
        ImageView playBtn = (ImageView)findViewById(R.id.playBtn);
        playBtn.setImageResource(R.drawable.play);
    }

    //VIEW UPDATES
    private void setScreen() {
        if(musicBound) {
            TextView artistView = (TextView) findViewById(R.id.artistTxt);
            TextView albumView = (TextView) findViewById(R.id.albumTxt);
            TextView songView = (TextView) findViewById(R.id.songTxt);
            artistView.setText(musicService.getCurrentSong().getArtist());
            albumView.setText(musicService.getCurrentSong().getAlbum());
            songView.setText(musicService.getCurrentSong().getTitle());
            songView.setSelected(true);
            songView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songView.setSingleLine(true);
        }
    }

    private void setPlayerControls() {
        if(musicBound) {
            ImageView playBtn = (ImageView) findViewById(R.id.playBtn);
            ImageView shuffleBtn = (ImageView) findViewById(R.id.shuffleBtn);
            ImageView repeatBtn = (ImageView) findViewById(R.id.repeatBtn);
            if(musicService.isPlaying()) {
                playBtn.setImageResource(R.drawable.pause);
            } else {
                playBtn.setImageResource(R.drawable.play);
            }
            if(musicService.isShuffled()) {
                shuffleBtn.setImageResource(R.drawable.shuffle);
            } else {
                shuffleBtn.setImageResource(R.drawable.shuffle_off);
            }
//            return 1; //queue
//            return 2; //song
//            return 0;//none
            switch(musicService.isRepeat()) {
                case 1:
                    repeatBtn.setImageResource(R.drawable.repeat_queue);
                    break;
                case 2:
                    repeatBtn.setImageResource(R.drawable.repeat_song);
                    break;
                case 0:
                    repeatBtn.setImageResource(R.drawable.repeat_off);
                    break;
            }
        }
    }

    private void startTrackingSongProgress() {
       // songProgressTimer.schedule();
    }

    private String timeFormat(int milliseconds) {
        String format;
        int seconds = (milliseconds/1000) % 60;
        int minutes = (milliseconds/1000/60) % 60;
        //do time formating "00:07"
        //format += minutes < 10 ? "0" + minutes : minutes;
        format = minutes + ":";
        format += seconds < 10 ? "0" + seconds : seconds;
        //add check for hours
        return format;
    }

    //Recieves notification of a prepared Song
    private class MusicServiceReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            setScreen();

            TextView songDurationView = (TextView) findViewById(R.id.trackEndTxt);
            songDurationView.setText(timeFormat(musicService.getDuration()));
            TextView songProgressView = (TextView) findViewById(R.id.trackProgressTxt);
            songProgressView.setText(timeFormat(0));

            //SEEKBAR
            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setProgress(0);
            seekBar.setMax(musicService.getDuration());

            //PROGRESS HANDLER
            songProgressHandler.postDelayed(songProgressRunnable, 1000);

            //Bitmap bm = BitmapFactory.decodeFile(musicService.getCurrentSong().getAlbumArt());
            //ImageView albumArt = (ImageView)findViewById(R.id.albumArt);
            //albumArt.setImageBitmap(bm);
            ContentResolver musicResolve = getContentResolver();
            Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            Cursor music =musicResolve.query(albumUri, null, MediaStore.Audio.Albums.ALBUM + " LIKE ?",
                    new String[]{musicService.getCurrentSong().getAlbum()}, null);



            music.moveToFirst();            //i put only one song in my external storage to keep things simple
            int x=music.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            String thisArt = music.getString(x);
            //Toast.makeText(getApplicationContext(), "Song picked: " + thisArt, Toast.LENGTH_SHORT).show();

            if(thisArt!= null) {
                Bitmap bm= BitmapFactory.decodeFile(thisArt);
                ImageView image=(ImageView)findViewById(R.id.albumArt);
                //image.setScaleType(ImageView.ScaleType.FIT_XY);
                image.setImageBitmap(bm);
            } else {
                ImageView image=(ImageView)findViewById(R.id.albumArt);
                image.setImageResource(R.drawable.no_album_art);
            }
        }
    }
}

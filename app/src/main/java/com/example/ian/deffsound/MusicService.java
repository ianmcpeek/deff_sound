package com.example.ian.deffsound;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.ian.deffsound.songview.Song;

import java.io.IOException;

/**
 * Created by Ian on 8/28/2016.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    //media player
    private MediaPlayer player;
    //song list
    private SongQueue queue;
    //current position
    //bind to main activity
    private final IBinder bind = new MusicBinder();

    private boolean autoplay = true;


    private NoisyAudioReciever noisyAudioReciever;

    private class NoisyAudioReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pausePlayer();
            }
        }
    }

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {

                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        if(player.isPlaying()) {
                            unregisterReceiver(noisyAudioReciever);
                        }
                        player.pause();
                    }
                }
            };

    private boolean hasAudioFocus() {
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("SERVICE", "Music Service has been created");
        player = new MediaPlayer();
        initPlayer();

        noisyAudioReciever = new NoisyAudioReciever();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public void initPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return bind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //player.stop();
        //player.release();
        return false; //may need refactoring for errors
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(queue.nextSong() != null) {
            playSong();
        } else {
            queue.resetQueue();
            mp.reset();
            autoplay = false;
            prepareSong();
            mp.prepareAsync();
            unregisterReceiver(noisyAudioReciever);
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //broadcast
        Intent completed = new Intent("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(completed);
        //start playback

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyAudioReciever, intentFilter);
        if(hasAudioFocus()) {
            if(autoplay) mp.start();
        }
    }

    public void setQueue(SongQueue songQueue) {
        this.queue = songQueue;
        playSong();
    }

    public void setSong(int idx) {
        queue.setCurrentSong(idx);
    }

    public void prepareSong() {
        //get song
        Song songPlaying = queue.getCurrentSong();
        //get id
        long currentSongId = songPlaying.getId();
        //set URI
        Uri trackUri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currentSongId);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    private void playSong() {
        player.reset();
        prepareSong();
        autoplay = true;
        player.prepareAsync();
    }

     /***************************
     |  CORE PLAYER CONTROLS    |
     ***************************/

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
        unregisterReceiver(noisyAudioReciever);
    }

    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    public void play() {
        player.start();
        //need playlist over variable to reset on playlist end
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyAudioReciever, intentFilter);
        if(hasAudioFocus()) {
            player.start();
        }
    }

    public boolean next() {
        if(queue.nextSong()!=null)
            playSong();
        else {
            queue.resetQueue();
            player.reset();
            autoplay = false;
            prepareSong();
            player.prepareAsync();
            return false;
        }
        return true;
    }

    public boolean prev() {
        if(player.getCurrentPosition() > 3000) playSong();
        else if(queue.prevSong()!=null)
            playSong();
        else if(player.isPlaying()) {
            playSong();
            return false;
        } else {
            autoplay = false;
            queue.resetQueue();
            player.reset();
            prepareSong();
            player.prepareAsync();
            return false;
        }
        return true;
    }

    public boolean isShuffled() {return queue.isShuffled();}

    public boolean shuffle() {
        queue.toggleShuffle();
        return queue.isShuffled();
    }

    public int isRepeat() {
        if(queue.isRepeatQueue()) {
            return 1; //queue
        } else if(queue.isRepeatSong()) {
            return 2; //song
        } else {
            return 0;//none
        }
    }

    public int repeat() {
        queue.toggleRepeat();
        return isRepeat();
    }

    public Song getCurrentSong() {
        return queue.getCurrentSong();
    }

    public boolean isQueueSet() {
        return queue != null;
    }
}

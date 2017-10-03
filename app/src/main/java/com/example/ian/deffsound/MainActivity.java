package com.example.ian.deffsound;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MusicItemListFragment.OnFragmentInteractionListener,
    NowPlayingFragment.OnFragmentInteractionListener {

//    private MusicService musicService;
//    private Intent playIntent;
    private boolean musicBound = false;
    private MusicService musicService;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_person_accent_24dp,
            R.drawable.ic_album_accent_24dp,
            R.drawable.ic_audiotrack_accent_24dp
    };
    ArrayList<MusicItemListFragment> musicItemListFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicItemListFragments = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                musicItemListFragments.get(viewPager.getCurrentItem()).resetHistory();
                musicItemListFragments.get(viewPager.getCurrentItem()).displayDirectory();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

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

    private void setupTabIcons() {
        TextView artistTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        artistTab.setText("ARTIST");
        artistTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_person_accent_24dp, 0, 0);
        tabLayout.getTabAt(0).setCustomView(artistTab);

        TextView albumTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        albumTab.setText("ALBUM");
        albumTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_album_accent_24dp, 0, 0);
        tabLayout.getTabAt(1).setCustomView(albumTab);

        TextView songTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        songTab.setText("SONG");
        songTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audiotrack_accent_24dp, 0, 0);
        tabLayout.getTabAt(2).setCustomView(songTab);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdaptor adaptor = new ViewPagerAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(adaptor);
    }

    class ViewPagerAdaptor extends FragmentStatePagerAdapter {

        ViewPagerAdaptor(FragmentManager manager) {
            super(manager);
            musicItemListFragments.add(0, MusicItemListFragment.newInstance("Artist"));
            musicItemListFragments.add(1, MusicItemListFragment.newInstance("Album"));
            musicItemListFragments.add(2, MusicItemListFragment.newInstance("Song"));
        }

        @Override
        public Fragment getItem(int position) {
            Log.v("TAB", MusicDirectoryType.getStringFromInt(position));
            musicItemListFragments.set(position,
                MusicItemListFragment.newInstance(
                    MusicDirectoryType.getStringFromInt(position)
                )
            );
            return musicItemListFragments.get(position);
        }

        @Override
        public int getCount() {
            return musicItemListFragments.size();
        }
    }

    @Override
    public void onBackPressed() {
        if(MusicDirectoryQueryHistory.isEmpty()) {
            super.onBackPressed();
        } else musicItemListFragments.get(viewPager.getCurrentItem()).stepOutOfDirectory();
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
        }
    }

    private void displayNowPlayingFragment() {
        FrameLayout lay = (FrameLayout) findViewById(R.id.nowPlayingFragment);
        if(lay.getVisibility() == View.VISIBLE) return;
        lay.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = params.height - lay.getMeasuredHeight();
        viewPager.setLayoutParams(params);
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

    private void startNowPlaying() {
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(String data) {
        if(data.compareTo("expand") == 0) {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            MainActivity.this.startActivity(intent);
        } else if(data.compareTo("play song") == 0) {
            if(musicBound && !musicService.isPlaying()) musicService.play();
        } else if(data.compareTo("pause song") == 0) {
            if(musicBound && musicService.isPlaying()) musicService.pausePlayer();
        }
    }

    @Override
    public void onFragmentInteraction(String data, int pos) {
        if(data.compareTo("start_new_playlist") == 0) {
            musicService.setActivePlaylist(musicItemListFragments.get(viewPager.getCurrentItem()).getPlaylist(), pos);
            startNowPlaying();
        } else if(data.compareTo("resume_song") == 0) {
            startNowPlaying();
        } else if(data.compareTo("add_up_next") == 0) {
            if (musicBound) {
                Song song = musicItemListFragments.get(viewPager.getCurrentItem()).getPlaylist().get(pos);
                musicService.addUpNext(song);
                //make toast
                Context context = getApplicationContext();
                CharSequence text = song.getTitle() + " added Up Next!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                if(!musicService.isQueueSet()) {

                }
            }
        }
    }

    public boolean isMusicBound() {return  musicBound;}
    public MusicService getMusicService() {return musicService;}

}

package com.example.ian.deffsound;

import android.content.ComponentName;
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MusicItemListFragment.OnFragmentInteractionListener,
    NowPlayingWidget.OnFragmentInteractionListener {

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
            //startService(playIntent);
        }
            //displayNowPlayingFragment if musicbound
    }

    private void displayNowPlayingFragment() {
        FrameLayout lay = (FrameLayout) findViewById(R.id.nowPlayingWidget);
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

    private void startNowPlaying(int songIndex) {
        //start nowplaying activity
        //TODO modify to check if already musicBound
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        //bundle song picked into NowPlayingActivity
        intent.putExtra("songPicked", songIndex);
        intent.putParcelableArrayListExtra("playlist", musicItemListFragments.get(viewPager.getCurrentItem()).getPlaylist());
        musicService.setQueue(new SongQueue(
                songIndex, musicItemListFragments.get(viewPager.getCurrentItem()).getPlaylist()));
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(String data) {
        if(data.compareTo("expand") == 0) {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            //bundle song picked into NowPlayingActivity
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
            startNowPlaying(pos);
        }
    }

    public boolean isMusicBound() {return  musicBound;}
    public MusicService getMusicService() {return musicService;}

}

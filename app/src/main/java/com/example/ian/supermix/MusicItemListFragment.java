package com.example.ian.supermix;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ian.supermix.musiclist.MusicItem;
import com.example.ian.supermix.musiclist.MusicItemListAdaptor;
import com.example.ian.supermix.musiclist.SongItem;
import com.example.ian.supermix.songqueue.NowPlayingController;
import com.example.ian.supermix.songview.Song;

import java.util.ArrayList;

public class MusicItemListFragment extends Fragment {
    private ArrayList<Song> playlist;
    private ArrayList<MusicItem> musicItemList;
    private ListView musicListView;
    // private MusicDirectoryQueryHistory queryHistory;

    private OnFragmentInteractionListener mListener;
    private Context fragmentContext;

    private static final String ARG_PARAM1 = "argMusicDirectoryType";
    private static final int READ_EXTERNAL_STORAGE = 400;
    private boolean readPermissionGranted;

    private MusicDirectoryType directoryType;

    public static MusicItemListFragment newInstance(String param1) {
        MusicItemListFragment fragment = new MusicItemListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public MusicItemListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            directoryType = MusicDirectoryType.parseString(getArguments().getString(ARG_PARAM1));
        }

        playlist = null;
        musicItemList = new ArrayList<>();
        readPermissionGranted = false;

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
        }
    }

    public static MusicItemListFragment newInstance() {
        MusicItemListFragment fragment = new MusicItemListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_item_list, container, false);

        musicListView = (ListView)view.findViewById(R.id.song_list);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicItemPicked(view, i);
            }
        });
        musicListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                addUpNext(i);
                return true;
            }
        });
        return view;
    }

    private void addUpNext(int pos) {
        if(musicItemList == null) return;
        MusicItem item = musicItemList.get(pos);
        if(item.isSong()) {
            if (mListener != null) {
                mListener.onFragmentInteraction("add_up_next", pos);
            }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fragmentContext = activity;
        try {
            mListener = (MusicItemListFragment.OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(directoryType != null) {

            MusicDirectoryQuery query = MusicDirectoryQueryHistory.getQuery();
            if(query == null) {
                resetHistory();
            } else {
                musicItemList = retrieveDirectoryFromStorage(query);
            }

            displayDirectory();
        }

    }

    public void resetHistory() {
        Log.v("HISTORY", "resetting history");
        MusicDirectoryQuery defaultQuery = new MusicDirectoryQuery(directoryType, directoryType, null);
        MusicDirectoryQueryHistory.resetQueryHistory(defaultQuery);
        musicItemList = retrieveDirectoryFromStorage(defaultQuery);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String data, int pos);
    }

    public void displayDirectory() {
        //set list view
        MusicItemListAdaptor adt = new MusicItemListAdaptor(getActivity(), musicItemList);
        musicListView.setAdapter(adt);
        displayCurrentDirectoryTitle();
    }

    private void displayCurrentDirectoryTitle() {
        MusicDirectoryQuery query = MusicDirectoryQueryHistory.getQuery();
        //if(query == null) return;
        // if(query.hasQueryTitle())
    }

    public ArrayList<MusicItem> retrieveDirectoryFromStorage(MusicDirectoryQuery query) {
        if(query == null) return null;
        ArrayList<MusicItem> directory = null;
        if(fragmentContext == null) {
            return null;
        }

        switch(query.getMusicDirectoryType()) {
            case ARTIST:
                directory = LocalSongStorage.retrieveAllArtistsDirectory(fragmentContext);
                break;
            case ALBUM:
                if(query.hasQueryTitle())
                    directory = LocalSongStorage.retrieveAllAlbumsFromArtistDirectory(fragmentContext, query.getTitle());
                else
                    directory = LocalSongStorage.retrieveAllAlbumsDirectory(fragmentContext);
                break;
            case SONG:
                if(query.hasQueryTitle()) {
                    directory = LocalSongStorage.retrieveSongsInAlbumDirectory(fragmentContext, query.getTitle());
                    playlist = LocalSongStorage.retrieveAlbumPlaylist(fragmentContext, query.getTitle());
                } else {
                    directory = LocalSongStorage.retrieveAllSongsDirectory(fragmentContext);
                    playlist = LocalSongStorage.retrieveAllSongsPlaylist(fragmentContext);
                }
                break;
        }
        return directory;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readPermissionGranted = true;
                } else {
                    Log.v("MUSICITEMLIST", "permission denied");
                }
                break;
        }
    }

    public void stepIntoDirectory(MusicDirectoryQuery query) {
        musicItemList = retrieveDirectoryFromStorage(query);
        if(musicItemList != null) {
            MusicDirectoryQueryHistory.addToHistory(query);
            displayDirectory();
        }
    }

    public void stepOutOfDirectory() {
        MusicDirectoryQuery query = MusicDirectoryQueryHistory.removeFromHistory();
        if(query != null) {
            musicItemList = retrieveDirectoryFromStorage(query);
            displayDirectory();
        }
    }

    public void musicItemPicked(View view, int pos) {
        if(musicItemList == null) return;
        MusicItem item = musicItemList.get(pos);
        //check if song and playlist set
        if(item.isSong()) {
            if (playlist != null) {
                //check if song already playing
                Song currentSong = NowPlayingController.getInstance().getAvailableSong();
                if (currentSong != null) {
                    SongItem songItem = (SongItem) item;
                    if (songItem.getTitle().equals(currentSong.getTitle()) &&
                            songItem.getAlbum().equals(currentSong.getAlbum()) &&
                            songItem.getArtist().equals(currentSong.getArtist())) {
                        mListener.onFragmentInteraction("resume_song", pos);
                        return;
                    }
                }
                if (mListener != null) {
                    mListener.onFragmentInteraction("start_new_playlist", pos);
                }
            }
        } else if(item.getCategory() == MusicDirectoryType.ARTIST) {
            MusicDirectoryQuery query = new MusicDirectoryQuery(directoryType, MusicDirectoryType.ALBUM, item.getTitle());
            stepIntoDirectory(query);
        } else if(item.getCategory() == MusicDirectoryType.ALBUM) {
            MusicDirectoryQuery query = new MusicDirectoryQuery(directoryType, MusicDirectoryType.SONG, item.getTitle());
            stepIntoDirectory(query);
        }
    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }
}

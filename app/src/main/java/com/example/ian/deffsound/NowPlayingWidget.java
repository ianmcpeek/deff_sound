package com.example.ian.deffsound;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NowPlayingWidget.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NowPlayingWidget#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NowPlayingWidget extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean isPlaying = false;
    private MusicFragmentReciever reciever;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NowPlayingWidget.
     */
    // TODO: Rename and change types and number of parameters
    public static NowPlayingWidget newInstance(String param1, String param2) {
        NowPlayingWidget fragment = new NowPlayingWidget();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NowPlayingWidget() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        reciever = new MusicFragmentReciever();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_now_playing_widget, container, false);
        ImageView expandBtn = (ImageView) v.findViewById(R.id.fragmentUpBtn);
        expandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentInteraction("expand");
                }
            }
        });
        ImageView playBtn = (ImageView) v.findViewById(R.id.fragmentPlayBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    ImageView playBtn = (ImageView) getActivity().findViewById(R.id.fragmentPlayBtn);
                    if(isPlaying) {
                        mListener.onFragmentInteraction("pause song");
                        isPlaying = false;
                        playBtn.setImageResource(R.drawable.play);
                    } else {
                        mListener.onFragmentInteraction("play song");
                        isPlaying = true;
                        playBtn.setImageResource(R.drawable.pause);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter =
                new IntentFilter("SONG_PREPARED");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reciever, filter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String data);
    }


        //Recieves notification of a prepared Song
    private class MusicFragmentReciever extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity act = (MainActivity) getActivity();
                if(act == null) return;
                if(act.isMusicBound()) {
                    MusicService srv = act.getMusicService();
                    TextView song = (TextView) act.findViewById(R.id.fragSongTxt);
                    TextView artist = (TextView) act.findViewById(R.id.fragArtistTxt);
                    if(srv.isQueueSet()) {
                        song.setText(srv.getCurrentSong().getTitle());
                        artist.setText(srv.getCurrentSong().getArtist() + " - " +
                                srv.getCurrentSong().getAlbum());
                    }
                    ImageView playBtn = (ImageView) act.findViewById(R.id.fragmentPlayBtn);
                    if(srv.isPlaying()) {
                        playBtn.setImageResource(R.drawable.pause);
                        isPlaying = true;
                    } else {
                        playBtn.setImageResource(R.drawable.play);
                        isPlaying = false;
                    }
                }
            }
    }
}

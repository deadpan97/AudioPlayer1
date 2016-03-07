package dirk.net.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by PLTW on 2/23/2016.
 */
public class MusicListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Context context;
    private ListView songView;
    private ArrayList songList;
    MediaPlayer mp;
    SeekBar seek;
    final int SKIP_FORWARD = 1000;
    final int SKIP_BACKWARD = 1000;
    private static final String TAG = "MusicListFragment";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MusicListFragment newInstance(int sectionNumber) {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MusicListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);
        context = getActivity().getApplicationContext();

        //retrieve list view
        songView = (ListView) rootView.findViewById(R.id.song_listview);
        //instantiate list
        songList = new ArrayList<Song>();
        //get songs from device
        getSongList();
        //sort alphabetically by title
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        //create and set adapter
        SongAdapter songAdt = new SongAdapter(context, songList);
        songView.setAdapter(songAdt);


        return rootView;
    }

    //method to retrieve song info from device
    public void getSongList() {
        //query external audio
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }


    public void onClicked(View view) {
        mp = MediaPlayer.create(getActivity(), R.raw.when_a_man_loves_a_woman_2003);
        final Button play_button = (Button)getActivity().findViewById(R.id.pause_play_button);
        final Button stop_button = (Button)getActivity().findViewById(R.id.stop_button);
        final Button fskip_button = (Button)getActivity().findViewById(R.id.skip_forward);
        final Button bskip_button = (Button)getActivity().findViewById(R.id.skip_backward);
        final SeekBar seekBar = (SeekBar)getActivity().findViewById(R.id.seekBar);

        final Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(mp != null){
                    seekBar.setMax(mp.getDuration());
                    int mCurrentPosition = mp.getCurrentPosition();
                    seekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        play_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    Log.v(TAG, "mp stopped");
                    play_button.setText("►");
                } else {
                    Log.v(TAG, "Playing sound...");
                    mp.start();
                    play_button.setText("❚❚");
                }
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mp.getCurrentPosition() > 0) {
                    mp.pause();
                    mp.seekTo(0);
                    play_button.setText("►");
                } else {
                    Log.v(TAG, "It's not playing, doofus");
                }
            }
        });
        fskip_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mp.isPlaying() || mp.getCurrentPosition() != 0 && mp.getCurrentPosition() + SKIP_FORWARD < mp.getDuration()) {
                    mp.seekTo(mp.getCurrentPosition() + SKIP_FORWARD);
                    play_button.setText("►");
                } else {
                    Log.v(TAG, "stahp skipping");
                }
            }
        });

        bskip_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mp.isPlaying() || mp.getCurrentPosition() != 0 && mp.getCurrentPosition() - SKIP_BACKWARD > 0) {
                    mp.seekTo(mp.getCurrentPosition() - SKIP_BACKWARD);
                    play_button.setText("►");
                } else {
                    Log.v(TAG, "stahp skipping");
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mp != null && fromUser) {
                    mp.seekTo(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    public class SongAdapter extends BaseAdapter {

        private ArrayList<Song> songs;
        private LayoutInflater songInf;

        public SongAdapter(Context c, ArrayList<Song> theSongs) {
            songs = theSongs;
            songInf = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //map to song layout
            LinearLayout songLay = (LinearLayout) songInf.inflate
                    (R.layout.song, parent, false);
            //get title and artist views
            TextView songView = (TextView) songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
            //get song using position
            Song currSong = songs.get(position);
            //get title and artist strings
            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            //set position as tag
            songLay.setTag(position);
            return songLay;
        }

    }

}

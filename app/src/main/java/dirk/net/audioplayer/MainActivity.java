package dirk.net.audioplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    ToggleButton mToggleButton;
    private ArrayList<Song> songList;
    private ListView songView;
    MediaPlayer mp;
    SeekBar seek;
    final int SKIP_FORWARD = 1000;
    final int SKIP_BACKWARD = 1000;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    // commit test change
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();
        //booty
    }
    public void getSongList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
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
        mp = MediaPlayer.create(this, R.raw.when_a_man_loves_a_woman_2003);
        final Button play_button = (Button)this.findViewById(R.id.pause_play_button);
        final Button stop_button = (Button)this.findViewById(R.id.stop_button);
        final Button fskip_button = (Button)this.findViewById(R.id.skip_forward);
        final Button bskip_button = (Button)this.findViewById(R.id.skip_backward);
        final SeekBar seekBar = (SeekBar)this.findViewById(R.id.seekBar);

        final Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
        MainActivity.this.runOnUiThread(new Runnable() {

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
                if (mp.isPlaying() || mp.getCurrentPosition() != 0) {
                    mp.seekTo(mp.getCurrentPosition() + SKIP_FORWARD);
                    play_button.setText("►");
                } else {
                    Log.v(TAG, "stahp skipping");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mp != null && fromUser){
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

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}

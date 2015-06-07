package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    protected static android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int position = Integer.parseInt(getIntent().getStringExtra(Intent.EXTRA_TEXT));

        // set title in the actionbar
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);
        actionBar.setSubtitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        setContentView(R.layout.activity_player);
    }
}

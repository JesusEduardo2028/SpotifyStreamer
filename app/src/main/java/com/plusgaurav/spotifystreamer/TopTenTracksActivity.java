package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TopTenTracksActivity extends AppCompatActivity {

    public static String[] artistInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get Intent
        artistInfo = getIntent().getExtras().getStringArray(Intent.EXTRA_TEXT);

        setContentView(R.layout.activity_top_ten_tracks);

        // set subtitle in the actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setSubtitle(artistInfo[1]);
    }
}

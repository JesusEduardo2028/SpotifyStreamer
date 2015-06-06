package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TopTenTracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);
        Intent intent = getIntent();
        String[] artistInfo = intent.getStringArrayExtra(Intent.EXTRA_TEXT);

        // set subtitle in the actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setSubtitle(artistInfo[1]);
    }
}

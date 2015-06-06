package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        String[] trackInfo = intent.getStringArrayExtra(Intent.EXTRA_TEXT);

        // set title in the actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(trackInfo[0]);
        actionBar.setSubtitle(trackInfo[1]);
    }
}

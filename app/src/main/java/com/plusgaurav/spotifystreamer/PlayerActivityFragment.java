package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerActivityFragment extends Fragment {

    Boolean isPlaying;
    MediaPlayer mediaPlayer;
    at.markushi.ui.CircleButton playButton;
    private ProgressBar spinner;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // Progress Bar
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar3);
        spinner.setVisibility(View.VISIBLE);

        // get trackInfo
        String[] trackInfo = getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

        // artist name
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(trackInfo[4]);

        // album name
        TextView albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        albumNameView.setText(trackInfo[1]);

        // album art
        ImageView trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
        String url = trackInfo[2];
        Picasso.with(rootView.getContext()).load(url).placeholder(R.drawable.ic_album).error(R.drawable.ic_album).into(trackImageView);

        // track Name
        TextView trackNameView = (TextView) rootView.findViewById(R.id.trackName);
        trackNameView.setText(trackInfo[0]);

        // Play music
        isPlaying = false;
        playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
        final String previewUrl = trackInfo[5];
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //spinner.setVisibility(View.GONE);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isPlaying) {
                            mediaPlayer.start();
                            playButton.setImageResource(R.drawable.ic_pause);
                            isPlaying = true;
                        } else {
                            mediaPlayer.pause();
                            isPlaying = false;
                            playButton.setImageResource(R.drawable.ic_play);
                        }

                    }
                });
            }
        });


        return rootView;
    }
}

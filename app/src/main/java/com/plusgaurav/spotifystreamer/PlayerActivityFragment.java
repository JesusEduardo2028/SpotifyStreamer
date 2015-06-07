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

    View rootView;
    Boolean isPlaying;
    int position;
    MediaPlayer mediaPlayer;
    private ProgressBar spinner;
    at.markushi.ui.CircleButton prevButton;
    at.markushi.ui.CircleButton playButton;
    at.markushi.ui.CircleButton nextButton;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // if song running -> cancel it
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }

        // Progress Bar
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar3);
        spinner.setVisibility(View.VISIBLE);

        // get position
        position = Integer.parseInt(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));

        // setup ui
        setUi(position);

        // prepare music
        prepareMusic(position);

        // prev button
        prevButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                position = position - 1;
                if (position < 0) {
                    position = 0;
                }
                setUi(position);
                playButton.setImageResource(R.drawable.ic_play);
                mediaPlayer.reset();
                prepareMusic(position);
            }
        });

        nextButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                position = position + 1;
                if (position > 9) {
                    position = 0;
                }
                setUi(position);
                playButton.setImageResource(R.drawable.ic_play);
                mediaPlayer.reset();
                prepareMusic(position);
            }
        });

        return rootView;
    }


    private void setUi(int position) {

        // update title
        // set title in the actionbar
        android.support.v7.app.ActionBar actionBar = PlayerActivity.actionBar;
        assert actionBar != null;
        actionBar.setTitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);
        actionBar.setSubtitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // artist name
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackArtist);

        // album name
        TextView albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        albumNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // album art
        ImageView trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
        String url = TopTenTracksActivityFragment.topTenTrackList.get(position).trackImageLarge;
        Picasso.with(rootView.getContext()).load(url).placeholder(R.drawable.ic_album).error(R.drawable.ic_album).into(trackImageView);

        // track Name
        TextView trackNameView = (TextView) rootView.findViewById(R.id.trackName);
        trackNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);

    }

    private void prepareMusic(int position) {

        final String previewUrl = TopTenTracksActivityFragment.topTenTrackList.get(position).trackPreviewUrl;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // play button
        playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
        isPlaying = false;

        // grey out until prepare
        playButton.setClickable(false);
        playButton.setImageResource(R.drawable.ic_stop);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                spinner.setVisibility(View.GONE);

                // restore button
                playButton.setClickable(true);

                mediaPlayer.start();
                playButton.setImageResource(R.drawable.ic_pause);
                isPlaying = true;
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

    }
}

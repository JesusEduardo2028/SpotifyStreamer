package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import wseemann.media.FFmpegMediaPlayer;

public class PlayerActivityFragment extends Fragment {

    View rootView;
    Boolean isPlaying;
    int position;
    protected static FFmpegMediaPlayer freePlayer;
    private ProgressBar spinner;
    at.markushi.ui.CircleButton prevButton;
    at.markushi.ui.CircleButton playButton;
    at.markushi.ui.CircleButton nextButton;
    protected String trackUrl;
    protected static Player premiumPlayer;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // if song running -> cancel it
        if (freePlayer != null) {
            freePlayer.reset();
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
                if (freePlayer != null) {
                    freePlayer.reset();
                }
                prepareMusic(position);
            }
        });

        // next button
        nextButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                position = position + 1;
                if (position > TopTenTracksActivityFragment.topTenTrackList.size() - 1) {
                    position = 0;
                }
                setUi(position);
                playButton.setImageResource(R.drawable.ic_play);
                if (freePlayer != null) {
                    freePlayer.reset();
                }
                prepareMusic(position);
            }
        });

        return rootView;
    }


    private void setUi(int position) {

        // set title in the actionbar
        final android.support.v7.app.ActionBar actionBar = PlayerActivity.actionBar;
        assert actionBar != null;
        actionBar.setTitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);
        actionBar.setSubtitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // update background image and album art and theme
        String url = TopTenTracksActivityFragment.topTenTrackList.get(position).trackImageLarge;

        // set background image
        ImageView backgroundImageView = (ImageView) rootView.findViewById(R.id.backgroundImage);
        Picasso.with(rootView.getContext()).load(url).transform(new BlurTransformation(rootView.getContext(), 25)).into(backgroundImageView);

        Picasso.with(rootView.getContext()).load(url).placeholder(R.drawable.ic_album).error(R.drawable.ic_album).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                // set track image
                ImageView trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
                trackImageView.setImageBitmap(null);
                trackImageView.setImageBitmap(bitmap);

                // get prominent colors and set it to ui elements
                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        actionBar.setBackgroundDrawable(new ColorDrawable(palette.getMutedColor(android.R.color.black)));
                        getActivity().getWindow().setStatusBarColor(palette.getMutedColor(android.R.color.black));
                        getActivity().getWindow().setNavigationBarColor(palette.getMutedColor(android.R.color.black));

                        at.markushi.ui.CircleButton prevButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.prevButton);
                        prevButton.setColor(palette.getMutedColor(android.R.color.black));

                        at.markushi.ui.CircleButton playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
                        playButton.setColor(palette.getMutedColor(android.R.color.black));

                        at.markushi.ui.CircleButton nextButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.nextButton);
                        nextButton.setColor(palette.getMutedColor(android.R.color.black));
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        // artist name
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackArtist);

        // album name
        TextView albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        albumNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // track Name
        TextView trackNameView = (TextView) rootView.findViewById(R.id.trackName);
        trackNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);

    }

    private void prepareMusic(int position) {

        // check for free or premium user
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userType = prefs.getString(getString(R.string.user_type_key),
                getString(R.string.user_type_key));

        if (userType.equals("free")) {
            freePlayer = new FFmpegMediaPlayer();
            freePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            trackUrl = TopTenTracksActivityFragment.topTenTrackList.get(position).trackPreviewUrl;
            try {
                freePlayer.setDataSource(trackUrl);
                freePlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // play button
            playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
            isPlaying = false;

            // grey out until prepare
            playButton.setClickable(false);
            playButton.setImageResource(R.drawable.ic_stop);

            freePlayer.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    spinner.setVisibility(View.GONE);

                    // restore button
                    playButton.setClickable(true);

                    freePlayer.start();
                    playButton.setImageResource(R.drawable.ic_pause);
                    isPlaying = true;
                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlaying) {
                                freePlayer.start();
                                playButton.setImageResource(R.drawable.ic_pause);
                                isPlaying = true;
                            } else {
                                freePlayer.pause();
                                isPlaying = false;
                                playButton.setImageResource(R.drawable.ic_play);
                            }
                        }
                    });
                }
            });
        } else {
            // play button
            playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
            isPlaying = false;

            // grey out until prepare
            playButton.setClickable(false);
            playButton.setImageResource(R.drawable.ic_stop);

            trackUrl = TopTenTracksActivityFragment.topTenTrackList.get(position).trackUrl;
            Config playerConfig = new Config(getActivity(), SearchArtistActivity.getAccessToken(), SearchArtistActivity.CLIENT_ID);
            premiumPlayer = Spotify.getPlayer(playerConfig, getActivity(), new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {
                    spinner.setVisibility(View.GONE);

                    // restore button
                    playButton.setClickable(true);

                    premiumPlayer.play(trackUrl);
                    playButton.setImageResource(R.drawable.ic_pause);
                    isPlaying = true;
                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlaying) {
                                premiumPlayer.play(trackUrl);
                                playButton.setImageResource(R.drawable.ic_pause);
                                isPlaying = true;
                            } else {
                                premiumPlayer.pause();
                                isPlaying = false;
                                playButton.setImageResource(R.drawable.ic_play);
                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        }
    }
}

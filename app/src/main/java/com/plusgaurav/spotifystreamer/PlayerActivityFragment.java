package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

import wseemann.media.FFmpegMediaPlayer;

public class PlayerActivityFragment extends Fragment {

    View rootView;
    Boolean isPlaying;
    private int position;
    protected static FFmpegMediaPlayer freePlayer;
    private ProgressBar spinner;
    at.markushi.ui.CircleButton prevButton;
    at.markushi.ui.CircleButton playButton;
    at.markushi.ui.CircleButton nextButton;
    protected String trackUrl;
    protected static Player premiumPlayer;
    ImageView backgroundImageView;
    ImageView trackImageView;
    android.support.v7.app.ActionBar actionBar;
    private YouTube youtube;
    private YouTube.Search.List query;
    private SeekBar seekBarView;
    Handler seekHandler = new Handler();
    private TextView currentDuration;
    private TextView finalDuration;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // get ui elements
        backgroundImageView = (ImageView) rootView.findViewById(R.id.backgroundImage);
        trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
        seekBarView = (SeekBar) rootView.findViewById(R.id.seekBar);
        currentDuration = (TextView) rootView.findViewById(R.id.currentDuration);
        finalDuration = (TextView) rootView.findViewById(R.id.finalDuration);
        actionBar = PlayerActivity.actionBar;

        // if song running -> cancel it
        if (freePlayer != null) {
            freePlayer.reset();
        }
        if (premiumPlayer != null) {
            premiumPlayer.pause();
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


    private void setUi(final int position) {

        // get new image url
        String url = TopTenTracksActivityFragment.topTenTrackList.get(position).trackImageLarge;

        // set background and track image and update other ui elements
        Picasso.with(rootView.getContext()).load(url).placeholder(R.drawable.ic_album).error(R.drawable.ic_album).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Bitmap backgroundBitmap = bitmap;
                backgroundBitmap = blurImage(backgroundBitmap, 25.0f);
                backgroundImageView.setImageBitmap(backgroundBitmap);

                trackImageView.setImageBitmap(null);
                trackImageView.setImageBitmap(bitmap);

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

                trackImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // search for video on youtube
                        SearchId searchId = new SearchId();
                        searchId.execute(TopTenTracksActivityFragment.topTenTrackList.get(position).trackArtist + " " + TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);
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
        // set title in the actionbar
        actionBar.setTitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);
        actionBar.setSubtitle(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // artist name
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackArtist);

        // album name
        TextView albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        albumNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackAlbum);

        // track Name
        TextView trackNameView = (TextView) rootView.findViewById(R.id.trackName);
        trackNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(position).trackName);

        seekBarView.setMax(Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(position).trackDuration));
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (premiumPlayer != null) {
                        premiumPlayer.seekToPosition(progress);
                    }
                    if (freePlayer != null) {
                        freePlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        currentDuration.setText("00:00");
        String duration = TopTenTracksActivityFragment.topTenTrackList.get(position).trackDuration;
        int seconds = (int) ((Integer.parseInt(duration) / 1000) % 60);
        int minutes = (int) ((Integer.parseInt(duration) / 1000) / 60);
        if (seconds < 10) {
            finalDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
        } else {
            finalDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
        }
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
                    setSeekBar();
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

    Runnable run = new Runnable() {
        @Override
        public void run() {
            setSeekBar();
        }
    };

    private void setSeekBar() {

        if (freePlayer != null) {
            seekBarView.setProgress(freePlayer.getDuration());
            if (seekBarView.getMax() == freePlayer.getDuration()) {
                nextButton.callOnClick();
            }
        }
        if (premiumPlayer != null) {
            premiumPlayer.getPlayerState(new PlayerStateCallback() {
                @Override
                public void onPlayerState(PlayerState playerState) {
                    seekBarView.setProgress(playerState.positionInMs);
                    int seconds = ((playerState.positionInMs / 1000) % 60);
                    int minutes = ((playerState.positionInMs / 1000) / 60);
                    if (seconds < 10) {
                        currentDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
                    } else {
                        currentDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
                    }
                }
            });

        }
        seekHandler.postDelayed(run, 1000);
    }

    class SearchId extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                youtube = new YouTube.Builder(new NetHttpTransport(),
                        new JacksonFactory(), new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest hr) throws IOException {
                    }
                }).setApplicationName(getActivity().getString(R.string.app_name)).build();

                query = youtube.search().list("id");
                query.setKey("AIzaSyDmD2n10SLAimt0Uv8pclhx8D1le50AV10");
                query.setQ(params[0]);
                query.setType("video");
                query.setFields("items(id/videoId)");
                query.setMaxResults(1l);

                SearchListResponse response = query.execute();
                List<SearchResult> results = response.getItems();
                return results.get(0).getId().getVideoId();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {

                // pause currently running music
                if (freePlayer != null) {
                    freePlayer.pause();
                }
                if (premiumPlayer != null) {
                    premiumPlayer.pause();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + s));
                startActivity(intent);

            } else {
                Toast.makeText(getActivity(), "Video not found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    // for blurring image
    private Bitmap blurImage(Bitmap src, float r) {

        Bitmap bitmap = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(getActivity());

        Allocation blurInput = Allocation.createFromBitmap(renderScript, src);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(r);
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();
        return bitmap;
    }
}

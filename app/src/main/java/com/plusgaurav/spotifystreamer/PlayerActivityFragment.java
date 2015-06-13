package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
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

    // views
    private View rootView;
    private android.support.v7.app.ActionBar actionBar;
    private ImageView backgroundImageView;
    private TextView artistNameView;
    private TextView albumNameView;
    private ImageView trackImageView;
    private ImageView youTubeButtonView;
    private TextView trackNameView;
    private TextView currentDuration;
    private SeekBar seekBarView;
    private TextView finalDuration;
    private at.markushi.ui.CircleButton prevButton;
    private at.markushi.ui.CircleButton playButton;
    private at.markushi.ui.CircleButton nextButton;
    private ProgressBar spinner;

    private Boolean isPlaying;
    private int songPosition;
    String imageUrl;
    protected static FFmpegMediaPlayer freePlayer;

    protected static Player premiumPlayer;

    private YouTube youtube;
    private YouTube.Search.List query;

    private Handler seekHandler = new Handler();


    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get root view
        rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // initialize ui elements
        actionBar = PlayerActivity.actionBar;
        backgroundImageView = (ImageView) rootView.findViewById(R.id.backgroundImage);
        artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
        youTubeButtonView = (ImageButton) rootView.findViewById(R.id.youTubeButton);
        trackNameView = (TextView) rootView.findViewById(R.id.trackName);
        currentDuration = (TextView) rootView.findViewById(R.id.currentDuration);
        seekBarView = (SeekBar) rootView.findViewById(R.id.seekBar);
        finalDuration = (TextView) rootView.findViewById(R.id.finalDuration);
        prevButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.prevButton);
        playButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.playButton);
        nextButton = (at.markushi.ui.CircleButton) rootView.findViewById(R.id.nextButton);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar3);

        // if song running -> pause it
        if (freePlayer != null) {
            freePlayer.pause();
        }
        if (premiumPlayer != null) {
            premiumPlayer.pause();
        }

        // Progress Bar to display loading while everything is being set up
        spinner.setVisibility(View.VISIBLE);

        // get song number from list of songs
        songPosition = Integer.parseInt(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));

        // setup ui
        setUi();

        // prepare music
        prepareMusic();

        return rootView;
    }


    private void setUi() {

        // set track name and album name in the actionbar
        actionBar.setTitle(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
        actionBar.setSubtitle(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

        // TODO: remove once ui is modified
        // artist and album name
        artistNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist);
        albumNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

        // get image url
        imageUrl = TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackImageLarge;
        Picasso.with(rootView.getContext()).load(imageUrl).placeholder(R.drawable.ic_album).error(R.drawable.ic_album).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                // blur and set background image with animation
                Bitmap backgroundBitmap = bitmap;
                backgroundBitmap = blurImage(backgroundBitmap, 25.0f);
                backgroundImageView.setImageBitmap(backgroundBitmap);
                AlphaAnimation alpha = new AlphaAnimation(1, 0.5F);
                alpha.setDuration(1000);
                alpha.setFillAfter(true);
                backgroundImageView.startAnimation(alpha);

                // set track image
                trackImageView.setImageBitmap(bitmap);

                // update ui elements based on average color of track image
                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        int color = palette.getMutedColor(android.R.color.black);
                        int alphaColor = Color.argb(Math.round(Color.alpha(color) * 0.9f), Color.red(color), Color.green(color), Color.blue(color));

                        // action bar
                        actionBar.setBackgroundDrawable(new ColorDrawable(palette.getMutedColor(android.R.color.black)));

                        // status bar
                        getActivity().getWindow().setStatusBarColor(alphaColor);
                        getActivity().getWindow();

                        // navigation bar
                        getActivity().getWindow().setNavigationBarColor(alphaColor);

                        // playback buttons
                        prevButton.setColor(palette.getMutedColor(android.R.color.black));
                        playButton.setColor(palette.getMutedColor(android.R.color.black));
                        nextButton.setColor(palette.getMutedColor(android.R.color.black));

                        // seek bar
                        seekBarView.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(palette.getMutedColor(android.R.color.black), PorterDuff.Mode.MULTIPLY));
                        // TODO: Add change color of thumb
                    }
                });

                // search and link music video on youtube
                // TODO: Disable youtube linkage on freeplayer
                youTubeButtonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SearchVideoId searchVideoId = new SearchVideoId();
                        searchVideoId.execute(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist + " " + TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                // try again
                setUi();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });


        // track Name
        trackNameView.setText(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);

        // seekbar setup and progress listener
        seekBarView.setMax(Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration));
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    if (freePlayer != null) {
                        freePlayer.seekTo(progress);
                    }
                    if (premiumPlayer != null) {
                        premiumPlayer.seekToPosition(progress);

                        // get updated lyrics from musixmatch on position change
                        // reposition lyrics
                        Intent intent = new Intent();
                        intent.setAction("com.android.music.playstatechanged");
                        Bundle bundle = new Bundle();

                        // put the song's metadata
                        bundle.putString("track", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
                        bundle.putString("artist", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist);
                        bundle.putString("album", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

                        // put the song's total duration (in ms)
                        bundle.putLong("duration", Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration)); // 4:05

                        // put the song's current position
                        bundle.putLong("position", progress); // beginning of the song

                        // put the playback status
                        bundle.putBoolean("playing", true); // currently playing

                        // put your application's package
                        bundle.putString("scrobbling_source", "com.plusgaurav.spotifystreamer");

                        intent.putExtras(bundle);
                        getActivity().sendBroadcast(intent);

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

        // set start position of track
        currentDuration.setText("00:00");

        // set end duration of track
        String duration = TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration;
        int seconds = ((Integer.parseInt(duration) / 1000) % 60);
        int minutes = ((Integer.parseInt(duration) / 1000) / 60);
        if (seconds < 10) {
            finalDuration.setText(String.valueOf(minutes) + ":0" + String.valueOf(seconds));
        } else {
            finalDuration.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
        }

        // prev button on click listener
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                songPosition = songPosition - 1;
                if (songPosition < 0) {
                    songPosition = 0;
                }
                setUi();
                playButton.setImageResource(R.drawable.ic_play);
                if (freePlayer != null) {
                    freePlayer.reset();
                }
                prepareMusic();
            }
        });

        // next button on click listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                songPosition = songPosition + 1;
                if (songPosition > TopTenTracksActivityFragment.topTenTrackList.size() - 1) {
                    songPosition = 0;
                }
                setUi();
                playButton.setImageResource(R.drawable.ic_play);
                if (freePlayer != null) {
                    freePlayer.reset();
                }
                prepareMusic();
            }
        });
    }

    private void prepareMusic() {

        // check for free or premium user
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userType = prefs.getString(getString(R.string.user_type_key),
                getString(R.string.user_type_key));

        // free user
        if (userType.equals("free")) {

            // get preview track
            String trackUrl = TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackPreviewUrl;

            freePlayer = new FFmpegMediaPlayer();
            freePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                freePlayer.setDataSource(trackUrl);
                freePlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // initially not playing
            isPlaying = false;

            // disable until prepared
            playButton.setClickable(false);
            playButton.setImageResource(R.drawable.ic_stop);

            freePlayer.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    spinner.setVisibility(View.GONE);

                    // restore button
                    playButton.setClickable(true);
                    playButton.setImageResource(R.drawable.ic_pause);

                    freePlayer.start();
                    setSeekBar();
                    isPlaying = true;

                    // play/pause
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
            // premium player

            // get track
            final String trackUrl = TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackUrl;

            // initially not playing
            isPlaying = false;

            // disable until prepared
            playButton.setClickable(false);
            playButton.setImageResource(R.drawable.ic_stop);

            Config playerConfig = new Config(getActivity(), SearchArtistActivity.getAccessToken(), SearchArtistActivity.CLIENT_ID);
            premiumPlayer = Spotify.getPlayer(playerConfig, getActivity(), new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {

                    // restore button
                    playButton.setClickable(true);

                    premiumPlayer.play(trackUrl);
                    spinner.setVisibility(View.GONE);

                    // start lyrics if musixmatch app installed
                    Intent intent = new Intent();
                    intent.setAction("com.android.music.metachanged");
                    Bundle bundle = new Bundle();

                    // put the song's metadata
                    bundle.putString("track", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
                    bundle.putString("artist", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist);
                    bundle.putString("album", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

                    // put the song's total duration (in ms)
                    bundle.putLong("duration", Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration));

                    // put the song's current position
                    bundle.putLong("position", 0L);

                    // put the playback status
                    bundle.putBoolean("playing", true);

                    // put your application's package
                    bundle.putString("scrobbling_source", "com.plusgaurav.spotifystreamer");

                    intent.putExtras(bundle);
                    getActivity().sendBroadcast(intent);

                    playButton.setImageResource(R.drawable.ic_pause);
                    setSeekBar();
                    isPlaying = true;

                    // play/pause
                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlaying) {
                                premiumPlayer.resume();
                                playButton.setImageResource(R.drawable.ic_pause);
                                isPlaying = true;

                                premiumPlayer.getPlayerState(new PlayerStateCallback() {
                                    @Override
                                    public void onPlayerState(PlayerState playerState) {
                                        int songPosition = playerState.positionInMs;

                                        // resume lyrics
                                        Intent intent = new Intent();
                                        intent.setAction("com.android.music.playstatechanged");
                                        Bundle bundle = new Bundle();

                                        // put the song's metadata
                                        bundle.putString("track", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
                                        bundle.putString("artist", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist);
                                        bundle.putString("album", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

                                        // put the song's total duration (in ms)
                                        bundle.putLong("duration", Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration));

                                        // put the song's current position
                                        bundle.putLong("position", songPosition);

                                        // put the playback status
                                        bundle.putBoolean("playing", true);

                                        // put your application's package
                                        bundle.putString("scrobbling_source", "com.plusgaurav.spotifystreamer");

                                        intent.putExtras(bundle);
                                        getActivity().sendBroadcast(intent);
                                    }
                                });

                            } else {
                                premiumPlayer.pause();
                                isPlaying = false;
                                playButton.setImageResource(R.drawable.ic_play);

                                // pause lyrics
                                Intent intent = new Intent();
                                intent.setAction("com.android.music.playstatechanged");
                                Bundle bundle = new Bundle();

                                // put the song's metadata
                                bundle.putString("track", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackName);
                                bundle.putString("artist", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackArtist);
                                bundle.putString("album", TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackAlbum);

                                // put the song's total duration (in ms)
                                bundle.putLong("duration", Integer.parseInt(TopTenTracksActivityFragment.topTenTrackList.get(songPosition).trackDuration)); // 4:05

                                // put the playback status
                                bundle.putBoolean("playing", false);

                                // put your application's package
                                bundle.putString("scrobbling_source", "com.plusgaurav.spotifystreamer");

                                intent.putExtras(bundle);
                                getActivity().sendBroadcast(intent);

                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    //try again
                    prepareMusic();
                }
            });
        }
    }

    // set up seek bar properties and also update current and max duration
    private void setSeekBar() {

        if (freePlayer != null) {
            seekBarView.setProgress(freePlayer.getDuration());
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

        // ping for updated position every second
        seekHandler.postDelayed(run, 1000);
    }

    // seperate thread for pinging seekbar position
    Runnable run = new Runnable() {
        @Override
        public void run() {
            setSeekBar();
        }
    };

    // Search for music video in youtube
    class SearchVideoId extends AsyncTask<String, Void, String> {

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

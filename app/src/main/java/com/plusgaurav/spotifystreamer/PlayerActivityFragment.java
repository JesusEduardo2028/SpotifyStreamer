package com.plusgaurav.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PlayerActivityFragment extends Fragment {

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // get trackInfo
        String[] trackInfo = getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

        // get artist name
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(trackInfo[4]);

        // album name
        TextView albumNameView = (TextView) rootView.findViewById(R.id.albumName);
        albumNameView.setText(trackInfo[1]);

        // album art
        ImageView trackImageView = (ImageView) rootView.findViewById(R.id.trackImage);
        String url = trackInfo[2];
        Picasso.with(rootView.getContext()).load(url).placeholder(R.drawable.ic_queue_music_black_48dp).error(R.drawable.ic_queue_music_black_48dp).into(trackImageView);

        return rootView;
    }
}

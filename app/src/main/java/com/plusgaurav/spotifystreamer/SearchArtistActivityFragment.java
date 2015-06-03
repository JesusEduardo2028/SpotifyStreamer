package com.plusgaurav.spotifystreamer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchArtistActivityFragment extends Fragment {

    private EditText searchArtist;

    public SearchArtistActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = (View) inflater.inflate(R.layout.fragment_search_artist, container, false);
        searchArtist = (EditText) rootView.findViewById(R.id.searchArtist);
        searchArtist.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Toast.makeText(getActivity(),"Hola!",Toast.LENGTH_LONG).show();
                return true;
            }
        });
        return rootView;
    }
}

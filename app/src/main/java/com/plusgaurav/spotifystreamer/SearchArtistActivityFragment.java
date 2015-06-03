package com.plusgaurav.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class SearchArtistActivityFragment extends Fragment {

    private EditText searchArtist;

    public SearchArtistActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        // Listener for when the user is done typing the artist name in the edittext feild
        searchArtist = (EditText) rootView.findViewById(R.id.searchArtist);
        searchArtist.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchArtist.getWindowToken(), 0);

                    // TODO: search for artists
                    FetchArtistTask task = new FetchArtistTask();
                    task.execute(searchArtist.getText().toString());
                    Toast.makeText(getActivity(), "Hola!", Toast.LENGTH_LONG).show();

                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    public class FetchArtistTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... artistName) {

            String[] result = new String[10];

            // do spotify transaction
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(SearchArtistActivity.getAccessToken());
            SpotifyService spotify = api.getService();

            // get artist
            ArtistsPager artistsPager = spotify.searchArtists(artistName[0]);

            Pager<Artist> artist = artistsPager.artists;


            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String[] result) {
        }
    }
}

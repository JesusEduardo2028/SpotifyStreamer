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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

public class SearchArtistActivityFragment extends Fragment {

    private EditText searchArtist;
    private static myArtistAdapter artistAdapter;
    private static List<HashMap<String, String>> artistList;

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

                    // search for artists
                    FetchArtistTask task = new FetchArtistTask();
                    task.execute(searchArtist.getText().toString());

                    return true;
                }
                return false;
            }
        });


        // artist list view
        artistList = new ArrayList<>();

        // Keys used in Hashmap
        String[] from = {"artistName"};

        // Ids of views in listview_layout
        int[] to = {R.id.artistName};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        artistAdapter = new myArtistAdapter(getActivity(), artistList, R.layout.artistlistview_layout, from, to);
        ListView artistView = (ListView) rootView.findViewById(R.id.artistListView);

        artistView.setAdapter(artistAdapter);

        return rootView;
    }

    public class FetchArtistTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... artistName) {

            String[] result = new String[20];

            // do spotify transaction
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(SearchArtistActivity.getAccessToken());
            SpotifyService spotify = api.getService();

            Map<String, Object> options = new HashMap<>();
            options.put("limit", 20);
            ArtistsPager artistsPager = spotify.searchArtists(artistName[0], options);

            artistList.clear();
            int i = 0;
            for (Artist artist : artistsPager.artists.items) {
                HashMap<String, String> artistMap = new HashMap<>();
                artistMap.put("artistName", artist.name);
                artistMap.put("artistId", artist.id);
                // TODO put logic to decide image size
                for (Image image : artist.images) {
                    //if((image.height)*(image.width)==200)
                    artistMap.put("artistImage", image.url);
                }
                artistList.add(artistMap);
                i++;
            }
            return !artistList.isEmpty();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                artistAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "No results found for \"" + searchArtist.getText() + "\"", Toast.LENGTH_LONG).show();
            }
        }
    }

    // got help from here "http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view"
    public class myArtistAdapter extends SimpleAdapter {

        public myArtistAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            // Then we get reference for Picasso
            ImageView img = (ImageView) v.findViewById(R.id.artistImage);

            // get the url from the data you passed to the `Map`
            String url = (String) ((Map) getItem(position)).get("artistImage");
            if (url != null) {
                Picasso.with(v.getContext()).load(url).placeholder(R.drawable.ic_play_circle_filled_black_36dp).error(R.drawable.ic_play_circle_filled_black_36dp).into(img);
            }
            // return the view
            return v;
        }
    }
}

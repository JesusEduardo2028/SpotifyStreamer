package com.plusgaurav.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

    private EditText searchArtistEditText;
    private static myArtistAdapter artistAdapter;
    private static List<HashMap<String, String>> artistList;

    public SearchArtistActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        // Listener for when the user is done typing the artist name in the edittext feild
        searchArtistEditText = (EditText) rootView.findViewById(R.id.searchArtistEditText);
        searchArtistEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchArtistEditText.getWindowToken(), 0);

                    // search for artists
                    // TODO implement callback
                    FetchArtistTask task = new FetchArtistTask();
                    task.execute(searchArtistEditText.getText().toString());

                    return true;
                }
                return false;
            }
        });


        // artist listview part
        artistList = new ArrayList<>();

        // Keys used in Hashmap
        String[] from = {"artistName", "artistImage"};

        // Ids of views in listview_layout
        int[] to = {R.id.artistName, R.id.artistImage};

        // initialize adapter
        artistAdapter = new myArtistAdapter(getActivity(), artistList, R.layout.artistlistview_layout, from, to);

        // bind listview
        ListView artistView = (ListView) rootView.findViewById(R.id.artistListView);
        artistView.setAdapter(artistAdapter);

        // open top 10 track view
        artistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = artistList.get(position).get("artistId");
                String artistName = artistList.get(position).get("artistName");
                Intent intent = new Intent(getActivity(), TopTenTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, new String[]{artistId, artistName});
                startActivity(intent);
            }
        });

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

            // set options
            Map<String, Object> options = new HashMap<>();
            options.put("limit", 20);

            // search artist
            if (artistName[0].equals("")) {
                return false;
            }

            ArtistsPager artistsPager = spotify.searchArtists(artistName[0], options);

            // update data source
            artistList.clear();
            for (Artist artist : artistsPager.artists.items) {
                HashMap<String, String> artistMap = new HashMap<>();
                artistMap.put("artistName", artist.name);
                artistMap.put("artistId", artist.id);
                for (Image image : artist.images) {
                    if (image.width >= 200 && image.width <= 300) {
                        artistMap.put("artistImage", image.url);
                        break;
                    }
                }
                artistList.add(artistMap);
            }

            // return true if data source refreshed
            return !artistList.isEmpty();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean isDataSourceRefereshed) {
            if (isDataSourceRefereshed) {
                artistAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "No results found for \"" + searchArtistEditText.getText() + "\"", Toast.LENGTH_LONG).show();
            }
        }
    }

    // create custom adapter
    // got help from "http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view"
    public class myArtistAdapter extends SimpleAdapter {

        public myArtistAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);

            // get reference to imageview
            ImageView artistImageView = (ImageView) view.findViewById(R.id.artistImage);

            // get the url from the data source
            String url = (String) ((Map) getItem(position)).get("artistImage");

            // load it to the imageview
            Picasso.with(view.getContext()).load(url).placeholder(R.drawable.ic_play_circle_filled_black_36dp).error(R.drawable.ic_play_circle_filled_black_36dp).into(artistImageView);


            return view;
        }
    }
}

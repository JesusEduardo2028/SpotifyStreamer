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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class SearchArtistActivityFragment extends Fragment {

    private EditText searchArtistEditText;
    private static ArtistAdapter artistAdapter;
    private ArrayList<ArtistListData> artistList;
    ListView artistView;

    public SearchArtistActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get saved datasource if present
        if (savedInstanceState != null) {
            artistList = savedInstanceState.getParcelableArrayList("savedArtistList");
            bindView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save data source
        if (artistList != null) {
            outState.putParcelableArrayList("savedArtistList", artistList);
        }
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
                    FetchArtistTask task = new FetchArtistTask();
                    task.execute(searchArtistEditText.getText().toString());

                    return true;
                }
                return false;
            }
        });

        // bind view with adapter
        artistList = new ArrayList<>();
        artistView = (ListView) rootView.findViewById(R.id.artistListView);
        bindView();

        // open top 10 track view
        artistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = artistList.get(position).artistId;
                String artistName = artistList.get(position).artistName;
                Intent intent = new Intent(getActivity(), TopTenTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, new String[]{artistId, artistName});
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void bindView() {

        // initialize adapter
        artistAdapter = new ArtistAdapter(getActivity(), artistList);

        // bind listview
        artistView.setAdapter(artistAdapter);
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

            // check for empty string
            if (artistName[0].equals("")) {
                return false;
            }

            // search artist
            ArtistsPager artistsPager = spotify.searchArtists(artistName[0], options);

            // update data source
            artistList.clear();
            for (Artist artist : artistsPager.artists.items) {
                ArtistListData currentArtist = new ArtistListData(artist);
                artistList.add(currentArtist);
            }

            // return true if data source refreshed
            return !artistList.isEmpty();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean isDataSourceRefreshed) {
            if (isDataSourceRefreshed) {
                artistAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "No results found for \"" + searchArtistEditText.getText() + "\"", Toast.LENGTH_LONG).show();
            }
        }
    }

    // custom adapter
    // got help from "http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view"
    public class ArtistAdapter extends BaseAdapter {
        ArrayList artistList = new ArrayList();
        Context context;


        public ArtistAdapter(Context context, ArrayList artistList) {
            this.artistList = artistList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return artistList.size();
        }

        @Override
        public ArtistListData getItem(int position) {
            return (ArtistListData) artistList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.artistlistview_layout, viewGroup, false);

            // put artist image
            de.hdodenhof.circleimageview.CircleImageView artistImageView = (de.hdodenhof.circleimageview.CircleImageView) row.findViewById(R.id.artistImage);
            String url = getItem(position).artistImage;
            Picasso.with(row.getContext()).load(url).placeholder(R.drawable.ic_play_circle_filled_black_36dp).error(R.drawable.ic_play_circle_filled_black_36dp).into(artistImageView);

            // put artist name
            TextView artistName = (TextView) row.findViewById(R.id.artistName);
            artistName.setText(getItem(position).artistName);

            return row;
        }
    }
}

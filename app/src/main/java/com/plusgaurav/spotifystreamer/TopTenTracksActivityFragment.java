package com.plusgaurav.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTenTracksActivityFragment extends Fragment {

    private static TopTenTrackAdapter topTenTrackAdapter;
    private ArrayList<trackListData> topTenTrackList;

    public TopTenTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        // TODO bind list to adapter
        topTenTrackList = new ArrayList<>();
        topTenTrackAdapter = new TopTenTrackAdapter(getActivity(), topTenTrackList);


        // bind listview
        ListView artistView = (ListView) rootView.findViewById(R.id.artistListView);
        artistView.setAdapter(topTenTrackAdapter);

        // get top ten tracks of the artist (async task)
        FetchTopTenTrack task = new FetchTopTenTrack();
        String[] artistInfo = getActivity().getIntent().getExtras().getStringArray(Intent.EXTRA_TEXT);

        // pass artistId
        assert artistInfo != null;
        task.execute(artistInfo[0]);

        // TODO implement listener to start PlayMusicActivity


        return rootView;
    }

    public class FetchTopTenTrack extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... artistId) {

            // do spotify transaction
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(SearchArtistActivity.getAccessToken());
            SpotifyService spotify = api.getService();

            // set options
            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");

            // search top 10 tracks of the artist
            Tracks topTracks = spotify.getArtistTopTrack(artistId[0], options);
            topTenTrackList.clear();
            for (Track track : topTracks.tracks) {
                trackListData currentTrack = new trackListData();
                currentTrack.setTrackName(track.name);
                currentTrack.setTrackAlbum(track.album.name);
                for (Image image : track.album.images) {
                    if (image.width >= 200 && image.width <= 300) {
                        currentTrack.setTrackImageSmall(image.url);
                    }
                    if (image.width >= 640) {
                        currentTrack.setTrackImageLarge(image.url);
                    }
                    currentTrack.setTrackPreviewUrl(track.preview_url);
                }
                topTenTrackList.add(currentTrack);
            }

            // return true if data source refreshed
            return !topTenTrackList.isEmpty();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean isDataSourceRefereshed) {
            if (isDataSourceRefereshed) {
                topTenTrackAdapter.notifyDataSetChanged();
            } else {
                String[] artistInfo = getActivity().getIntent().getExtras().getStringArray(Intent.EXTRA_TEXT);
                assert artistInfo != null;
                Toast.makeText(getActivity(), "No tracks found for \"" + artistInfo[1] + "\"", Toast.LENGTH_LONG).show();
            }
        }
    }


    // custom adapter
    // got help from "http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view"
    public class TopTenTrackAdapter extends BaseAdapter {
        ArrayList topTenTrackList = new ArrayList();
        Context context;


        public TopTenTrackAdapter(Context context, ArrayList topTenTrackList) {
            this.topTenTrackList = topTenTrackList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return topTenTrackList.size();
        }

        @Override
        public trackListData getItem(int position) {
            return (trackListData) topTenTrackList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.toptentracklistview_layout, viewGroup, false);

            // put track image
            de.hdodenhof.circleimageview.CircleImageView trackImageView = (de.hdodenhof.circleimageview.CircleImageView) row.findViewById(R.id.trackImage);
            String url = getItem(position).getTrackImageSmall();
            Picasso.with(row.getContext()).load(url).placeholder(R.drawable.ic_play_circle_filled_black_36dp).error(R.drawable.ic_play_circle_filled_black_36dp).into(trackImageView);

            // put track name
            TextView trackName = (TextView) row.findViewById(R.id.trackName);
            trackName.setText(getItem(position).getTrackName());

            // put track album
            TextView trackAlbum = (TextView) row.findViewById(R.id.trackAlbum);
            trackAlbum.setText(getItem(position).getTrackAlbum());

            return row;
        }
    }
}

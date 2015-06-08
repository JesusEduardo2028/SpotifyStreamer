package com.plusgaurav.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackListData implements Parcelable {

    String trackName;
    String trackAlbum;
    String trackImageSmall;
    String trackImageLarge;
    String trackPreviewUrl;
    String trackDuration;
    String trackArtist;
    String trackUrl;

    public TrackListData(Track track) {
        this.trackName = track.name;
        this.trackAlbum = track.album.name;
        for (Image image : track.album.images) {
            if (image.width >= 150 && image.width <= 300) {
                this.trackImageSmall = image.url;
            }
            if (image.width >= 500) {
                this.trackImageLarge = image.url;
            }
            this.trackPreviewUrl = track.preview_url;
        }
        this.trackDuration = String.valueOf(track.duration_ms);
        this.trackUrl = track.uri;
    }

    public TrackListData(Parcel in) {
        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        trackName = in.readString();
        trackAlbum = in.readString();
        trackImageSmall = in.readString();
        trackImageLarge = in.readString();
        trackPreviewUrl = in.readString();
        trackDuration = in.readString();
        trackArtist = in.readString();
        trackUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(trackName);
        out.writeString(trackAlbum);
        out.writeString(trackImageSmall);
        out.writeString(trackImageLarge);
        out.writeString(trackPreviewUrl);
        out.writeString(trackDuration);
        out.writeString(trackArtist);
        out.writeString(trackUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TrackListData> CREATOR = new Parcelable.Creator<TrackListData>() {
        public TrackListData createFromParcel(Parcel in) {
            return new TrackListData(in);
        }

        public TrackListData[] newArray(int size) {
            return new TrackListData[size];
        }
    };

}

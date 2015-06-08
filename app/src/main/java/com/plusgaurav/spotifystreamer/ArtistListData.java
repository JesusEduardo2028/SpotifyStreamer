package com.plusgaurav.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistListData implements Parcelable {
    String artistName;
    String artistId;
    String artistImage;

    public ArtistListData(Artist artist) {
        artistName = artist.name;
        artistId = artist.id;
        for (Image image : artist.images) {
            if (image.width >= 150 && image.width <= 300) {
                artistImage = image.url;
                break;
            }
        }
    }

    public ArtistListData(Parcel in) {
        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        artistName = in.readString();
        artistId = in.readString();
        artistImage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(artistName);
        out.writeString(artistId);
        out.writeString(artistImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ArtistListData> CREATOR = new Parcelable.Creator<ArtistListData>() {
        public ArtistListData createFromParcel(Parcel in) {
            return new ArtistListData(in);
        }

        public ArtistListData[] newArray(int size) {
            return new ArtistListData[size];
        }
    };
}
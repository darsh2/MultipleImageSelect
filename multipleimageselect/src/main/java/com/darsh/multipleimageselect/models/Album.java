package com.darsh.multipleimageselect.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album implements Parcelable {
    public String name;
    public String imagePath;

    public Album(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imagePath);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private Album(Parcel in) {
        name = in.readString();
        imagePath = in.readString();
    }
}

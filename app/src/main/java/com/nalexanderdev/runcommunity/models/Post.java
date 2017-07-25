package com.nalexanderdev.runcommunity.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by niholas.alexander x13125354@student.ncirl.ie on 27/06/2017.
 */
@IgnoreExtraProperties
public class Post implements Parcelable {

    String key;
    String uid;
    String username;
    String post;
    String imageUrl;
    public Post() {
    }

    public Post(String uid, String username, String post) {
        this.uid = uid;
        this.username = username;
        this.post = post;
    }

    public Post(String uid, String username, String post, String imageUrl) {
        this.uid = uid;
        this.username = username;
        this.post = post;
        this.imageUrl = imageUrl;
    }

    protected Post(Parcel in) {
        key = in.readString();
        uid = in.readString();
        username = in.readString();
        post = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPost() {
        return post;
    }

    public String getUid() {
        return uid;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("post", post);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(uid);
        parcel.writeString(username);
        parcel.writeString(post);
        parcel.writeString(imageUrl);
    }
}

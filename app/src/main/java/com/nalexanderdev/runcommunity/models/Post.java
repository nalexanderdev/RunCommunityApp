package com.nalexanderdev.runcommunity.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by niholas.alexander x13125354@student.ncirl.ie on 27/06/2017.
 */
@IgnoreExtraProperties
public class Post {
    String uid;
    String username;
    String post;

    public Post() {
    }

    public Post(String uid, String username, String post) {
        this.uid = uid;
        this.username = username;
        this.post = post;
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

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("post", post);
        return result;
    }
}

package com.nalexanderdev.runcommunity.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.adapters.CommentListViewAdapter;
import com.nalexanderdev.runcommunity.fragments.CreateCommentDialog;
import com.nalexanderdev.runcommunity.models.Comment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nalexanderdev.runcommunity.models.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends BaseActivity {

    String postKey, postTitle;

    ListView listView;
    CommentListViewAdapter adapter;
    private List<Comment> comments;

    private DatabaseReference commentDb;
    private ValueEventListener eventListener;
    private ChildEventListener childEventListener;

    private FirebaseUser user;
    private ImageView toolbarImageView;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!getIntent().hasExtra("pid") || !getIntent().hasExtra("post") || !getIntent().hasExtra("postObject")){
            finish();
        }

        user = getUser();
        if(user == null){
            finish();
        }

        postKey = getIntent().getStringExtra("pid");
        postTitle = getIntent().getStringExtra("post");
        post = getIntent().getParcelableExtra("postObject");

        setTitle(postTitle);

        commentDb = FirebaseDatabase.getInstance().getReference().child("comments");

        comments = new ArrayList<>();
        adapter = new CommentListViewAdapter(this,R.layout.post_item, comments);
        listView = (ListView) findViewById(R.id.listView);
        toolbarImageView = (ImageView) findViewById(R.id.ivBigImage);
        listView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager manager = getSupportFragmentManager();
                CreateCommentDialog c = CreateCommentDialog.newInstance(user.getUid(),
                        user.getDisplayName(), postKey);
                c.show(manager, "Comment Dialog");
            }
        });

        SetImageView();

        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(PostDetailActivity.this, MapsActivity.class);
                startActivity(i);
            }
        }) ;
    }

    private void SetImageView() {
        // set title background image
        final String imageUrl = post.getImageUrl();
        Log.d("POST_IMAGE", String.format("Image url %s", imageUrl));
        if (imageUrl != null) {
            Uri selectedImageUri = Uri.parse(imageUrl);
            Log.d("POST_IMAGE", String.format("Image url %s", selectedImageUri));
            if (selectedImageUri != null) {
                Log.d("POST_IMAGE", "Ready to display");
                Picasso.with(PostDetailActivity.this).load(selectedImageUri.toString()).fit().centerCrop().into(toolbarImageView);
                toolbarImageView.setVisibility(View.VISIBLE);

                toolbarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(PostDetailActivity.this, FullscreenImageActivity.class);
                        intent.putExtra("imageUrl",imageUrl);
                        intent.putExtra("title", post.getPost());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(eventListener != null){
            commentDb.removeEventListener(eventListener);
        }
        if(childEventListener != null){
            commentDb.removeEventListener(childEventListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Comment c = child.getValue(Comment.class);
                    if (c.getPid().equals(postKey)) {
                        comments.add(c);
                    }
                }

                adapter.updateList(comments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        commentDb.addValueEventListener(eventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        commentDb.addChildEventListener(childEventListener);

    }
}

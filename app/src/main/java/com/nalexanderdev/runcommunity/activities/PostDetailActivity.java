package com.nalexanderdev.runcommunity.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.adapters.CommentListViewAdapter;
import com.nalexanderdev.runcommunity.fragmenst.CreateCommentDialog;
import com.nalexanderdev.runcommunity.models.Comment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends BaseActivity {

    String postKey, post;

    ListView listView;
    CommentListViewAdapter adapter;
    private List<Comment> comments;

    private DatabaseReference commentDb;
    private ValueEventListener eventListener;
    private ChildEventListener childEventListener;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!getIntent().hasExtra("pid") || !getIntent().hasExtra("post") ){
            finish();
        }

        user = getUser();
        if(user == null){
            finish();
        }

        postKey = getIntent().getStringExtra("pid");
        post = getIntent().getStringExtra("post");

        setTitle(post);

        commentDb = FirebaseDatabase.getInstance().getReference().child("comments");

        comments = new ArrayList<>();
        adapter = new CommentListViewAdapter(this,R.layout.post_item, comments);
        listView = (ListView) findViewById(R.id.listView);
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
                    comments.add(c);
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

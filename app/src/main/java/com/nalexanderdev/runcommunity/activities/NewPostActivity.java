package com.nalexanderdev.runcommunity.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.models.Post;

public class NewPostActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;
    private DatabaseReference db;
    private EditText contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        auth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //
                    startActivity(new Intent(NewPostActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(NewPostActivity.this, LoginActivity.class));
            finish();
        }

        db = FirebaseDatabase.getInstance().getReference().child("posts");

        // Get ref
        Button createBtn = (Button) findViewById(R.id.create_post);
        contentText = (EditText) findViewById(R.id.content);

        // Setup actions
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save Post
                createPost();
            }
        });
    }

    private void createPost() {
        // Validate data
        if (validatePost()) {

            // create post
            Post p = new Post(user.getUid(), user.getDisplayName(), contentText.getText().toString());

            // Save post
            db.push().setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // start Post Activity
                        finish();
                    }
                }
            });
        }
    }

    private boolean validatePost() {
        if (contentText.getText().toString().isEmpty()) {
            contentText.setError("Required");
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            auth.removeAuthStateListener(listener);
        }
    }
}

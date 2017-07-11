package com.nalexanderdev.runcommunity.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.models.Post;
import com.squareup.picasso.Picasso;

public class NewPostActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;

    private StorageReference mStorage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog mProgressDialog;

    private DatabaseReference db;
    private EditText contentText;
    private String imageUrl;
    private ImageView mImageView;
    private Uri selectedImageUri;

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
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);

        // Get ref
        Button createBtn = (Button) findViewById(R.id.create_post);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        contentText = (EditText) findViewById(R.id.content);
        mImageView = (ImageView) findViewById(R.id.post_image);

        // Setup actions
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save Post
                createPost();
            }
        });

        //
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }

    private void createPost() {
        // Validate data
        if (validatePost()) {

            //if image
            if (selectedImageUri != null) {
                mProgressDialog.setMessage("Uploading...");
                mProgressDialog.show();
                imageUrl = selectedImageUri.getLastPathSegment();

                StorageReference filepath = mStorage.child("Photos").child(imageUrl);

                UploadTask uploadTask = filepath.putFile(selectedImageUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProgressDialog.dismiss();

                        Uri downloadUri = taskSnapshot.getDownloadUrl();

                        Log.d("path", downloadUri.toString());

                        imageUrl = downloadUri.toString();
                        Post p = new Post(user.getUid(), user.getDisplayName(), contentText.getText().toString(), imageUrl);

                        // Save post
                        db.push().setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // start Post Activity
                                    finish();
                                }
                            }
                        });

                        Toast.makeText(NewPostActivity.this, "Upload done", Toast.LENGTH_LONG).show();
                    }

                });
            } else {
                // create post
                Post p = new Post(user.getUid(), user.getDisplayName(), contentText.getText().toString());

                // Save post
                db.push().setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // start Post Activity
                            finish();
                        }
                    }
                });
            }


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            selectedImageUri = data.getData();
            Picasso.with(NewPostActivity.this).load(selectedImageUri.toString()).fit().centerCrop().into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
        }
    }
}

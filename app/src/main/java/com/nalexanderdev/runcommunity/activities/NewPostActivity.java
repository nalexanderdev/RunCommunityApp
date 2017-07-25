package com.nalexanderdev.runcommunity.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
    private boolean isEditing = false;
    private boolean hasSelectedImage = false;
    private Post postEdit;

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

        // get Post if passed in
        if (getIntent().hasExtra("post")) {
            isEditing = true;
            postEdit = getIntent().getParcelableExtra("post");
            contentText.setText(postEdit.getPost());
            Picasso.with(NewPostActivity.this).load(postEdit.getImageUrl()).fit().centerCrop().into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
            createBtn.setText(getString(R.string.editText));
        }

        // Setup actions
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save Post
                if (!isEditing) {
                    createPost();
                } else {
                    updatePost();
                }
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

    private void updatePost() {
        boolean needsTitleChange = !postEdit.getPost().equals(contentText.getText().toString());
        if (needsTitleChange && hasSelectedImage) {
            // update the whole thing
            db.child(postEdit.getKey()).child("post").setValue(contentText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadImageToFirebase(editPicturePostOnSuccessListener);
                            }
                        }
                    });
            return;
        }

        if (needsTitleChange) {
            // title has changed
            db.child(postEdit.getKey()).child("post").setValue(contentText.getText().toString())
                    .addOnCompleteListener(completeListener);
        }
        if (hasSelectedImage) {
            uploadImageToFirebase(editPicturePostOnSuccessListener);
        }
    }

    private void uploadImageToFirebase(OnSuccessListener<UploadTask.TaskSnapshot> newPostOnSuccessListener) {
        mProgressDialog.setMessage("Uploading...");
        mProgressDialog.show();
        imageUrl = selectedImageUri.getLastPathSegment();

        StorageReference filepath = mStorage.child("Photos").child(imageUrl);

        UploadTask uploadTask = filepath.putFile(selectedImageUri);


        uploadTask.addOnSuccessListener(newPostOnSuccessListener);
    }

    private void createPost() {
        // Validate data
        if (validatePost()) {

            //if image
            if (selectedImageUri != null) {
                uploadImageToFirebase(newPostOnSuccessListener);
            } else {
                // create post
                Post p = new Post(user.getUid(), user.getDisplayName(), contentText.getText().toString());

                // Save post
                db.push().setValue(p).addOnCompleteListener(completeListener);
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
            hasSelectedImage = true;
            selectedImageUri = data.getData();
            Picasso.with(NewPostActivity.this).load(selectedImageUri.toString()).fit().centerCrop().into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
        }
    }
    // Update listener
    private OnSuccessListener<UploadTask.TaskSnapshot> newPostOnSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            mProgressDialog.dismiss();

            Uri downloadUri = taskSnapshot.getDownloadUrl();

            Log.d("path", downloadUri.toString());

            imageUrl = downloadUri.toString();
            Post p = new Post(user.getUid(), user.getDisplayName(), contentText.getText().toString(), imageUrl);

            // Save post
            db.push().setValue(p)
                    .addOnCompleteListener(completeListener);

            Toast.makeText(NewPostActivity.this, "Upload done", Toast.LENGTH_LONG).show();
        }

    };

    private OnSuccessListener<UploadTask.TaskSnapshot> editPicturePostOnSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            mProgressDialog.dismiss();

            Uri downloadUri = taskSnapshot.getDownloadUrl();

            Log.d("path", downloadUri.toString());

            imageUrl = downloadUri.toString();

            // Update post

            db.child(postEdit.getKey()).child("imageUrl").setValue(imageUrl)
                    .addOnCompleteListener(completeListener);
            Toast.makeText(NewPostActivity.this, "Upload done", Toast.LENGTH_LONG).show();
        }

    };

    private OnCompleteListener<Void> completeListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                // start Post Activity
                finish();
            }
        }
    };
}

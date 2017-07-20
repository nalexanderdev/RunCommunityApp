package com.nalexanderdev.runcommunity.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nalexanderdev.runcommunity.R;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends BaseActivity {

    EditText emailF, nameF;
    Button cancelBtn, updateBtn;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener listener;
    FirebaseUser user;

    private Button mSelectImage;
    private StorageReference mStorage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog mProgressDialog;
    private ImageView mImageView;

    private String imageUr = "https://firebasestorage.googleapis.com/v0/b/runcommunity-f337f.appspot.com/o/Photos%2F19168?alt=media&token=99283374-1602-4a48-9fdb-090d1731f16b";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mImageView = (ImageView) findViewById(R.id.circleImageView);

        emailF = (EditText) findViewById(R.id.emailField);
        nameF = (EditText) findViewById(R.id.nameField);
        updateBtn = (Button) findViewById(R.id.updateBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        emailF.setEnabled(false);
        nameF.setEnabled(false);
        updateBtn.setVisibility(View.INVISIBLE);
        cancelBtn.setVisibility(View.INVISIBLE);

        mStorage = FirebaseStorage.getInstance().getReference();
        mSelectImage = (Button) findViewById(R.id.selectImage);
        mProgressDialog = new ProgressDialog(this);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        auth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user == null){
                    //
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            nameF.setText(user.getDisplayName());
            emailF.setText(user.getEmail());
            if(user.getPhotoUrl() != null) {
                Log.d("Profile", user.getPhotoUrl().toString());
                Picasso.with(ProfileActivity.this).load(user.getPhotoUrl().toString()).fit().centerCrop()
                        .into(mImageView);
            }else{
                for (UserInfo userInfo : user.getProviderData()) {
                    if (userInfo.getPhotoUrl() != null) {
                        Log.d("Profile", userInfo.getPhotoUrl().toString());
                        Picasso.with(ProfileActivity.this).load(userInfo.getPhotoUrl().toString()).fit().centerCrop()
                                .into(mImageView);
                    }
                }
            }
        }



        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameF.setEnabled(false);
                updateBtn.setVisibility(View.INVISIBLE);
                cancelBtn.setVisibility(View.INVISIBLE);
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameF.getText().toString().isEmpty()){
                    nameF.setError("Required!");
                }else if(nameF.getText().toString().equals(user.getDisplayName())){
                    showToast("No new changes!");
                    nameF.setEnabled(false);
                    updateBtn.setVisibility(View.INVISIBLE);
                    cancelBtn.setVisibility(View.INVISIBLE);
                }else{
                    updateName(nameF.getText().toString());
                }

            }
        });


    }

    void updateName(String name){
        showMessageDialog("Updating Name...");
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressDialog();
                        if(!task.isSuccessful()){
                            showToast("Failed to update Profile Name!");
                        }else{
                            nameF.setEnabled(false);
                            updateBtn.setVisibility(View.INVISIBLE);
                            cancelBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            return true;
        }else if(id == R.id.action_edit_profile){
            nameF.setEnabled(true);
            updateBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            return true;
        }
        else if(id == R.id.action_posts){
            startActivity(new Intent(this, PostActivity.class));
            return true;
        }

        else if(id == R.id.action_new_post){
            startActivity(new Intent(this, NewPostActivity.class));
            return true;
        }

        else if(id == R.id.action_web_view){
            startActivity(new Intent(this, WebViewActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listener != null){
            auth.removeAuthStateListener(listener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.show();

            Uri uri = data.getData();
            imageUr = uri.getLastPathSegment();

            StorageReference filepath = mStorage.child("Photos").child(imageUr);

            UploadTask uploadTask = filepath.putFile(uri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mProgressDialog.dismiss();

                    @SuppressWarnings("VisibleForTests") Uri downloadUri = taskSnapshot.getDownloadUrl();

                    Log.d("path",downloadUri.toString() );

                    imageUr = downloadUri.toString();

                    updateImage(downloadUri);

                    Picasso.with(ProfileActivity.this).load(downloadUri.toString()).fit().centerCrop().into(mImageView);

                    Toast.makeText(ProfileActivity.this, "Upload done", Toast.LENGTH_LONG).show();
                }

            });
        }
    }

    void updateImage(Uri uri){
        showMessageDialog("Updating Name...");
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressDialog();
                        if(!task.isSuccessful()){
                            showToast("Failed to update Profile Name!");
                        }else{
                            showToast("Profile Picture Updated!");
                        }
                    }
                });

    }
}




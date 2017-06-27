package com.nalexanderdev.runcommunity.fragmenst;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by room3.04 on 21/06/2017.
 */

public class CreatePostDialog extends DialogFragment {

    DatabaseReference db;

    EditText postText;
    Button createBtn;

    String uid, username;


    public CreatePostDialog() {
    }

    public static CreatePostDialog newInstance(String uid, String username){
        CreatePostDialog dialog = new CreatePostDialog();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("username", username);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            uid = getArguments().getString("uid");
            username = getArguments().getString("username");
        }

        db = FirebaseDatabase.getInstance().getReference().child("posts");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.post_layout, container, false);
        postText = (EditText) view.findViewById(R.id.postTxt);
        createBtn = (Button) view.findViewById(R.id.createBtn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Post
                if(postText.getText().toString().isEmpty()){
                    postText.setError("Required");
                }else {
                    Post p = new Post(uid, username, postText.getText().toString());
                    createNewPost(p);
                }
            }
        });

        return view;
    }

    private void createNewPost(Post p) {
        db.push().setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getDialog().dismiss();
                }
            }
        });
    }
}

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
import com.nalexanderdev.runcommunity.models.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by room3.04 on 21/06/2017.
 */

public class CreateCommentDialog extends DialogFragment {

    DatabaseReference db;

    EditText commentText;
    Button createBtn;

    String uid, username, pid;


    public CreateCommentDialog() {
    }

    public static CreateCommentDialog newInstance(String uid, String username, String pid){
        CreateCommentDialog dialog = new CreateCommentDialog();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("username", username);
        args.putString("pid", pid);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            uid = getArguments().getString("uid");
            username = getArguments().getString("username");
            pid = getArguments().getString("pid");
        }

        db = FirebaseDatabase.getInstance().getReference().child("comments");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.comment_layout, container, false);
        commentText = (EditText) view.findViewById(R.id.commentTxt);
        createBtn = (Button) view.findViewById(R.id.createBtn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Post
                if(commentText.getText().toString().isEmpty()){
                    commentText.setError("Required");
                }else {
                    Comment c = new Comment(uid, username,pid, commentText.getText().toString());
                    createNewPost(c);
                }
            }
        });

        return view;
    }

    private void createNewPost(Comment c) {
        db.push().setValue(c).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getDialog().dismiss();
                }
            }
        });
    }
}

package com.nalexanderdev.runcommunity.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.nalexanderdev.runcommunity.R;

/**
 * Created by Harkebi on 04/07/2017.
 */
public class DeletePostDialog extends DialogFragment {
    DatabaseReference db;

    Button yesBtn;
    Button noBtn;

    String pid;
    DataSnapshot postDataSnapshot;

    public DeletePostDialog() {
    }

    public static DeletePostDialog newInstance(String pid){
        DeletePostDialog dialog = new DeletePostDialog();
        Bundle args = new Bundle();
        args.putString("pid", pid);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            pid = getArguments().getString("pid");
        }

        db = FirebaseDatabase.getInstance().getReference().child("posts");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.delete_post_layout, container, false);
        yesBtn = (Button) view.findViewById(R.id.yes);
        noBtn = (Button) view.findViewById(R.id.no);

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete Post
                if (pid != null) {
                    deletePost(pid);
                } else {
                    getDialog().dismiss();
                }
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    private void deletePost(String pid) {
        db.child(pid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getDialog().dismiss();
                }
            }
        });
    }
}

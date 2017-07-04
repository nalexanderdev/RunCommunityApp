package com.nalexanderdev.runcommunity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nalexanderdev.runcommunity.R;
import com.nalexanderdev.runcommunity.models.Comment;

import java.util.List;

/**
 * Created by Nick Alexander x13125354@student.ncirl.ie 13/06/2017.
 */

public class CommentListViewAdapter extends ArrayAdapter<Comment>{
    //List<Post> posts;
    int resource;
    List<Comment> posts;
    private Context context;
    public CommentListViewAdapter(Context context, int resource, List<Comment> postList) {
        super(context, resource, postList);
        this.resource = resource;
        this.context = context;
        this.posts = postList;
    }


    public void updateList(List<Comment> postList){
        posts.clear();
        posts.addAll(postList);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Comment c = posts.get(position);
        PostHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);

            holder = new PostHolder();
            holder.nameField = (TextView) convertView.findViewById(R.id.nameText);
            holder.commentField = (TextView) convertView.findViewById(R.id.postText);

            convertView.setTag(holder);
        }else{
            holder = (PostHolder) convertView.getTag();
        }

        holder.nameField.setText(c.getUsername());
        holder.commentField.setText(c.getComment());

        return convertView;
    }

    static class PostHolder{
        TextView nameField;
        TextView commentField;
    }
}

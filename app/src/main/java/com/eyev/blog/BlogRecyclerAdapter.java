package com.eyev.blog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blogList;
    private FirebaseFirestore mFirestore;
    private Context mContext;

    BlogRecyclerAdapter(List<BlogPost> blogList){
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String descData = blogList.get(position).getDescription();
        holder.setDescriptionView(descData);

        String imageUrl = blogList.get(position).getImage();
        holder.setImage(imageUrl);

        String userId = blogList.get(position).getUserId();
        holder.setProfile(userId);

        long milliseconds = blogList.get(position).getTimestamp().getTime();
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(milliseconds);
        Log.d(TAG, "onBindViewHolder: date : "+date);
        holder.setDate(date);

    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private ImageView mImageView;
        private ImageView mUserProfile;
        private TextView userName;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDescriptionView(String description){

            TextView descriptionView = mView.findViewById(R.id.post_description);
            descriptionView.setText(description);
        }

        void setImage(String downloadUri){

            mImageView = mView.findViewById(R.id.post_image);
            Glide.with(mContext).load(downloadUri).into(mImageView);
        }

        void setProfile(String userId){
            mUserProfile = mView.findViewById(R.id.post_user_image);
            mFirestore = FirebaseFirestore.getInstance();
            userName = mView.findViewById(R.id.post_username);

            mFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null){
                        String name = task.getResult().getString("name");
                        String profileUrl = task.getResult().getString("image");

                        userName.setText(name);

                        Glide.with(mContext).load(profileUrl).into(mUserProfile);

                    }
                }
            });
        }

        void setDate(String date){
            TextView dateView = mView.findViewById(R.id.post_time);
            dateView.setText(date);
        }


    }
}

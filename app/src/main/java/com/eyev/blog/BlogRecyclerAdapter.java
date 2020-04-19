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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blogList;
    private FirebaseFirestore mFirestore;
    private Context mContext;
    private FirebaseAuth mAuth;

    BlogRecyclerAdapter(List<BlogPost> blogList){
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String descData = blogList.get(position).getDescription();
        holder.setDescriptionView(descData);

        String imageUrl = blogList.get(position).getImage();
        String thumbUrl = blogList.get(position).getThumbnailUrl();
        holder.setImage(imageUrl, thumbUrl);

        final String userId = blogList.get(position).getUserId();
        mFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null){
                    String name = task.getResult().getString("name");
                    String profileUrl = task.getResult().getString("image");

                    holder.setProfile(name, profileUrl);
                }
            }
        });


        long milliseconds = blogList.get(position).getTimestamp().getTime();
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(milliseconds);
        holder.setDate(date);

        final String blogPostId = blogList.get(position).blogPostId;

        final String currentUserId = mAuth.getCurrentUser().getUid();



        mFirestore.collection("posts/" + blogPostId + "/likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(documentSnapshots != null && !documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();
                    Log.d(TAG, "onEvent: size "+count);

                    holder.updateLikesCount(count);

                } else {
                    Log.d(TAG, "onEvent: size "+0);
                    holder.updateLikesCount(0);

                }

            }
        });

        mFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot != null && documentSnapshot.exists()){

                    holder.blogLikeBtn.setImageDrawable(mContext.getDrawable(R.drawable.like_accent_btn));

                } else {

                    holder.blogLikeBtn.setImageDrawable(mContext.getDrawable(R.drawable.like_grey_btn));

                }

            }
        });

        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            HashMap<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            mFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).set(likesMap);

                        } else {

                            mFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).delete();

                        }

                    }
                });
            }
        });

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
        private ImageView blogLikeBtn;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.like_btn);

        }

        void setDescriptionView(String description){

            TextView descriptionView = mView.findViewById(R.id.post_description);
            descriptionView.setText(description);
        }

        void setImage(String downloadUrl, String thumbUrl){

            mImageView = mView.findViewById(R.id.post_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.default_image_placeholder_2);

            Glide.with(mContext).applyDefaultRequestOptions(requestOptions).load(downloadUrl).thumbnail(
                    Glide.with(mContext).load(thumbUrl)
            ).into(mImageView);
        }

        void setProfile(String name, String profileUrl){
            mUserProfile = mView.findViewById(R.id.post_user_image);
            userName = mView.findViewById(R.id.post_username);

            userName.setText(name);

            Glide.with(mContext).load(profileUrl).into(mUserProfile);

        }

        void setDate(String date){
            TextView dateView = mView.findViewById(R.id.post_time);
            dateView.setText(date);
        }


        void updateLikesCount(int count) {
            TextView blogLikeCount = mView.findViewById(R.id.like_count);
            blogLikeCount.setText(String.valueOf(count));
        }
    }
}

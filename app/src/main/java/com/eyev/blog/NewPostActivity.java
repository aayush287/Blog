package com.eyev.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class NewPostActivity extends AppCompatActivity {

    private ImageView newPostImage;
    private EditText newPostDescription;

    private Uri postImageUri;

    private FirebaseFirestore mFirestore;
    private StorageReference mStorage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Add Post");
        }

        mStorage = FirebaseStorage.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        newPostImage = findViewById(R.id.post_image);
        newPostDescription = findViewById(R.id.new_post_description);
        Button newPostBtn = findViewById(R.id.post_btn);
        final ProgressBar progressBar = findViewById(R.id.add_post_progress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropWindowSize(512, 512)
                        .setAspectRatio(2, 1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String description = newPostDescription.getText().toString();
                if (!TextUtils.isEmpty(description) && postImageUri != null){
                    progressBar.setVisibility(View.VISIBLE);

                    final String userId = mAuth.getCurrentUser().getUid();
                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = mStorage.child("post_images").child(randomName+".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                File newImageFile = new File(postImageUri.getPath());
                                Bitmap compressedImage = null;
                                try {
                                    compressedImage = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(10)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImage.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                                byte[] thumbData =baos.toByteArray();

                                final UploadTask uploadTask = mStorage.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String downloadThumbUrl = uri.toString();
                                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put("image", uri.toString());
                                                        map.put("description", description);
                                                        map.put("thumbnailUrl", downloadThumbUrl);
                                                        map.put("userId", userId);
                                                        map.put("timestamp", FieldValue.serverTimestamp());

                                                        mFirestore.collection("posts").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(NewPostActivity.this, "Post added", Toast.LENGTH_LONG).show();
                                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();
                                                                }else{
                                                                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                                                                    Toast.makeText(NewPostActivity.this, "Error:"+errorMessage,Toast.LENGTH_LONG).show();
                                                                }
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(NewPostActivity.this, "Something wrong happened, Try again", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(NewPostActivity.this, "Something wrong happened, Try again", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String errorMessage = e.getMessage();
                                        Toast.makeText(NewPostActivity.this, "Error : "+errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else{
                                String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(NewPostActivity.this, "Error:"+errorMessage,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(NewPostActivity.this, "Fields are empty",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null){
                if (resultCode == RESULT_OK) {

                    postImageUri = result.getUri();

                    newPostImage.setImageURI(postImageUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    throw new IllegalStateException(error);
                }
            }else {
                Toast.makeText(this, "Image not captured", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

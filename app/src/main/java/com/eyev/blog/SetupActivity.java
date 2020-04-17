package com.eyev.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class SetupActivity extends AppCompatActivity {


    public static final int READ_STORAGE_PERMISSION = 1;

    private CircleImageView setupImage;
    private Uri mainImage = null;
    private EditText setupName;
    private ProgressBar setupBar;
    private String userId;
    private boolean isChanged = false;

    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar mToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Account Setup");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        setupImage = findViewById(R.id.profile_image);
        setupName = findViewById(R.id.setup_name);
        Button setupBtn = findViewById(R.id.setup_save_btn);
        setupBar = findViewById(R.id.setup_progress_bar);

        if (ContextCompat.checkSelfPermission(SetupActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SetupActivity.this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);
        }

        // Retrieving the data if user is logged in
        mFirebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    if (Objects.requireNonNull(task.getResult()).exists()){
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");

                        mainImage = Uri.parse(imageUrl);

                        setupName.setText(name);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(requestOptions).load(imageUrl).into(setupImage);
                    }

                }else{
                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "Error: "+errorMessage,Toast.LENGTH_LONG).show();
                }
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here setting the profile image and name to firebase
                // Extract the name and image from view

                final String userName = setupName.getText().toString();

                // Checking if name and profile image is not empty in account
                if (!TextUtils.isEmpty(userName) && mainImage != null){
                    // set progress bar visible
                    setupBar.setVisibility(View.VISIBLE);
                    if (isChanged){
                        // Create a child reference
                        // imagePath now points to "profile_image"
                        final StorageReference imagePath = mStorageReference.child("profile_image").child(userId + ".jpg");
                        imagePath.putFile(mainImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            storeData(userName, uri);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            setupBar.setVisibility(View.INVISIBLE);
                                            Toast.makeText(SetupActivity.this, "Something wrong happened, Try again", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else {
                                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(SetupActivity.this, "Error: "+errorMessage,Toast.LENGTH_LONG).show();
                                    setupBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Uri uri = mainImage;
                        storeData(userName, uri);
                    }

                }else{
                    // show user that these field are mandatory
                    Toast.makeText(SetupActivity.this, "Profile and Name is mandatory",Toast.LENGTH_LONG).show();
                }


            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SetupActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    // Permission not granted so ask for permission
                    if (ActivityCompat.shouldShowRequestPermissionRationale(SetupActivity.this, READ_EXTERNAL_STORAGE)){
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);

                    }else{
                        Snackbar.make(v, "Permission not granted for Gallery", Snackbar.LENGTH_LONG)
                                .setAction("Grant Access", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",SetupActivity.this.getPackageName(),null);
                                        intent.setData(uri);
                                        SetupActivity.this.startActivity(intent);
                                    }
                                }).show();
                    }
                }else{
                        // Permission already granted
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(SetupActivity.this);
                }

            }
        });
    }

    private void storeData(String userName, Uri uri) {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", uri.toString());

        mFirebaseFirestore.collection("users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "Account Settings updated",Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else{
                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "Error:"+errorMessage,Toast.LENGTH_LONG).show();
                }
                setupBar.setVisibility(View.INVISIBLE);
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
                    mainImage = result.getUri(); // storing the Uri of cropped image
                    setupImage.setImageURI(mainImage); // setting the circular image

                    isChanged = true;
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

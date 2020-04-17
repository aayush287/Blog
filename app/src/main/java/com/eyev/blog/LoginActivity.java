package com.eyev.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
//    private static final String TAG = "LoginActivity";

    private EditText mEmail;
    private EditText mPassword;
    private ProgressBar mLoginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.login_password);
        Button mLoginButton = findViewById(R.id.login_button);
        Button mLoginRegButton = findViewById(R.id.need_account_button);
        mLoginProgress = findViewById(R.id.login_progress);

        // Setting the OnClickListener to listen the callbacks of login button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieving the email and password from view as string
                String loginEmail = mEmail.getText().toString();
                String loginPassword = mPassword.getText().toString();

                // If email and login password is not empty then login
                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
                    // set Progress bar to visible
                    mLoginProgress.setVisibility(View.VISIBLE);
                    // Sign in with email and password
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // When sign in is complete then check if task is successful or not
                            if (task.isSuccessful()){
                                // Call sendToMainActivity()
                                sendToMainActivity();
                            }else{
                                // If sign in is not successful then get the error message and show the toast
                                String  errorMessage;
                                if (task.getException() != null){
                                    errorMessage = task.getException().getMessage();
                                }else{
                                    errorMessage = getString(R.string.error_message);
                                }


                                Toast.makeText(LoginActivity.this, "Error: "+errorMessage, Toast.LENGTH_LONG).show();
                            }
                            // Make Progress bar invisible even if task is not successful
                            mLoginProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }else{
                    Toast.makeText(LoginActivity.this, R.string.fill_all_field_text, Toast.LENGTH_LONG).show();
                }
            }
        });

        mLoginRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating intent for Register Activity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

    }

    private void sendToMainActivity() {
        // Creating intent of main activity and starting MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get CurrentUser
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If Current User is not null then move to main activity otherwise login first
        if (currentUser != null){
            // Creating intent for main activity
            sendToMainActivity();
        }
    }
}

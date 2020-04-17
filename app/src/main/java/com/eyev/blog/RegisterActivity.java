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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar mProgressBar;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPass;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mProgressBar = findViewById(R.id.register_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        // Initializing variables
        mEmail = findViewById(R.id.email_register);
        mPassword = findViewById(R.id.register_password);
        mConfirmPass = findViewById(R.id.register_confirm_password);
        Button mCreateAccount = findViewById(R.id.register_button);
        mCreateAccount.setOnClickListener(this);       // Calling onClick() method
        Button mLogin = findViewById(R.id.reg_login_button);
        mLogin.setOnClickListener(this);                // Calling onClick() method

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_button:
                createAccount();
                break;
            case R.id.reg_login_button:
                finish();
                break;
            default:
                break;
        }
    }

    private void createAccount() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPass = mConfirmPass.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPass)){
            // Check if password match with Confirm Password
            if (password.equals(confirmPass)){
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // Send it to SetupActivity
                            Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                            startActivity(setupIntent);

                        }else {
                            // If sign in is not successful then get the error message and show the toast
                            String  errorMessage;
                            if (task.getException() != null){
                                errorMessage = task.getException().getMessage();
                            }else{
                                errorMessage = getString(R.string.error_message);
                            }
                               Toast.makeText(RegisterActivity.this, "Error: "+errorMessage, Toast.LENGTH_LONG).show();
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }else{
                // Password doesn't match so make a toast
                Toast.makeText(RegisterActivity.this, R.string.password_unmatch, Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(RegisterActivity.this, R.string.fill_all_field_text, Toast.LENGTH_LONG).show();
        }

    }
}

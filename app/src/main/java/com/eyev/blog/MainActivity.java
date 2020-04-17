package com.eyev.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
    private HomeFragment mHomeFragment;
    private NotificationFragment mNotificationFragment;
    private AccountFragment mAccountFragment;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // we are using androidx.widget.Toolbar so we will use
        // setSupportActionBar() instead of setActionBar()
        Toolbar mMainToolbar =  findViewById(R.id.main_toolbar);
        setSupportActionBar(mMainToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Blog");
        }

        FloatingActionButton addPost = findViewById(R.id.add_post);
        BottomNavigationView mBottomNavigationView = findViewById(R.id.main_bottom_nav);

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });

        mHomeFragment = new HomeFragment();
        mNotificationFragment = new NotificationFragment();
        mAccountFragment = new AccountFragment();

//        replaceFragment(mHomeFragment);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_home:
                        replaceFragment(mHomeFragment);
                        return true;
                    case R.id.bottom_notification:
                        replaceFragment(mNotificationFragment);
                        return true;
                    case R.id.bottom_account:
                        replaceFragment(mAccountFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
           // User is not logged in, then send user to login page
            sendToLogin();
        }else{
            String current_user_id = currentUser.getUid();
            mFirestore.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult() != null && !task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }else{
                            replaceFragment(mHomeFragment);
                        }

                    }else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : "+errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });



        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_search_btn:
                //TODO Implement search function
                break;
            case R.id.action_settings_btn:
                // send user to setup activity
                Intent setupActivity = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setupActivity);
                break;
            case R.id.action_logout_btn:
                // Logout the account by calling logOut() method
                logOut();
                break;
            default:
                throw new IllegalStateException("No view found to corresponding item");
        }

        return true;
    }

    private void logOut() {
        // Logging out the user
        mAuth.signOut();
        // User logged out, send user to login page
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}

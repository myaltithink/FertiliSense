package com.fertilisense.fertilisense;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private TextView textViewWelcome, textViewUsername, textViewEmail,
            textViewGender, textViewAge, textViewDOB, textViewPhone;

    private ImageView imageView;

    private String username, email, gender, age, date, phone;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("User Profile Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); //custom back button drawable
        }

        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu)); //custom overflow icon

        textViewWelcome = findViewById(R.id.username_settings);
        textViewUsername = findViewById(R.id.show_username_settings);
        textViewEmail = findViewById(R.id.show_email_settings);
        textViewGender = findViewById(R.id.show_gender_settings);
        textViewAge = findViewById(R.id.show_age_settings);
        textViewDOB = findViewById(R.id.show_dob_settings);
        textViewPhone = findViewById(R.id.show_phone_settings);

        //For uploading profile Picture
        imageView = findViewById(R.id.profile_picture_settings);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePictureActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UserProfileActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
        }
    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        // Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Verify your email now");

        // Open email app
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Show the AlertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadwriteUserDetails readUserDetails = snapshot.getValue(ReadwriteUserDetails.class);
                if (readUserDetails != null) {
                    username = readUserDetails.username;
                    email = firebaseUser.getEmail();
                    gender = readUserDetails.gender;
                    age = readUserDetails.age;
                    date = readUserDetails.date;
                    phone = readUserDetails.phone;

                    Log.d(TAG, "Username: " + username); // Debugging username

                    textViewWelcome.setText(username);
                    textViewUsername.setText(username);
                    textViewEmail.setText(email);
                    textViewGender.setText(gender);
                    textViewAge.setText(age);
                    textViewDOB.setText(date);
                    textViewPhone.setText(phone);

                    //Set User Dp after uploading in database
                    Uri uri = firebaseUser.getPhotoUrl();

                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);

                } else {
                    Log.d(TAG, "No data found for user with ID: " + userID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database error: " + error.getMessage());
                Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Action Bar Common Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: Menu inflated.");
        return true; // Ensure to return true here
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: Menu item selected with ID " + id);

        if(id == android.R.id.home){
            //Back Button
            NavUtils.navigateUpFromSameTask(UserProfileActivity.this);
        } else if (id == R.id.common_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            startActivity(getIntent());
            finish();
        } else if (id == R.id.common_menu_update_profile) {
            // Handle update profile
            Log.d(TAG, "onOptionsItemSelected: Update Profile selected.");
            Intent intent = new Intent(UserProfileActivity.this, UpdateUserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_change_password) {
            // Handle change password
            Log.d(TAG, "onOptionsItemSelected: Change Password selected.");
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_delete_account) {
            // Handle delete account
            Log.d(TAG, "onOptionsItemSelected: Delete Account selected.");
            Intent intent = new Intent(this, DeleteProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
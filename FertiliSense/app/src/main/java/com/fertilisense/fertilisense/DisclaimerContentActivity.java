package com.fertilisense.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisclaimerContentActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private Button toggleScrollButton;
    private FirebaseAuth authProfile;
    private FirebaseUser currentUser;
    private DatabaseReference userDatabaseReference;
    private boolean isAtBottom = false;
    private static final String TAG = "DisclaimerContentActivity";
    private String userGender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer_content);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Disclaimer Content");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); // Custom back button drawable
        }

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        currentUser = authProfile.getCurrentUser();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        // Log currently logged-in user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            Log.d(TAG, "Logged in as: " + email);

            // Fetch user's gender
            fetchUserGender(currentUser.getUid());
        }

        scrollView = findViewById(R.id.scrollView);
        toggleScrollButton = findViewById(R.id.toggle_scroll_button);

        toggleScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtBottom) {
                    scrollView.fullScroll(View.FOCUS_UP);
                    toggleScrollButton.setText("Scroll to Bottom");
                } else {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    toggleScrollButton.setText("Scroll to Top");
                }
                isAtBottom = !isAtBottom;
            }
        });
    }

    // Fetch the user's gender from Firebase
    private void fetchUserGender(String uid) {
        userDatabaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userGender = snapshot.child("gender").getValue(String.class);
                    if (userGender != null) {
                        Log.d(TAG, "User gender: " + userGender);
                    } else {
                        Log.e(TAG, "Gender information missing");
                    }
                } else {
                    Log.e(TAG, "User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to retrieve user gender: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Back button pressed, navigating based on user gender");

            if (userGender != null) {
                if (userGender.equalsIgnoreCase("female")) {
                    navigateToActivity(FertiliSenseDashboardActivity.class);
                } else if (userGender.equalsIgnoreCase("male")) {
                    navigateToActivity(MaleDashboardActivity.class);
                } else {
                    Log.e(TAG, "Invalid gender value");
                }
            } else {
                Log.e(TAG, "User gender not set yet");
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Navigate to the correct dashboard based on gender
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(DisclaimerContentActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);  // Removes transition animations
        finish();
    }
}
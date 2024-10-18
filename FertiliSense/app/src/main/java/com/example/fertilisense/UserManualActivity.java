package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserManualActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private Button toggleScrollButton;
    private FirebaseAuth authProfile;
    private FirebaseUser currentUser;
    private DatabaseReference userDatabaseReference;
    private boolean isAtBottom = false;
    private static final String TAG = "UserManualActivity";
    private String userGender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Users Manual");
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

        scrollView = findViewById(R.id.scroll_view);
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

    // Fetch the user's gender from the Firebase database
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
        Intent intent = new Intent(UserManualActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);  // This removes transition animations
        finish();
    }

}
package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class FeedbackActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference feedbackRef;
    private DatabaseReference userDatabaseReference;
    private EditText emailFeedbackEditText;
    private EditText userFeedbackEditText;
    private static final String TAG = "FeedbackActivity";
    private String userGender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Feedback Form");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); // Custom back button drawable
        }

        auth = FirebaseAuth.getInstance();
        feedbackRef = FirebaseDatabase.getInstance().getReference("Feedbacks"); // Node in Firebase Realtime Database
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        emailFeedbackEditText = findViewById(R.id.email_feedback);
        userFeedbackEditText = findViewById(R.id.user_feedback);

        Button feedbackButton = findViewById(R.id.feedback_button);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

        // Load current user's email into the email feedback field
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            emailFeedbackEditText.setText(currentUser.getEmail());
            emailFeedbackEditText.setEnabled(false); // Make it read-only

            // Fetch user's gender
            fetchUserGender(currentUser.getUid());
        }
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

    private void submitFeedback() {
        String email = emailFeedbackEditText.getText().toString().trim();
        String feedback = userFeedbackEditText.getText().toString().trim();

        if (TextUtils.isEmpty(feedback)) {
            Log.d(TAG, "No written feedback");
            Toast.makeText(this, "Please provide your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Feedback object
        Feedback feedbackObject = new Feedback(email, feedback);

        // Save feedback to Firebase Realtime Database
        feedbackRef.push().setValue(feedbackObject).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Feedback submitted successfully");
                Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();

                // Navigate back to the appropriate dashboard based on gender
                navigateToDashboard();
            } else {
                Toast.makeText(FeedbackActivity.this, "Failed to submit feedback. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard() {
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
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(FeedbackActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0); // Removes transition animations
        finish(); // Close FeedbackActivity
    }

    private static class Feedback {
        public String email;
        public String feedback;

        public Feedback(String email, String feedback) {
            this.email = email;
            this.feedback = feedback;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Back button pressed, navigating based on user gender");
            navigateToDashboard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
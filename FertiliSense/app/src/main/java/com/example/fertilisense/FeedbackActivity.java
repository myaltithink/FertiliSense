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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference feedbackRef;
    private EditText emailFeedbackEditText;
    private EditText userFeedbackEditText;
    private static final String TAG = "FeedbackActivity";

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
        }
    }

    private void submitFeedback() {
        String email = emailFeedbackEditText.getText().toString().trim();
        String feedback = userFeedbackEditText.getText().toString().trim();

        if (TextUtils.isEmpty(feedback)) {
            Log.d("FertiliSense","No written feedbacks");
            Toast.makeText(this, "Please provide your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Feedback object
        Feedback feedbackObject = new Feedback(email, feedback);

        // Save feedback to Firebase Realtime Database
        feedbackRef.push().setValue(feedbackObject).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FertiliSense", "Navigationg to FertiliSenseDashboardActivity");
                Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();

                // Navigate back to FertiliSenseDashboardActivity
                Intent intent = new Intent(FeedbackActivity.this, FertiliSenseDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish(); // Close FeedbackActivity
            } else {
                Toast.makeText(FeedbackActivity.this, "Failed to submit feedback. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
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
            Log.d("FertiliSense", "Navigationg to Dashboard");
            Intent intent = new Intent(FeedbackActivity.this, FertiliSenseDashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

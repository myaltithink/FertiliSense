package com.example.fertilisense;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TermsAndConditionContentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private DatabaseReference userDatabaseReference;
    private ScrollView scrollView;
    private Button toggleScrollButton;
    private ImageView saveIcon;
    private boolean isAtBottom = false;
    private String userGender = null; // Added userGender variable

    private static final String TAG = "TermsAndConditionContentActivity";

    // Declare TextViews
    private TextView titleTextView, subtitleTextView, rule1TextView, rule2TextView, rule3TextView, rule4TextView, rule5TextView, rule6TextView, rule7TextView, rule8TextView, rule9TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_condition_content);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("TermsAndCondition");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Registered Users"); // Reference for user data

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Terms and Conditions");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); // Custom back button drawable
        }

        scrollView = findViewById(R.id.scrollView);
        toggleScrollButton = findViewById(R.id.toggle_scroll_button);
        saveIcon = findViewById(R.id.ic_save);

        // Initialize TextViews
        titleTextView = findViewById(R.id.title);
        subtitleTextView = findViewById(R.id.subtitle);
        rule1TextView = findViewById(R.id.rule_1);
        rule2TextView = findViewById(R.id.rule_2);
        rule3TextView = findViewById(R.id.rule_3);
        rule4TextView = findViewById(R.id.rule_4);
        rule5TextView = findViewById(R.id.rule_5);
        rule6TextView = findViewById(R.id.rule_6);
        rule7TextView = findViewById(R.id.rule_7);
        rule8TextView = findViewById(R.id.rule_8);
        rule9TextView = findViewById(R.id.rule_9);

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

        saveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Save icon clicked");
                saveTermsAndConditions();
            }
        });

        // Load user gender
        loadUserGender();
    }

    private void loadUserGender() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userDatabaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userGender = snapshot.child("gender").getValue(String.class);
                        Log.d(TAG, "User gender: " + userGender);
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
    }

    private void saveTermsAndConditions() {
        // Get the current user
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user logged in");
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();

        // Get the current date and time
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Generate PDF file
        File pdfFile = new File(getExternalFilesDir(null), "terms_and_conditions.pdf");
        try {
            PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            // Add title and subtitle
            document.add(new Paragraph(titleTextView.getText().toString())
                    .setFontSize(18)
                    .setBold());
            document.add(new Paragraph(subtitleTextView.getText().toString())
                    .setFontSize(16));

            // Add each rule
            document.add(new Paragraph(rule1TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule2TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule3TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule4TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule5TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule6TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule7TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule8TextView.getText().toString())
                    .setFontSize(16));
            document.add(new Paragraph(rule9TextView.getText().toString())
                    .setFontSize(16));

            document.add(new Paragraph("Email: " + email));
            document.add(new Paragraph("Date and Time: " + dateTime));

            document.close();
            Log.d(TAG, "PDF created successfully at " + pdfFile.getAbsolutePath());

            // Upload user details to Firebase
            uploadUserDetails(email, dateTime);

            // Show success message with file path
            Toast.makeText(this, "Terms and conditions saved successfully at " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Optionally, open the PDF file
            openPdfFile(pdfFile);

        } catch (IOException e) {
            Log.e(TAG, "Failed to save terms and conditions", e);
            Toast.makeText(this, "Failed to save terms and conditions", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file) {
        // Ensure this matches the authority specified in AndroidManifest.xml
        Uri pdfUri = FileProvider.getUriForFile(this, "com.example.fertilisense.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(Intent.createChooser(intent, "Open PDF with"));
    }

    private void uploadUserDetails(String email, String dateTime) {
        // Create a new record with the current timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference userDetailsRef = databaseRef.child(timestamp);

        // Create a map of user details
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("email", email);
        userDetails.put("dateTime", dateTime);

        // Upload to Firebase
        userDetailsRef.setValue(userDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User details uploaded to Firebase successfully");
            } else {
                Log.e(TAG, "Failed to upload user details to Firebase", task.getException());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d("FertiliSense", "Navigating to Dashboard");

            // Navigate based on user gender
            Intent intent;
            if ("male".equalsIgnoreCase(userGender)) {
                intent = new Intent(TermsAndConditionContentActivity.this, MaleDashboardActivity.class);
            } else {
                intent = new Intent(TermsAndConditionContentActivity.this, FertiliSenseDashboardActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private ScrollView scrollView;
    private Button toggleScrollButton;
    private ImageView saveIcon;
    private boolean isAtBottom = false;

    private static final String TAG = "TermsAndConditionContentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_condition_content);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("TermsAndCondition");

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

            document.add(new Paragraph("Terms and Conditions Details")
                    .setFontSize(18)
                    .setBold());

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
            Intent intent = new Intent(TermsAndConditionContentActivity.this, FertiliSenseDashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

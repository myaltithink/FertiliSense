package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.squareup.picasso.Picasso;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import android.content.pm.PackageManager;

import android.os.Environment;
import java.io.FileOutputStream;

import android.graphics.pdf.PdfDocument;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.Manifest; // Import Manifest class
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; // Ensure this import is present
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue; // Add this import

public class ReportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int STORAGE_PERMISSION_CODE = 100;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private String appPackageName;

    private static final String TAG = "ReportActivity";
    private MaterialCalendarView calendarView;
    private FirebaseFirestore db;

    private TextView cyclesInfo, symptomsInfo; // Declare TextViews
    private Button downloadButton; // Declare Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize FirebaseAuth and Firestore
        authProfile = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize navigation components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Initialize TextViews
        cyclesInfo = findViewById(R.id.cycles_info);
        symptomsInfo = findViewById(R.id.symptoms_info);

        // Initialize download button
        downloadButton = findViewById(R.id.download_report);
        downloadButton.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadReport();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        });

        // Load user information in the navigation header
        loadUserInformation();

        fetchMenstrualCycleData();
        fetchSymptomData();

        // Set up the drawer and bottom navigation listeners
        findViewById(R.id.ic_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Intent intent = new Intent(ReportActivity.this, FemaleReproductiveSystemActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.calendar) {
                    Intent intent = new Intent(ReportActivity.this, CalendarActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.home) {
                    Intent intent = new Intent(ReportActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Intent intent = new Intent(ReportActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    Log.d("FertiliSense", "User report details clicked");
                }

                return true;
            }
        });

        // Set "Report" as the default selected item in bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.report);
    }

    private void downloadReport() {
        // Create the PDF document
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setColor(Color.BLACK);

        String reportContent = "Menstrual Cycle Data:\n" +
                cyclesInfo.getText().toString() +
                "\nSymptom Data:\n" +
                symptomsInfo.getText().toString();

        canvas.drawText(reportContent, 10, 25, paint);
        pdfDocument.finishPage(page);

        // Save the report to the Firebase Realtime Database
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportContent", reportContent);
        reportData.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue to add a timestamp

        String userId = authProfile.getCurrentUser().getUid();
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports").child(userId);

        reportsRef.push().setValue(reportData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Report saved to Firebase Realtime Database", Toast.LENGTH_SHORT).show();
                // Now save the PDF file locally
                savePdf(pdfDocument);
            } else {
                Toast.makeText(this, "Failed to save report: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                pdfDocument.close(); // Ensure document is closed if the Firebase save fails
            }
        });
    }

    private void savePdf(PdfDocument pdfDocument) {
        File reportFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "report.pdf"); // Use app-specific directory

        // Create directory if it doesn't exist
        if (!reportFile.getParentFile().exists()) {
            reportFile.getParentFile().mkdirs();
        }

        try {
            // Write the PDF document to the file
            pdfDocument.writeTo(new FileOutputStream(reportFile));
            Toast.makeText(this, "Successfully Downloaded Report: " + reportFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close(); // Ensure document is closed
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadReport();
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void fetchMenstrualCycleData() {
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Fetching menstrual cycle data for user ID: " + userId);

            db.collection("menstrual_cycles")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Log the entire document
                                Log.d(TAG, "Document data: " + document.getData());

                                // Get the cycles list from Firestore
                                List<Map<String, Object>> cycles = (List<Map<String, Object>>) document.get("cycles");
                                if (cycles != null && !cycles.isEmpty()) {
                                    StringBuilder cyclesInfo = new StringBuilder(); // To hold all cycle info

                                    // Iterate through each cycle and log the information
                                    for (Map<String, Object> cycle : cycles) {
                                        String cycleDuration = (String) cycle.get("cycle_duration");
                                        String endDate = (String) cycle.get("end_date");
                                        String periodDuration = (String) cycle.get("period_duration");
                                        String startDate = (String) cycle.get("start_date");

                                        // Log each cycle's information
                                        Log.d(TAG, "Cycle Duration: " + cycleDuration);
                                        Log.d(TAG, "End Date: " + endDate);
                                        Log.d(TAG, "Period Duration: " + periodDuration);
                                        Log.d(TAG, "Start Date: " + startDate);

                                        // Append the information to cyclesInfo in the desired order
                                        cyclesInfo.append("Start Date: ").append(startDate != null ? startDate : "N/A").append("\n")
                                                .append("End Date: ").append(endDate != null ? endDate : "N/A").append("\n")
                                                .append("Cycle Duration: ").append(cycleDuration != null ? cycleDuration : "N/A").append("\n")
                                                .append("Period Duration: ").append(periodDuration != null ? periodDuration : "N/A").append("\n\n");

                                    }

                                    // Update the UI on the main thread
                                    runOnUiThread(() -> {
                                        TextView cyclesTextView = findViewById(R.id.cycles_info); // Assuming you have a TextView to display all cycle info
                                        cyclesTextView.setText(cyclesInfo.toString());
                                    });
                                } else {
                                    Log.d(TAG, "No cycles found for user ID: " + userId);
                                }
                            } else {
                                Log.d(TAG, "No menstrual cycle data found for user ID: " + userId);
                            }
                        } else {
                            Log.e(TAG, "Error fetching menstrual cycle data: " + task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "No user is signed in");
        }
    }






    private void fetchSymptomData() {
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Fetching symptom data for user ID: " + userId);

            db.collection("symptom_logs")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Log the entire document
                                Log.d(TAG, "Document data: " + document.getData());

                                // Get the logs list from Firestore
                                List<Map<String, Object>> logs = (List<Map<String, Object>>) document.get("logs");
                                if (logs != null && !logs.isEmpty()) {
                                    StringBuilder symptomsInfo = new StringBuilder(); // To hold all symptom info

                                    // Iterate through each log and log the information
                                    for (Map<String, Object> log : logs) {
                                        String symptom = (String) log.get("symptoms");
                                        String startDate = (String) log.get("start_date");
                                        String endDate = (String) log.get("end_date");

                                        // Log each symptom's information
                                        Log.d(TAG, "Symptom Start: " + startDate);
                                        Log.d(TAG, "Symptom End: " + endDate);
                                        Log.d(TAG, "Symptom: " + symptom);

                                        // Append the information to symptomsInfo
                                        symptomsInfo.append("Start Date: ").append(startDate != null ? startDate : "N/A").append("\n")
                                                .append("End Date: ").append(endDate != null ? endDate : "N/A").append("\n")
                                                .append("Symptom: ").append(symptom != null ? symptom : "N/A").append("\n\n");
                                    }

                                    // Update the UI on the main thread
                                    runOnUiThread(() -> {
                                        TextView symptomsTextView = findViewById(R.id.symptoms_info); // Assuming you have a TextView to display symptoms
                                        symptomsTextView.setText(symptomsInfo.toString());
                                    });
                                } else {
                                    Log.d(TAG, "No logs found for user ID: " + userId);
                                }
                            } else {
                                Log.d(TAG, "No symptom data found for user ID: " + userId);
                            }
                        } else {
                            Log.e(TAG, "Error fetching symptom data: " + task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "No user is signed in");
        }
    }






    private void loadUserInformation() {
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_drawer_username);
        TextView emailTextView = headerView.findViewById(R.id.nav_drawer_email);
        ImageView profilePictureView = findViewById(R.id.nav_header_profile_picture); // Add this

        FirebaseUser user = authProfile.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            emailTextView.setText(email);

            // Fetch username from the database
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userId = user.getUid();
            Log.d("FertiliSense", "Fetching data for user ID: " + userId);

            referenceProfile.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        if (username != null && !username.isEmpty()) {
                            usernameTextView.setText(username);
                        }

                        // Fetch the profile picture from FirebaseUser
                        Uri photoUri = user.getPhotoUrl();
                        if (photoUri != null) {
                            Picasso.with(ReportActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .transform(new CircleTransform()) // Make the image circular
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(ReportActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ReportActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Set a click listener to open UserProfileActivity
            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(ReportActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.d("FertiliSense", "No current user logged in");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.share_us) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Download this app: https://play.google.com/store/apps/details?id=" + appPackageName);
            startActivity(Intent.createChooser(intent, "Share this app"));
        } else if (id == R.id.rate_us) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.feedback) {
            Intent intent = new Intent(ReportActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.privacy) {
            Intent intent = new Intent(ReportActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.disclaimer_content) {
            Intent intent = new Intent(ReportActivity.this, DisclaimerContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.terms_condition) {
            Intent intent = new Intent(ReportActivity.this, TermsAndConditionContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Intent intent = new Intent(ReportActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


}
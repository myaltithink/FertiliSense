package com.example.fertilisense;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.TimeUnit;


public class FertiliSenseDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private FirebaseFirestore db;

    private TextView dateNow;
    private TextView nextOvulationLeft;
    private TextView nextOvulationDate;

    private String appPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertili_sense_dashboard);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Current Date
        dateNow = findViewById(R.id.date_now);
        setCurrentDate();

        // Next ovulation date and how many days left
        nextOvulationLeft = findViewById(R.id.next_ovulation_left);
        nextOvulationDate = findViewById(R.id.next_ovulation_date);
        setNextOvulationInfo();

        // Load user information in the navigation header
        loadUserInformation();

        findViewById(R.id.ic_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Log.d("FertiliSense", "Navigating to FemaleReproductiveSystemActivity");
                    Intent intent = new Intent(FertiliSenseDashboardActivity.this, FemaleReproductiveSystemActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.calendar) {
                    Log.d("FertiliSense", "Checking cycle logs before navigating to CalendarActivity");
                    checkForLoggedCyclesAndShowDialog();
                } else if (id == R.id.home) {
                    // Handle "Home" action
                    Log.d("FertiliSense", "Already on the Home screen");
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(FertiliSenseDashboardActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    Log.d("FertiliSense", "User report details clicked");
//                    Intent intent = new Intent(ReportActivity.this, ChatBotActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                    finish();
                }

                return true;
            }
        });

        // Set "Home" as the default selected item
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    // Method for setting the current date
    private void setCurrentDate() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault()); // Format: August 20
        String currentDate = dateFormat.format(calendar.getTime());

        // Set the current date to TextView
        dateNow.setText(currentDate);
    }

    // Method to set next ovulation information
    private void setNextOvulationInfo() {
        // Assuming you have menstrual cycle data
        int cycleLength = 28; // Example cycle length (in days)
        int lutealPhaseLength = 14; // Example luteal phase length (in days)

        // Get current date
        Calendar calendar = Calendar.getInstance();

        // Calculate the next ovulation date
        Calendar nextOvulationDateCalendar = Calendar.getInstance();
        nextOvulationDateCalendar.setTime(calendar.getTime());

        // Assuming the last period started today
        nextOvulationDateCalendar.add(Calendar.DAY_OF_MONTH, cycleLength - lutealPhaseLength);

        // Set next ovulation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
        String nextOvulation = dateFormat.format(nextOvulationDateCalendar.getTime());
        nextOvulationDate.setText(nextOvulation);

        // Calculate remaining days until ovulation
        long diffInMillis = nextOvulationDateCalendar.getTimeInMillis() - calendar.getTimeInMillis();
        long remainingDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        // Set remaining days until ovulation
        nextOvulationLeft.setText(remainingDays + " days left");
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
                            Picasso.with(FertiliSenseDashboardActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .transform(new CircleTransform()) // Make the image circular
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(FertiliSenseDashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FertiliSenseDashboardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Set a click listener to open UserProfileActivity
            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(FertiliSenseDashboardActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.d("FertiliSense", "No current user logged in");
        }
    }

    private void checkForLoggedCyclesAndShowDialog() {
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("menstrual_cycles")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            showCycleLogDialog();
                        } else {
                            navigateToCalendarActivity();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FertiliSenseDashboardActivity.this, "Error checking cycle data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCycleLogDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Menstrual Cycle")
                .setMessage("You haven't logged any menstrual cycle yet. Would you like to log it now?")
                .setPositiveButton("OK", (dialog, which) -> navigateToChatBotActivity())
                .setNegativeButton("Cancel", (dialog, which) -> navigateToCalendarActivity())
                .setCancelable(false)
                .show();
    }

    private void navigateToCalendarActivity() {
        Intent intent = new Intent(FertiliSenseDashboardActivity.this, CalendarActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void navigateToChatBotActivity() {
        Intent intent = new Intent(FertiliSenseDashboardActivity.this, ChatBotActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.share_us) {
            Log.d("FertiliSense", "Share Us clicked");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Download this app: https://play.google.com/store/apps/details?id=" + appPackageName);
            startActivity(Intent.createChooser(intent, "Share this app"));
            finish();
        } else if (id == R.id.rate_us) {
            Log.d("FertiliSense", "Rate Us clicked");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
            finish();
        } else if (id == R.id.feedback) {
            Log.d("FertiliSense", "Feedback clicked");
            Intent intent = new Intent(FertiliSenseDashboardActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.privacy) {
            Log.d(TAG, "Privacy Policy clicked");
            Intent intent = new Intent(FertiliSenseDashboardActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.disclaimer_content) {
            Log.d(TAG, "Disclaimer clicked");
            Intent intent = new Intent(FertiliSenseDashboardActivity.this, DisclaimerContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.terms_condition) {
            Log.d(TAG, "Terms and Condition clicked");
            Intent intent = new Intent(FertiliSenseDashboardActivity.this, TermsAndConditionContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            // Handle "LOG OUT" action
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FertiliSenseDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
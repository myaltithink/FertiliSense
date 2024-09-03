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

public class FertiliSenseDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;

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
                    Log.d("FertiliSense", "Calendar clicked");
                    // Uncomment and modify when CalendarActivity is ready
                    // Intent intent = new Intent(FertiliSenseDashboardActivity.this, CalendarActivity.class);
                    // startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // finish();
                } else if (id == R.id.home) {
                    // Handle "Home" action
                    Log.d("FertiliSense", "Already on the Home screen");
                } else if (id == R.id.search) {
                    Log.d("FertiliSense", "Search clicked");
                    // Uncomment and modify when SearchActivity is ready
                    // Intent intent = new Intent(FertiliSenseDashboardActivity.this, SearchActivity.class);
                    // startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // finish();
                } else if (id == R.id.symptoms) {
                    Log.d("FertiliSense", "Symptoms clicked");
                    // Uncomment and modify when SymptomsActivity is ready
                    // Intent intent = new Intent(FertiliSenseDashboardActivity.this, SymptomsActivity.class);
                    // startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // finish();
                }

                return true;
            }
        });

        // Set "Home" as the default selected item
        bottomNavigationView.setSelectedItemId(R.id.home);

        ImageView chatBot = findViewById(R.id.ic_chatbot);

        // Set the onClickListener for the back button
        chatBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to ChatBot");
                Intent intent = new Intent(FertiliSenseDashboardActivity.this, ChatBotActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void loadUserInformation() {
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_drawer_username);
        TextView emailTextView = headerView.findViewById(R.id.nav_drawer_email);

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
                    Log.d("FertiliSense", "DataSnapshot exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        Log.d("FertiliSense", "Username from snapshot: " + username);
                        if (username != null && !username.isEmpty()) {
                            Log.d("FertiliSense", "Fetched username: " + username);
                            usernameTextView.setText(username);
                        } else {
                            Log.d("FertiliSense", "Username is null or empty");
                        }
                    } else {
                        Log.d("FertiliSense", "User data not found");
                        Toast.makeText(FertiliSenseDashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FertiliSenseDashboardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.e("FertiliSense", "Database error: " + error.getMessage());
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

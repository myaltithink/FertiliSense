package com.fertilisense.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GlansActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private FirebaseUser currentUser;
    private static final String TAG = "GlansActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glans);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Glans Activity");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); // Custom back button drawable
        }

        // Set custom overflow icon
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        currentUser = authProfile.getCurrentUser();

        // Log currently logged-in user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            Log.d(TAG, "Logged in as: " + email);
        }

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Log.d(TAG, "Navigating to MaleReproductiveSystemActivity");
                    Intent intent = new Intent(GlansActivity.this, MaleReproductiveSystemActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.calendar) {
                    Log.d(TAG, "Calendar clicked");
                    Intent intent = new Intent(GlansActivity.this, CalendarActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.home) {
                    Log.d(TAG, "Dashboard clicked");
                    Intent intent = new Intent(GlansActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(GlansActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    Log.d("FertiliSense", "User report details clicked");
                    Intent intent = new Intent(GlansActivity.this, ReportActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                return true;
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.male_menu, menu);
        return true;
    }

    // Handle action bar item clicks here.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle different menu item clicks based on their IDs
        if (id == android.R.id.home) {
            // Back Button
            Intent intent = new Intent(GlansActivity.this, MaleReproductiveSystemActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.male_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.male_menu_seminal_vesicle) {
            Log.d(TAG, "onOptionsItemSelected: Seminal Vesicle selected.");
            Intent intent = new Intent(this, SeminalVesicleActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_bladder) {
            Log.d(TAG, "onOptionsItemSelected: Bladder selected.");
            Intent intent = new Intent(this, BladderActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_bulbourethral_gland) {
            Log.d(TAG, "onOptionsItemSelected: Bulbourethral Gland selected.");
            Intent intent = new Intent(this, BulbourethralGlandActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_testicles) {
            Log.d(TAG, "onOptionsItemSelected: Testicles selected.");
            Intent intent = new Intent(this, TesticlesActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_urethral_opening) {
            Log.d(TAG, "onOptionsItemSelected: Urethral Opening selected.");
            Intent intent = new Intent(this, UrethralOpeningActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_epididymis) {
            Log.d(TAG, "onOptionsItemSelected: Epididymis selected.");
            Intent intent = new Intent(this, EpididymisActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_prostrate_gland) {
            Log.d(TAG, "onOptionsItemSelected: Prostrate Gland selected.");
            Intent intent = new Intent(this, ProstrateGlandActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_ductus_vas_deferens) {
            Log.d(TAG, "onOptionsItemSelected: Ductus (Vas) Deferens selected.");
            Intent intent = new Intent(this, DuctusVasDeferensActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_penis) {
            Log.d(TAG, "onOptionsItemSelected: Penis selected.");
            Intent intent = new Intent(this, PenisActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.male_menu_glans) {
            Log.d(TAG, "onOptionsItemSelected: Penis selected.");
            Intent intent = new Intent(this, GlansActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        }

        // If none of the above conditions are met, delegate to superclass for handling
        return super.onOptionsItemSelected(item);
    }
}
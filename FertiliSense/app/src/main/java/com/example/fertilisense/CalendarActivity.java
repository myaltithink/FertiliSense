package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private String appPackageName;

    private static final String TAG = "CalendarActivity";
    private MaterialCalendarView calendarView;
    private Button editPeriodButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize FirebaseAuth and Firestore
        authProfile = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        calendarView = findViewById(R.id.calendarView);
        editPeriodButton = findViewById(R.id.editPeriodButton);

        // Initialize navigation components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Set up the drawer and bottom navigation listeners
        findViewById(R.id.ic_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Intent intent = new Intent(CalendarActivity.this, FemaleReproductiveSystemActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.calendar) {
                    // Already in CalendarActivity
                } else if (id == R.id.home) {
                    Intent intent = new Intent(CalendarActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Intent intent = new Intent(CalendarActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    // Placeholder for report action
                }

                return true;
            }
        });

        // Set "Calendar" as the default selected item in bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.calendar);

        // Set up click listener for the Edit Period button
        editPeriodButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, EditPeriodActivity.class);
            startActivity(intent);
        });

        // Fetch cycle data and highlight on the calendar
        fetchCycleData();
    }

    private void fetchCycleData() {
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("menstrual_cycles")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Your cycle data handling logic
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "Error getting document: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.d(TAG, "No user is signed in");
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
        } else if (id == R.id.feedback) {
            Intent intent = new Intent(CalendarActivity.this, FeedbackActivity.class);
            startActivity(intent);
        } else if (id == R.id.privacy) {
            Intent intent = new Intent(CalendarActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        } else if (id == R.id.disclaimer_content) {
            Intent intent = new Intent(CalendarActivity.this, DisclaimerContentActivity.class);
            startActivity(intent);
        } else if (id == R.id.terms_condition) {
            Intent intent = new Intent(CalendarActivity.this, TermsAndConditionContentActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}


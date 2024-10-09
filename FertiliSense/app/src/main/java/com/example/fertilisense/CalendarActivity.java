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
    //private Button editPeriodButton;
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
        //editPeriodButton = findViewById(R.id.editPeriodButton);

        // Initialize navigation components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Load user information in the navigation header
        loadUserInformation();

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
                    Log.d("FertiliSense", "Calendar clicked");
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
                    Log.d("FertiliSense", "User report details clicked");
                    Intent intent = new Intent(CalendarActivity.this, ReportActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                return true;
            }
        });

        // Set "Calendar" as the default selected item in bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.calendar);

        // Fetch cycle data and highlight on the calendar
        fetchCycleData();
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
                            Picasso.with(CalendarActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .transform(new CircleTransform()) // Make the image circular
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(CalendarActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CalendarActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Set a click listener to open UserProfileActivity
            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(CalendarActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.d("FertiliSense", "No current user logged in");
        }
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
                                    // Initialize collections for dates
                                    Set<CalendarDay> cycleDates = new HashSet<>();
                                    Set<CalendarDay> cycleDurationDates = new HashSet<>();
                                    Set<CalendarDay> fertileWindowDates = new HashSet<>();
                                    Set<CalendarDay> ovulationDates = new HashSet<>();
                                    Set<CalendarDay> nextCycleStartDates = new HashSet<>();
                                    Set<CalendarDay> strongFlowDates = new HashSet<>();
                                    Set<CalendarDay> predictionCycleDates = new HashSet<>();
                                    Set<CalendarDay> cycleEndDates = new HashSet<>();
                                    Set<CalendarDay> predictionCycleEndDates = new HashSet<>();

                                    // Process cycles
                                    List<Map<String, Object>> cycles = (List<Map<String, Object>>) document.get("cycles");
                                    if (cycles != null) {
                                        for (Map<String, Object> cycle : cycles) {
                                            String startDateStr = (String) cycle.get("start_date");
                                            String endDateStr = (String) cycle.get("end_date");
                                            String cycleDurationStr = (String) cycle.get("cycle_duration");

                                            Calendar startCalendar = parseDate(startDateStr);
                                            Calendar endCalendar = parseDate(endDateStr);

                                            // Highlight start to end dates in green
                                            while (!startCalendar.after(endCalendar)) {
                                                cycleDates.add(CalendarDay.from(startCalendar));
                                                startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                                            }

                                            // Highlight cycle duration in grey
                                            // Calendar cycleDurationStart = (Calendar) startCalendar.clone();
                                            // Calendar cycleDurationEnd = (Calendar) startCalendar.clone();
                                            // cycleDurationEnd.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycleDurationStr) - 1);
                                            // while (!cycleDurationStart.after(cycleDurationEnd)) {
                                            // cycleDurationDates.add(CalendarDay.from(cycleDurationStart));
                                            // cycleDurationStart.add(Calendar.DAY_OF_MONTH, 1);
                                            // }

                                            // Highlight cycle end dates in bloody red
                                            cycleEndDates.add(CalendarDay.from(parseDate(endDateStr)));
                                        }
                                        // Add decorators for cycles
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleColor, cycleDates, ""));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleDurationColor, cycleDurationDates, ""));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleEndDateColor, cycleEndDates, ""));
                                    }

                                    // Process predictions
                                    List<Map<String, Object>> predictions = (List<Map<String, Object>>) document.get("predictions");
                                    if (predictions != null) {
                                        for (Map<String, Object> prediction : predictions) {
                                            // Handle cycle start and end dates
                                            addDateToSet(prediction.get("cycle_start_date"), predictionCycleDates);
                                            addDateToSet(prediction.get("cycle_end_date"), predictionCycleEndDates);

                                            // Handle fertile window
                                            String fertileStartStr = (String) prediction.get("fertile_window_start");
                                            String fertileEndStr = (String) prediction.get("fertile_window_end");
                                            Calendar fertileStart = parseDate(fertileStartStr);
                                            Calendar fertileEnd = parseDate(fertileEndStr);

                                            // Highlight fertile window
                                            while (!fertileStart.after(fertileEnd)) {
                                                fertileWindowDates.add(CalendarDay.from(fertileStart));
                                                fertileStart.add(Calendar.DAY_OF_MONTH, 1);
                                            }

                                            // Handle ovulation date
                                            addDateToSet(prediction.get("ovulation_date"), ovulationDates);

                                            // Handle strong flow dates
                                            addDateToSet(prediction.get("strong_flow_start"), strongFlowDates);
                                            addDateToSet(prediction.get("strong_flow_end"), strongFlowDates);

                                            // Handle next cycle start date
                                            addDateToSet(prediction.get("next_cycle_start_date"), nextCycleStartDates);
                                        }
                                        // Add decorators for predictions
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.predictionCycleStartEndColor, predictionCycleDates, ""));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.fertileWindowColor, fertileWindowDates, ""));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.ovulationColor, ovulationDates, ""));

                                        // Assuming strongFlowDates is a Collection<CalendarDay> that you have populated
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.strongFlowColor, strongFlowDates, "Strong"));


                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.nextCycleStartColor, nextCycleStartDates, ""));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.predictionCycleEndDateColor, predictionCycleEndDates, ""));
                                    }

                                    // Finally, highlight today's date in blue (this decorator is added last)
                                    highlightToday();
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
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.feedback) {
            Intent intent = new Intent(CalendarActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.privacy) {
            Intent intent = new Intent(CalendarActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.disclaimer_content) {
            Intent intent = new Intent(CalendarActivity.this, DisclaimerContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.terms_condition) {
            Intent intent = new Intent(CalendarActivity.this, TermsAndConditionContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Calendar parseDate(String dateStr) {
        // Parse the date string to Calendar object
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private void addDateToSet(Object dateObj, Set<CalendarDay> dateSet) {
        if (dateObj != null) {
            String dateStr = (String) dateObj;
            Calendar calendar = parseDate(dateStr);
            dateSet.add(CalendarDay.from(calendar));
        }
    }

    private void highlightToday() {
        Calendar today = Calendar.getInstance(); // Get today's date
        Set<CalendarDay> todaySet = new HashSet<>();
        todaySet.add(CalendarDay.from(today)); // Add today's date to the set
        // Add a decorator to highlight today's date in blue
        calendarView.addDecorator(new EventDecorator(this, R.color.todayBlueColor, todaySet, null)); // No text for today
    }
}
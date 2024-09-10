package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

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

        // Initialize the Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        calendarView = findViewById(R.id.calendarView);
        editPeriodButton = findViewById(R.id.editPeriodButton);

        // Set up click listener for the Edit Period button
        editPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click, e.g., open EditPeriodActivity
                Intent intent = new Intent(CalendarActivity.this, EditPeriodActivity.class);
                startActivity(intent);
            }
        });

        // bading si albert
        fetchCycleData();
    }

    private void fetchCycleData() {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the UID of the logged-in user

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
                                            Calendar cycleDurationStart = (Calendar) startCalendar.clone();
                                            Calendar cycleDurationEnd = (Calendar) startCalendar.clone();
                                            cycleDurationEnd.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycleDurationStr) - 1);
                                            while (!cycleDurationStart.after(cycleDurationEnd)) {
                                                cycleDurationDates.add(CalendarDay.from(cycleDurationStart));
                                                cycleDurationStart.add(Calendar.DAY_OF_MONTH, 1);
                                            }

                                            // Highlight cycle end dates in bloody red
                                            cycleEndDates.add(CalendarDay.from(parseDate(endDateStr)));
                                        }
                                        // Add decorators for cycles
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleColor, cycleDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleDurationColor, cycleDurationDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.cycleEndDateColor, cycleEndDates));
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
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.predictionCycleStartEndColor, predictionCycleDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.fertileWindowColor, fertileWindowDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.ovulationColor, ovulationDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.strongFlowColor, strongFlowDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.nextCycleStartColor, nextCycleStartDates));
                                        calendarView.addDecorator(new EventDecorator(CalendarActivity.this, R.color.predictionCycleEndDateColor, predictionCycleEndDates));
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

    // Function to highlight today's date in blue (always add this last to prioritize today)
    private void highlightToday() {
        Calendar today = Calendar.getInstance(); // Get today's date
        Set<CalendarDay> todaySet = new HashSet<>();
        todaySet.add(CalendarDay.from(today)); // Add today's date to the set

        // Add a decorator to highlight today's date in blue
        calendarView.addDecorator(new EventDecorator(this, R.color.todayBlueColor, todaySet));
    }

    private void addDateToSet(Object dateObj, Set<CalendarDay> dateSet) {
        if (dateObj != null) {
            String dateStr = (String) dateObj;
            Calendar calendar = parseDate(dateStr);
            dateSet.add(CalendarDay.from(calendar));
        }
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
}

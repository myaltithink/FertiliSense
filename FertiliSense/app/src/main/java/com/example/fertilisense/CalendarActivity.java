package com.example.fertilisense;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private Button editPeriodButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String userId;

    private List<CalendarDay> newPeriodDays = new ArrayList<>();
    private List<CalendarDay> fullCycleDays = new ArrayList<>();
    private List<CalendarDay> strongDays = new ArrayList<>(); // Define strongDays list

    private static final int NEW_PERIOD_COLOR = Color.parseColor("#8B4513"); // Brown for new periods
    private static final int CYCLE_COLOR = Color.parseColor("#AAB396"); // AAB396 for the full cycle
    private static final int PREDICTED_PERIOD_COLOR = Color.parseColor("#00712D"); // Green for predicted periods
    private static final int STRONG_DAY_COLOR = Color.parseColor("#C7253E"); // Red for strong days
    private static final int END_DATE_COLOR = Color.parseColor("#D32F2F"); // Orange for end date
    private static final int PREVIOUS_CYCLE_COLOR = Color.parseColor("#D3D3D3"); // Light gray for previous cycle
    private static final int PREVIOUS_START_END_COLOR = Color.parseColor("#C7253E"); // Brown for previous start and end dates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        editPeriodButton = findViewById(R.id.editPeriodButton);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("MenstrualCycles");

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            loadMenstrualCycleData();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editPeriodButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, EditPeriodActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenstrualCycleData(); // Reload data when returning to this activity
    }

    private void loadMenstrualCycleData() {
        DatabaseReference userCyclesRef = databaseReference.child(userId);
        userCyclesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String startDateString = snapshot.child("startDate").getValue(String.class);
                String endDateString = snapshot.child("endDate").getValue(String.class);
                String cycleDurationString = snapshot.child("cycleDuration").getValue(String.class);
                String previousStartDateString = snapshot.child("previousStartDate").getValue(String.class);
                String previousEndDateString = snapshot.child("previousEndDate").getValue(String.class);
                String previousCycleDurationString = snapshot.child("previousCycleDuration").getValue(String.class);

                if (startDateString != null && endDateString != null && cycleDurationString != null) {
                    Log.d("CalendarActivity", "Start Date: " + startDateString + " End Date: " + endDateString + " Cycle Duration: " + cycleDurationString);
                    try {
                        int cycleDuration = Integer.parseInt(cycleDurationString);

                        // Fetch previous cycle details
                        Date previousStartDate = previousStartDateString != null ? parseDate(previousStartDateString) : null;
                        Date previousEndDate = previousEndDateString != null ? parseDate(previousEndDateString) : null;
                        int previousCycleDuration = previousCycleDurationString != null ? Integer.parseInt(previousCycleDurationString) : 0;

                        List<CalendarDay> calculatedNewPeriodDays = getPeriodDays(startDateString, endDateString);
                        List<CalendarDay> calculatedFullCycleDays = getFullCycleDays(startDateString, cycleDuration);

                        // Clear existing decorators
                        calendarView.removeDecorators();

                        // Highlight the new menstrual cycle dates
                        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, NEW_PERIOD_COLOR, calculatedNewPeriodDays));

                        // Highlight the full cycle
                        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, CYCLE_COLOR, calculatedFullCycleDays));

                        // Highlight the end date
                        CalendarDay endDate = CalendarDay.from(parseDate(endDateString));
                        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, END_DATE_COLOR, Collections.singleton(endDate)));

                        // Highlight today
                        Calendar today = Calendar.getInstance();
                        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, Color.parseColor("#BE29EC"), Collections.singleton(CalendarDay.from(today))));

// Highlight previous cycle details
                        if (previousStartDate != null && previousEndDate != null) {
                            // Get the range of days from the previous start date to the previous end date
                            List<CalendarDay> previousCycleDays = getFullCycleDays(new SimpleDateFormat("dd/MM/yyyy").format(previousStartDate), previousCycleDuration);

                            // Define a list to hold the days that will be highlighted in green
                            List<CalendarDay> greenDays = new ArrayList<>();
                            CalendarDay previousEndDateDay = CalendarDay.from(previousEndDate);
                            CalendarDay previousStartDay = CalendarDay.from(previousStartDate);

                            // Add days from previous start date to the day before the previous end date
                            for (CalendarDay day : previousCycleDays) {
                                if (day.isBefore(previousEndDateDay)) {
                                    greenDays.add(day);
                                }
                            }

                            // Add the previous cycle range in light gray
                            calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, PREVIOUS_CYCLE_COLOR, previousCycleDays));

                            // Highlight the range from previous start date to the day before previous end date in green
                            calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, PREDICTED_PERIOD_COLOR, greenDays));

                            // Highlight the previous end date in red
                            calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, END_DATE_COLOR, Collections.singleton(previousEndDateDay)));
                        }




                        // Highlight the first few days (2 to 4 in your example) as green
                        List<CalendarDay> greenDays = new ArrayList<>();
                        for (int i = 0; i < calculatedNewPeriodDays.size() - 1; i++) {
                            greenDays.add(calculatedNewPeriodDays.get(i));
                        }
                        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, PREDICTED_PERIOD_COLOR, greenDays));



                        // Predict and highlight strong days
                        strongDays = predictStrongDays(calculatedNewPeriodDays, cycleDuration);
                        calendarView.addDecorator(new EventDecorator(
                                CalendarActivity.this,
                                PREDICTED_PERIOD_COLOR,
                                "Strong",
                                Color.BLACK,
                                strongDays
                        ));

                        newPeriodDays.clear();
                        newPeriodDays.addAll(calculatedNewPeriodDays);

                        fullCycleDays.clear();
                        fullCycleDays.addAll(calculatedFullCycleDays);

                        // Predict and highlight next period start dates
                        predictNextPeriodStartDate(calculatedNewPeriodDays, cycleDuration);

                        calendarView.invalidateDecorators();
                    } catch (NumberFormatException e) {
                        Toast.makeText(CalendarActivity.this, "Invalid cycle duration format", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Toast.makeText(CalendarActivity.this, "Date parsing error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CalendarActivity.this, "No menstrual cycle data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Date parseDate(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.parse(dateString);
    }

    private List<CalendarDay> getPeriodDays(String startDateString, String endDateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = sdf.parse(startDateString);
        Date endDate = sdf.parse(endDateString);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        List<CalendarDay> periodDays = new ArrayList<>();
        Calendar dateIterator = Calendar.getInstance();
        dateIterator.setTime(startDate);

        while (!dateIterator.after(endCalendar)) {
            periodDays.add(CalendarDay.from(dateIterator));
            dateIterator.add(Calendar.DATE, 1);
        }
        return periodDays;
    }

    private List<CalendarDay> getFullCycleDays(String startDateString, int cycleDuration) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = sdf.parse(startDateString);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        List<CalendarDay> cycleDays = new ArrayList<>();
        Calendar dateIterator = Calendar.getInstance();
        dateIterator.setTime(startDate);

        for (int i = 0; i < cycleDuration; i++) {
            cycleDays.add(CalendarDay.from(dateIterator));
            dateIterator.add(Calendar.DATE, 1);
        }
        return cycleDays;
    }

    private List<CalendarDay> predictStrongDays(List<CalendarDay> lastPeriodDays, int cycleDuration) {
        List<CalendarDay> strongDays = new ArrayList<>();
        if (lastPeriodDays.isEmpty()) return strongDays;

        CalendarDay lastStartDate = lastPeriodDays.get(0);

        // Predict strong days based on the last start date and cycle duration
        Calendar strongDayStart = Calendar.getInstance();
        strongDayStart.set(lastStartDate.getYear(), lastStartDate.getMonth(), lastStartDate.getDay());

        for (int i = 0; i < cycleDuration; i++) {
            CalendarDay predictedStrongDay = CalendarDay.from(strongDayStart);
            strongDays.add(predictedStrongDay);
            if (strongDays.size() == 2) break; // Assuming strong days are the first 2 days
            strongDayStart.add(Calendar.DATE, 1);
        }

        return strongDays;
    }

    private void predictNextPeriodStartDate(List<CalendarDay> lastPeriodDays, int cycleDuration) {
        if (lastPeriodDays.isEmpty()) return;

        CalendarDay lastStartDate = lastPeriodDays.get(0);

        // Set the start date for predictions
        Calendar nextPeriodStart = Calendar.getInstance();
        nextPeriodStart.set(lastStartDate.getYear(), lastStartDate.getMonth(), lastStartDate.getDay());

        // Move to the next cycle start date
        nextPeriodStart.add(Calendar.DATE, cycleDuration);

        // Create a single predicted start date
        CalendarDay predictedStartDate = CalendarDay.from(nextPeriodStart);

        // Create a list to hold both the last start date and the predicted start date
        List<CalendarDay> predictedDates = new ArrayList<>();
        predictedDates.add(lastStartDate); // Add the last start date (which will also be green)
        predictedDates.add(predictedStartDate); // Add the predicted start date

        // Highlight the next period start date and the last start date in green
        calendarView.addDecorator(new BackgroundColorDecorator(CalendarActivity.this, PREDICTED_PERIOD_COLOR, predictedDates));

        // Save the predicted start date to Firebase
        saveNextPredictedDateToFirebase(predictedStartDate);
    }


    private void saveNextPredictedDateToFirebase(CalendarDay predictedDate) {
        DatabaseReference predictedDateRef = databaseReference.child(userId).child("PreviousPredictedDate");

        // Convert CalendarDay object to a format suitable for Firebase
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(predictedDate.getYear() - 1900, predictedDate.getMonth(), predictedDate.getDay());
        String predictedDateString = sdf.format(date);

        predictedDateRef.setValue(predictedDateString).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CalendarActivity.this, "Next predicted date saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CalendarActivity.this, "Failed to save next predicted date", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

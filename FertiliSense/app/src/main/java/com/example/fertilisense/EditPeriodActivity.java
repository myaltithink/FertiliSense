package com.example.fertilisense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent; // Add this import
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Collections;
import java.util.HashMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditPeriodActivity extends AppCompatActivity {

    private EditText startDateField, endDateField, periodDurationField, cycleDurationField;
    private Button saveButton;
    private TextView messageTextView;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_period);

        startDateField = findViewById(R.id.start_date_field);
        endDateField = findViewById(R.id.end_date_field);
        periodDurationField = findViewById(R.id.period_duration_field);
        cycleDurationField = findViewById(R.id.cycle_duration_field);
        saveButton = findViewById(R.id.save_button);
        messageTextView = findViewById(R.id.message_text_view);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch existing cycle data
        fetchCycleData();

        startDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(startDateField);
            }
        });

        endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(endDateField);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePeriodData();
            }
        });
    }

    private void showDatePicker(final EditText dateField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        dateField.setText(dateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void fetchCycleData() {
        // Replace "userId" with the actual user ID from your Firebase Authentication
        String userId = "2xET5LlzYvNi7WooDcC1mpujyJ73";

        db.collection("menstrual_cycles")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve the first cycle from the cycles array
                                if (document.contains("cycles")) {
                                    // Assuming the first cycle is to be fetched
                                    List<Map<String, Object>> cycles = (List<Map<String, Object>>) document.get("cycles");
                                    if (cycles != null && !cycles.isEmpty()) {
                                        Map<String, Object> cycle = cycles.get(0);

                                        String startDate = (String) cycle.get("start_date");
                                        String endDate = (String) cycle.get("end_date");
                                        String periodDuration = (String) cycle.get("period_duration");
                                        String cycleDuration = (String) cycle.get("cycle_duration");

                                        startDateField.setText(startDate);
                                        endDateField.setText(endDate);
                                        periodDurationField.setText(periodDuration);
                                        cycleDurationField.setText(cycleDuration);
                                    }
                                }
                            } else {
                                messageTextView.setText("No such document");
                            }
                        } else {
                            messageTextView.setText("Error getting document: " + task.getException());
                        }
                    }
                });
    }

    private void savePeriodData() {
        String startDate = startDateField.getText().toString();
        String endDate = endDateField.getText().toString();
        String periodDuration = periodDurationField.getText().toString();
        String cycleDuration = cycleDurationField.getText().toString();

        if (startDate.isEmpty() || endDate.isEmpty() || periodDuration.isEmpty() || cycleDuration.isEmpty()) {
            messageTextView.setText("Please fill in all fields.");
            return;
        }

        // Save cycle data
        String userId = "2xET5LlzYvNi7WooDcC1mpujyJ73"; // Replace with actual user ID

        Map<String, Object> cycleData = new HashMap<>();
        cycleData.put("start_date", startDate);
        cycleData.put("end_date", endDate);
        cycleData.put("period_duration", periodDuration);
        cycleData.put("cycle_duration", cycleDuration);

        db.collection("menstrual_cycles")
                .document(userId)
                .update("cycles", Collections.singletonList(cycleData))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Calculate and update predictions
                            updatePredictions(userId, startDate, endDate, cycleDuration);
                        } else {
                            messageTextView.setText("Error saving data: " + task.getException());
                        }
                    }
                });
    }

    private void updatePredictions(String userId, String startDate, String endDate, String cycleDuration) {
        // Convert date strings to Calendar objects
        Calendar startCalendar = parseDate(startDate);
        Calendar endCalendar = parseDate(endDate);
        int cycleLength = Integer.parseInt(cycleDuration);

        // Clear existing predictions
        db.collection("menstrual_cycles")
                .document(userId)
                .update("predictions", new ArrayList<>())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // List to hold predictions
                            List<Map<String, Object>> predictions = new ArrayList<>();
                            Calendar predictionStart = (Calendar) startCalendar.clone();
                            Calendar predictionEnd = (Calendar) endCalendar.clone();

                            for (int i = 0; i < 4; i++) { // Generate predictions for the next 4 cycles
                                // Calculate start and end dates of the cycle
                                Calendar cycleStart = (Calendar) predictionStart.clone();
                                Calendar cycleEnd = (Calendar) cycleStart.clone();
                                cycleEnd.add(Calendar.DAY_OF_MONTH, cycleLength - 1);

                                // Calculate fertile window
                                Calendar fertileWindowStart = (Calendar) cycleStart.clone();
                                fertileWindowStart.add(Calendar.DAY_OF_MONTH, 12); // Example, adjust as needed
                                Calendar fertileWindowEnd = (Calendar) fertileWindowStart.clone();
                                fertileWindowEnd.add(Calendar.DAY_OF_MONTH, 5); // Example, adjust as needed

                                // Calculate ovulation date
                                Calendar ovulationDate = (Calendar) cycleStart.clone();
                                ovulationDate.add(Calendar.DAY_OF_MONTH, 14); // Example, adjust as needed

                                // Calculate next cycle start date
                                Calendar nextCycleStart = (Calendar) cycleEnd.clone();
                                nextCycleStart.add(Calendar.DAY_OF_MONTH, 1);

                                // Create a map for this prediction
                                Map<String, Object> prediction = new HashMap<>();
                                prediction.put("cycle_start_date", dateFormat.format(cycleStart.getTime()));
                                prediction.put("cycle_end_date", dateFormat.format(cycleEnd.getTime()));
                                prediction.put("fertile_window_start", dateFormat.format(fertileWindowStart.getTime()));
                                prediction.put("fertile_window_end", dateFormat.format(fertileWindowEnd.getTime()));
                                prediction.put("ovulation_date", dateFormat.format(ovulationDate.getTime()));
                                prediction.put("next_cycle_start_date", dateFormat.format(nextCycleStart.getTime()));
                                prediction.put("strong_flow_start", dateFormat.format(cycleStart.getTime())); // Example, adjust as needed
                                prediction.put("strong_flow_end", dateFormat.format(cycleEnd.getTime())); // Example, adjust as needed

                                // Add this prediction to the list
                                predictions.add(prediction);

                                // Move to the next cycle
                                predictionStart.add(Calendar.DAY_OF_MONTH, cycleLength);
                                predictionEnd.add(Calendar.DAY_OF_MONTH, cycleLength);
                            }

                            // Save predictions to Firestore
                            db.collection("menstrual_cycles")
                                    .document(userId)
                                    .update("predictions", predictions)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                messageTextView.setText("Data saved and predictions updated successfully!");
                                                Intent intent = new Intent(EditPeriodActivity.this, CalendarActivity.class);
                                                startActivity(intent);
                                            } else {
                                                messageTextView.setText("Error updating predictions: " + task.getException());
                                            }
                                        }
                                    });
                        } else {
                            messageTextView.setText("Error clearing existing predictions: " + task.getException());
                        }
                    }
                });
    }




    private Calendar parseDate(String dateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(dateString));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar;
    }


}

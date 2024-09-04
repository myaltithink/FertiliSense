package com.example.fertilisense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditPeriodActivity extends AppCompatActivity {

    private EditText startDateField, endDateField, periodDurationField, cycleDurationField;
    private Button saveButton;
    private TextView messageTextView;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_period); // Updated layout file name

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("MenstrualCycles");

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return; // Ensure code execution stops after finishing the activity
        }

        startDateField = findViewById(R.id.start_date_field);
        endDateField = findViewById(R.id.end_date_field);
        periodDurationField = findViewById(R.id.period_duration_field);
        cycleDurationField = findViewById(R.id.cycle_duration_field);
        saveButton = findViewById(R.id.save_button);
        messageTextView = findViewById(R.id.message_text_view);

        startDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateField);
            }
        });

        endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateField);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMenstrualCycle();
            }
        });
    }

    private void showDatePickerDialog(final EditText dateField) {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog picker = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateField.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
        picker.show();
    }

    private void saveMenstrualCycle() {
        String startDate = startDateField.getText().toString();
        String endDate = endDateField.getText().toString();
        String periodDuration = periodDurationField.getText().toString();
        String cycleDuration = cycleDurationField.getText().toString();

        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) ||
                TextUtils.isEmpty(periodDuration) || TextUtils.isEmpty(cycleDuration)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userCyclesRef = databaseReference.child(userId);

        // Retrieve existing data to save as previous data
        userCyclesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String prevStartDate = snapshot.child("startDate").getValue(String.class);
                String prevEndDate = snapshot.child("endDate").getValue(String.class);
                String prevPeriodDuration = snapshot.child("periodDuration").getValue(String.class);
                String prevCycleDuration = snapshot.child("cycleDuration").getValue(String.class);

                // Save the current data to previous fields
                userCyclesRef.child("previousStartDate").setValue(prevStartDate);
                userCyclesRef.child("previousEndDate").setValue(prevEndDate);
                userCyclesRef.child("previousPeriodDuration").setValue(prevPeriodDuration);
                userCyclesRef.child("previousCycleDuration").setValue(prevCycleDuration);

                // Save the new data to current fields
                userCyclesRef.child("startDate").setValue(startDate);
                userCyclesRef.child("endDate").setValue(endDate);
                userCyclesRef.child("periodDuration").setValue(periodDuration);
                userCyclesRef.child("cycleDuration").setValue(cycleDuration).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPeriodActivity.this, "Menstrual cycle saved successfully", Toast.LENGTH_SHORT).show();
                            Log.d("EditPeriodActivity", "Menstrual cycle saved successfully");
                            finish(); // Close this activity and return to the previous one (Calendar Activity)
                        } else {
                            Log.e("EditPeriodActivity", "Failed to save menstrual cycle", task.getException());
                            Toast.makeText(EditPeriodActivity.this, "Failed to save menstrual cycle", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditPeriodActivity", "Failed to retrieve data", error.toException());
                Toast.makeText(EditPeriodActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

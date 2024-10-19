package com.fertilisense.fertilisense;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateUserProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateUsername, editTextUpdateAge,
            editTextUpdateDOB, editTextUpdatePhone;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textUsername, textAge, textDOB, textPhone, textGender;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_profile);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Upload Profile Picture");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); //custom back button drawable
        }

        // Set custom overflow icon
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        editTextUpdateUsername = findViewById(R.id.username_field_update);
        editTextUpdateAge = findViewById(R.id.age_field_update);
        editTextUpdateDOB = findViewById(R.id.date_field_update);
        editTextUpdatePhone = findViewById(R.id.phone_field_update);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //Fetch data from database
        showProfile(firebaseUser);

        editTextUpdateDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textSADOB[] = textDOB.split("/");

                int day = Integer.parseInt(textSADOB[0]);
                int month = Integer.parseInt(textSADOB[1]) - 1;
                int year = Integer.parseInt(textSADOB[2]);

                DatePickerDialog picker;

                picker = new DatePickerDialog(UpdateUserProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        //Update profile button
        Button buttonUpdateProfile = findViewById(R.id.button_update_user_details);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        // Validate mobile number using matcher and pattern
        String mobileRegex = "[0-9][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textPhone);

        if (TextUtils.isEmpty(textUsername)) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
            editTextUpdateUsername.setError("Username is required");
            editTextUpdateUsername.requestFocus();
        } else if (TextUtils.isEmpty(textAge)) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please enter your age", Toast.LENGTH_SHORT).show();
            editTextUpdateAge.setError("Age is required");
            editTextUpdateAge.requestFocus();
        } else if (TextUtils.isEmpty(textDOB)) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            editTextUpdateDOB.setError("Date of Birth is required");
            editTextUpdateDOB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textPhone)) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            editTextUpdatePhone.setError("Phone Number is required");
            editTextUpdatePhone.requestFocus();
        } else if (textPhone.length() != 11) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_SHORT).show();
            editTextUpdatePhone.setError("Phone Number should be 11 digits");
            editTextUpdatePhone.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateUserProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_SHORT).show();
            editTextUpdatePhone.setError("Phone Number is not valid");
            editTextUpdatePhone.requestFocus();
        } else {
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textUsername = editTextUpdateUsername.getText().toString();
            textDOB = editTextUpdateDOB.getText().toString();
            textAge = editTextUpdateAge.getText().toString();
            textPhone = editTextUpdatePhone.getText().toString();

            //Enter user data into firebase
            ReadwriteUserDetails writeUserDetails = new ReadwriteUserDetails(textUsername, textDOB, textGender, textPhone, textAge);

            //Extract User data from database
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Settings new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(textUsername).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateUserProfileActivity.this, "User details successfully updated", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UpdateUserProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(UpdateUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        //Extracting data from Registered Users
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ReadwriteUserDetails readwriteUserDetails = dataSnapshot.getValue(ReadwriteUserDetails.class);
                if(readwriteUserDetails != null){
                    textUsername =  readwriteUserDetails.username;
                    textAge = readwriteUserDetails.age;
                    textDOB = readwriteUserDetails.date;
                    textPhone = readwriteUserDetails.phone;
                    textGender = readwriteUserDetails.gender;

                    editTextUpdateUsername.setText(textUsername);
                    editTextUpdateAge.setText(textAge);
                    editTextUpdatePhone.setText(textPhone);
                    editTextUpdateDOB.setText(textDOB);

                    //Show Gender through radio button
                    if(textGender.equals("Male")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateUserProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateUserProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return true;
    }

    // Handle action bar item clicks here.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle different menu item clicks based on their IDs using if-else
        if(id == android.R.id.home){
            //Back Button
            NavUtils.navigateUpFromSameTask(UpdateUserProfileActivity.this);
        } else if (id == R.id.common_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.common_menu_update_profile) {
            // Handle update profile action
            Log.d(TAG, "onOptionsItemSelected: Update Profile selected.");
            Intent intent = new Intent(this, UpdateUserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_change_password) {
            // Handle change password action
            Log.d(TAG, "onOptionsItemSelected: Change Password selected.");
            Intent intent = new Intent(UpdateUserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_delete_account) {
            // Handle delete account action
            Log.d(TAG, "onOptionsItemSelected: Delete Account selected.");
            Intent intent = new Intent(this, DeleteProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        }

        // If none of the above conditions are met, delegate to superclass for handling
        return super.onOptionsItemSelected(item);
    }
}
package com.fertilisense.fertilisense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameFieldRegister, emailFieldRegister, ageFieldRegister, dateFieldRegister, phoneFieldRegister, passwordFieldRegister, confirmPasswordFieldRegister;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private static final String TAG = "RegisterActivity";
    private DatePickerDialog picker;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Register");
        }
        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_SHORT).show();

        usernameFieldRegister = findViewById(R.id.username_field_register);
        emailFieldRegister = findViewById(R.id.email_field_register);
        ageFieldRegister = findViewById(R.id.age_field_register);
        dateFieldRegister = findViewById(R.id.date_field_register);
        phoneFieldRegister = findViewById(R.id.phone_field_register);
        passwordFieldRegister = findViewById(R.id.password_field_register);
        confirmPasswordFieldRegister = findViewById(R.id.confirm_password_field_register);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        dateFieldRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateFieldRegister.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonRegister = findViewById(R.id.register_button);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                String textUsername = usernameFieldRegister.getText().toString();
                String textEmail = emailFieldRegister.getText().toString();
                String textAge = ageFieldRegister.getText().toString();
                String textDate = dateFieldRegister.getText().toString();
                String textPhone = phoneFieldRegister.getText().toString();
                String textPassword = passwordFieldRegister.getText().toString();
                String textConfirmPassword = confirmPasswordFieldRegister.getText().toString();
                String textGender;

                // Validate mobile number using matcher and pattern
                String mobileRegex = "[0-9][0-9]{9}";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textPhone);

                if (TextUtils.isEmpty(textUsername)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                    usernameFieldRegister.setError("Username is required");
                    usernameFieldRegister.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    emailFieldRegister.setError("Email is required");
                    emailFieldRegister.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your email", Toast.LENGTH_SHORT).show();
                    emailFieldRegister.setError("Valid Email is required");
                    emailFieldRegister.requestFocus();
                } else if (TextUtils.isEmpty(textAge)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your age", Toast.LENGTH_SHORT).show();
                    ageFieldRegister.setError("Age is required");
                    ageFieldRegister.requestFocus();
                } else if (TextUtils.isEmpty(textDate)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
                    dateFieldRegister.setError("Date of Birth is required");
                    dateFieldRegister.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textPhone)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    phoneFieldRegister.setError("Phone Number is required");
                    phoneFieldRegister.requestFocus();
                } else if (textPhone.length() != 11) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your phone number", Toast.LENGTH_SHORT).show();
                    phoneFieldRegister.setError("Phone Number should be 11 digits");
                    phoneFieldRegister.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your phone number", Toast.LENGTH_SHORT).show();
                    phoneFieldRegister.setError("Phone Number is not valid");
                    phoneFieldRegister.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    passwordFieldRegister.setError("Password is required");
                    passwordFieldRegister.requestFocus();
                } else if (textPassword.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 digits", Toast.LENGTH_SHORT).show();
                    passwordFieldRegister.setError("Password too weak");
                    passwordFieldRegister.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                    confirmPasswordFieldRegister.setError("Password Confirmation is required");
                    confirmPasswordFieldRegister.requestFocus();
                } else if (!textPassword.equals(textConfirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Please match your password with confirm password", Toast.LENGTH_SHORT).show();
                    confirmPasswordFieldRegister.setError("Password and Confirm Password doesn't match");
                    confirmPasswordFieldRegister.requestFocus();

                    passwordFieldRegister.clearComposingText();
                    confirmPasswordFieldRegister.clearComposingText();
                } else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    registerUser(textUsername, textEmail, textAge, textDate, textPhone, textPassword, textGender);
                }
            }
        });

        TextView loginLink = findViewById(R.id.login_link);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("fromRegister", true);
                overridePendingTransition(0,0);
                startActivity(intent);
            }
        });
    }

    private void registerUser(String textUsername, String textEmail, String textAge, String textDate, String textPhone, String textPassword, String textGender) {
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            // Enter user data into the firebase realtime database
                            ReadwriteUserDetails writeUserDetails = new ReadwriteUserDetails(textUsername, textDate, textGender, textPhone, textAge);

                            // Extracting user reference from Database for Registered Users
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseUser.sendEmailVerification();
                                        Toast.makeText(RegisterActivity.this, "Account registered successfully. Please verify your email", Toast.LENGTH_SHORT).show();

                                        // Sign out the user
                                        auth.signOut();

                                        // Open Login activity after successful registration
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
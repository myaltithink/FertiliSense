package com.fertilisense.fertilisense;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailFieldLogin, passwordFieldLogin;
    private FirebaseAuth authProfile;
    private DatabaseReference userDatabaseReference;
    private static final String TAG = "LoginActivity";
    private boolean isNavigatingFromRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Login");
        }

        emailFieldLogin = findViewById(R.id.email_field_login);
        passwordFieldLogin = findViewById(R.id.password_field_login);
        authProfile = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        // Show hide password
        ImageView imageViewShowHidePassword = findViewById(R.id.imageView_show_hide_password);
        imageViewShowHidePassword.setImageResource(R.drawable.ic_hide);
        imageViewShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordFieldLogin.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    passwordFieldLogin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHidePassword.setImageResource(R.drawable.ic_hide);
                } else {
                    passwordFieldLogin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePassword.setImageResource(R.drawable.ic_show);
                }
            }
        });

        Button buttonLogin = findViewById(R.id.login_button);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = emailFieldLogin.getText().toString();
                String textPassword = passwordFieldLogin.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    emailFieldLogin.setError("Email is required");
                    emailFieldLogin.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re-enter your email", Toast.LENGTH_SHORT).show();
                    emailFieldLogin.setError("Valid Email is required");
                    emailFieldLogin.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    passwordFieldLogin.setError("Password required");
                    passwordFieldLogin.requestFocus();
                } else {
                    loginUser(textEmail, textPassword);
                }
            }
        });

        // Set OnClickListener for register link
        TextView registerLink = findViewById(R.id.register_link);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, TermsAndCondition.class));
            }
        });

        // Check if this activity was started from RegisterActivity
        if (getIntent().getBooleanExtra("fromRegister", false)) {
            isNavigatingFromRegister = true;
        }

        //For Forgot Password
        TextView forgotPasswordButton = findViewById(R.id.forgot_password_button);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can reset your password now", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    // Check if email is verified
                    if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "You have successfully logged in", Toast.LENGTH_SHORT).show();
                        fetchUserGenderAndNavigate(firebaseUser.getUid());
                    } else {
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                        }
                        authProfile.signOut();
                        showAlertDialog();
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        // Error for invalid credentials (could be either email or password)
                        if (e.getMessage().contains("The password is invalid")) {
                            passwordFieldLogin.setError("Invalid password. Try again.");
                            passwordFieldLogin.requestFocus();
                        } else if (e.getMessage().contains("There is no user record corresponding to this identifier")) {
                            emailFieldLogin.setError("No account found with this email.");
                            emailFieldLogin.requestFocus();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(LoginActivity.this, "Login failed. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchUserGenderAndNavigate(String uid) {
        userDatabaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String gender = snapshot.child("gender").getValue(String.class);
                    if (gender != null) {
                        if (gender.equalsIgnoreCase("female")) {
                            navigateToActivity(FertiliSenseDashboardActivity.class);
                        } else if (gender.equalsIgnoreCase("male")) {
                            navigateToActivity(MaleDashboardActivity.class);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Gender information is missing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User data is missing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(LoginActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void showAlertDialog() {
        // Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Verify your email now");

        // Open email app
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Show the AlertDialog
        alertDialog.show();
    }

    // Check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        if (!isNavigatingFromRegister && authProfile.getCurrentUser() != null) {
            // If the user is already logged in, check if their email is verified
            FirebaseUser firebaseUser = authProfile.getCurrentUser();
            if (firebaseUser.isEmailVerified()) {
                Toast.makeText(LoginActivity.this, "You are currently logged in", Toast.LENGTH_SHORT).show();
                fetchUserGenderAndNavigate(firebaseUser.getUid());
            } else {
                // If the email is not verified, prompt them to verify their email
                showAlertDialog();
            }
        }
    }
}
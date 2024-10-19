package com.fertilisense.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button buttonForgetPassword;
    private EditText emailFieldForgotPassword;
    private FirebaseAuth authProfile;
    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Forgot Password");
        }

        emailFieldForgotPassword = findViewById(R.id.email_field_forgot_password);
        buttonForgetPassword = findViewById(R.id.forgot_password_button);

        buttonForgetPassword.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
               String email = emailFieldForgotPassword.getText().toString();

               if (TextUtils.isEmpty(email)){
                   Toast.makeText(ForgotPasswordActivity.this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
                   emailFieldForgotPassword.setError("Email is required");
                   emailFieldForgotPassword.requestFocus();
               } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                   Toast.makeText(ForgotPasswordActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                   emailFieldForgotPassword.setError("Valid Email is required");
                   emailFieldForgotPassword.requestFocus();
               } else {
                   resetPassword(email);
               }
           }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your inbox for password reset link", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);

                    //clear stack to prevent user coming back
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        emailFieldForgotPassword.setError("User does not exists or no longer valid");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
package com.example.fertilisense;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;

import java.lang.reflect.Method;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, getEditTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private String userPwdCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Change Password");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); //custom back button drawable
        }

        // Set custom overflow icon
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        editTextPwdNew = findViewById(R.id.new_password);;
        editTextPwdCurr = findViewById(R.id.current_password);
        getEditTextPwdConfirmNew = findViewById(R.id.confirm_password);

        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);

        buttonReAuthenticate = findViewById(R.id.button_change_pwd_current);
        buttonChangePwd = findViewById(R.id.button_change_pwd);

        //Disable editText for New Password and Confirm Password until user authenticate current password
        editTextPwdNew.setEnabled(false);
        getEditTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //Authenticate user before changing the password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr = editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "Enter current password", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Enter current password to authenticate");
                    editTextPwdCurr.requestFocus();
                } else {
                    //Reauthenticate user now

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Disable editText for Current Password. Enable New and Confirm Password
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                getEditTextPwdConfirmNew.setEnabled(true);

                                //Enable new pwd button. Disable curr button
                                buttonChangePwd.setEnabled(true);
                                buttonReAuthenticate.setEnabled(false);

                                //Set TextView to show user if authenticated of verified
                                textViewAuthenticated.setText("You can now change your password");
                                Toast.makeText(ChangePasswordActivity.this, "Change password now",Toast.LENGTH_SHORT).show();

                                //Update color of Change password button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this, R.color.violet));
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = getEditTextPwdConfirmNew.getText().toString(); // Changed from editTextPwdCurr to getEditTextPwdConfirmNew

        if (TextUtils.isEmpty(userPwdNew)) {
            Toast.makeText(ChangePasswordActivity.this, "New password needed", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter your new password");
            editTextPwdNew.requestFocus();
        } else if (TextUtils.isEmpty(userPwdConfirmNew)) {
            Toast.makeText(ChangePasswordActivity.this, "Confirm new password", Toast.LENGTH_SHORT).show();
            getEditTextPwdConfirmNew.setError("Please re-enter your new password"); // Changed from editTextPwdCurr to getEditTextPwdConfirmNew
            getEditTextPwdConfirmNew.requestFocus();
        } else if (!userPwdNew.equals(userPwdConfirmNew)) { // Changed from matches to equals and corrected fields
            Toast.makeText(ChangePasswordActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
            getEditTextPwdConfirmNew.setError("Please re-enter same password"); // Changed from editTextPwdCurr to getEditTextPwdConfirmNew
            getEditTextPwdConfirmNew.requestFocus();
        } else if (userPwdCurr.equals(userPwdNew)) { // Changed from matches to equals
            Toast.makeText(ChangePasswordActivity.this, "The new password cannot be the same as the old password", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password");
            editTextPwdNew.requestFocus();
        } else {
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
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
            NavUtils.navigateUpFromSameTask(ChangePasswordActivity.this);
        } else if (id == R.id.common_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.common_menu_update_profile) {
            // Handle update profile action
            Log.d(TAG, "onOptionsItemSelected: Update Profile selected.");
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateUserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_change_password) {
            // Handle change password action
            Log.d(TAG, "onOptionsItemSelected: Change Password selected.");
            Intent intent = new Intent(this, ChangePasswordActivity.class);
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
package com.fertilisense.fertilisense;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private String userPwd;
    private Button buttonAuthenticate, buttonDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Delete Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); //custom back button drawable
        }

        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        editTextUserPwd = findViewById(R.id.user_password);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_pwd);
        buttonAuthenticate = findViewById(R.id.button_authenticate_user);
        buttonDeleteUser = findViewById(R.id.button_delete_user);

        //Disable delete button unless authenticated
        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this,UserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //Authenticate user before changing the password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this, "Enter current password", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Enter current password to authenticate");
                    editTextUserPwd.requestFocus();
                } else {
                    //Reauthenticate user now

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Disable editText for Password.
                                editTextUserPwd.setEnabled(false);

                                //Enable delete user button.
                                buttonAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                //Set TextView to show user if authenticated of verified
                                textViewAuthenticated.setText("You can now delete your account");
                                Toast.makeText(DeleteProfileActivity.this, "You can delete your profile now",Toast.LENGTH_SHORT).show();

                                //Update color of Change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.violet));
                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        // Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Do you really wish to delete your account? This action is irreversible.");

        // Open email app
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData();
            }
        });

        //Return to user profile activity
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfileActivity.this,DeleteProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Change button color to continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.violet));
            }
        });

        // Show the AlertDialog
        alertDialog.show();
    }

    //Delete all data of user
    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    //Delete all data of user
    private void deleteUserData() {
        //Delete the profile picture of user
        if(firebaseUser.getPhotoUrl() != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "Onsuccess: Profile Picture Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete data of user from realtime database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Onsuccess: User Data Deleted");

                //Finally deleted the user
                deleteUser();
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
            NavUtils.navigateUpFromSameTask(DeleteProfileActivity.this);
        } else if (id == R.id.common_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.common_menu_update_profile) {
            // Handle update profile action
            Log.d(TAG, "onOptionsItemSelected: Update Profile selected.");
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateUserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_change_password) {
            // Handle change password action
            Log.d(TAG, "onOptionsItemSelected: Change Password selected.");
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
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
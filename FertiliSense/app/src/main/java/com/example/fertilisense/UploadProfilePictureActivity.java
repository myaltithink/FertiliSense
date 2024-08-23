package com.example.fertilisense;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadProfilePictureActivity extends AppCompatActivity {

    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;
    private static final String TAG = UploadProfilePictureActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

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

        // Initialize views
        imageViewUploadPic = findViewById(R.id.imageView_profile_dp);

        Button buttonUploadPicChoose = findViewById(R.id.upload_picture_button);
        Button buttonUploadPic = findViewById(R.id.save_profile_button);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        // Set Users current DP (Picasso Library)
        Picasso.with(UploadProfilePictureActivity.this).load(uri).into(imageViewUploadPic);

        // Selecting image for profile picture
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Saving the selected image for profile picture
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfilePicture();
            }
        });

    }

    // Choosing image for profile picture
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }

    // Saving chosen image for profile picture
    private void saveProfilePicture() {
        if (uriImage != null) {
            // Save the image with uid of currently login user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid()
                    + "/displaypic." + getFileExtension(uriImage));

            // Upload image into storage
            fileReference.putFile(uriImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful upload
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Handle successful download URL retrieval
                                    Uri downloadUri = uri;
                                    firebaseUser = authProfile.getCurrentUser();

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri).build();

                                    firebaseUser.updateProfile(profileUpdates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Profile updated successfully
                                                    Log.d(TAG, "Profile Updated");
                                                    Toast.makeText(UploadProfilePictureActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(UploadProfilePictureActivity.this, UserProfileActivity.class);
                                                    startActivity(intent);
                                                    overridePendingTransition(0,0);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle failure to update profile
                                                    Log.e(TAG, "Failed to update profile photo.", e);
                                                    Toast.makeText(UploadProfilePictureActivity.this, "Failed to update profile photo.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful upload
                            Log.e(TAG, "Upload failed.", e);
                            Toast.makeText(UploadProfilePictureActivity.this, "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle case where uriImage is null
            Toast.makeText(this, "Please select an image to upload.", Toast.LENGTH_SHORT).show();
        }
    }

    // Obtain file extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
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
            NavUtils.navigateUpFromSameTask(UploadProfilePictureActivity.this);
        } else if (id == R.id.common_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.common_menu_update_profile) {
            // Handle update profile action
            Log.d(TAG, "onOptionsItemSelected: Update Profile selected.");
            Intent intent = new Intent(UploadProfilePictureActivity.this, UpdateUserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.common_menu_change_password) {
            // Handle change password action
            Log.d(TAG, "onOptionsItemSelected: Change Password selected.");
            Intent intent = new Intent(UploadProfilePictureActivity.this, ChangePasswordActivity.class);
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
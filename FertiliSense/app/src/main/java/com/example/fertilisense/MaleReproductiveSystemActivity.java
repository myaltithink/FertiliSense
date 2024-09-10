package com.example.fertilisense;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MaleReproductiveSystemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth authProfile;
    private BottomNavigationView bottomNavigationView;
    private String appPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_male_reproductive_system);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();

        // Load user information in the navigation header
        loadUserInformation();

        findViewById(R.id.ic_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    // Handle "Genital" action
                    // No action needed as we are already on the genital screen
                    Log.d("FertiliSense", "Navigating to MaleReproductiveSystemActivity");
                } else if (id == R.id.calendar) {
                    Log.d("FertiliSense", "Calendar clicked");
                    // Intent intent = new Intent(MaleReproductiveSystemActivity.this, CalendarActivity.class);
                    // startActivity(intent);
                    // overridePendingTransition(0, 0);
                    // finish();
                } else if (id == R.id.home) {
                    Log.d("FertiliSense", "Home clicked");
                    Intent intent = new Intent(MaleReproductiveSystemActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(MaleReproductiveSystemActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                return true;
            }
        });

        // Initialize the back and next ImageViews
        ImageView backLink = findViewById(R.id.ic_back);
        ImageView nextLink = findViewById(R.id.ic_next);

        // Set the onClickListener for the back button
        backLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Male Reproductive System");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, FemaleReproductiveSystemActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Set the onClickListener for the next button
        nextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Mmale Reproductive System");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, FemaleReproductiveSystemActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Link for Male Reproductive Parts 10

        //Bladder 1
        TextView bladderLink = findViewById(R.id.bladder_link);
        bladderLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Bladder Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, BladderActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Seminal Vesicle 2
        TextView seminalVesicleLink = findViewById(R.id.seminal_vesicle_link);
        seminalVesicleLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Seminal Vesicle Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, SeminalVesicleActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Bulbourethral Gland 3
        TextView bulbourethralGlandLink = findViewById(R.id.bulbourethral_gland_link);
        bulbourethralGlandLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Bulbourethral Gland Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, BulbourethralGlandActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Testicles 4
        TextView testiclesLink = findViewById(R.id.testicles_link);
        testiclesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Testicles Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, TesticlesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Urethral Opening 5
        TextView urethralOpeningLink = findViewById(R.id.urethral_opening_link);
        urethralOpeningLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Urethral Opening Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, UrethralOpeningActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Epididymis 6
        TextView epididymisLink = findViewById(R.id.epididymis_link);
        epididymisLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Epididymis Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, EpididymisActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Prostrate Gland 7
        TextView prostrateGlandLink = findViewById(R.id.prostrate_gland_link);
        prostrateGlandLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Prostrate Gland Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, ProstrateGlandActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Ductus (Vas) Deferens 8
        TextView ductusVasDeferensLink = findViewById(R.id.ductus_vas_deferens_link);
        ductusVasDeferensLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Ductus (Vas) Deferens Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, DuctusVasDeferensActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //penis 9
        TextView penisLink = findViewById(R.id.penis_link);
        penisLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Penis Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, PenisActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Glans 10
        TextView glansLink = findViewById(R.id.glans_link);
        glansLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Glans Activity");
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, GlansActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void loadUserInformation() {
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_drawer_username);
        TextView emailTextView = headerView.findViewById(R.id.nav_drawer_email);
        ImageView profilePictureView = findViewById(R.id.nav_header_profile_picture); // Add this

        FirebaseUser user = authProfile.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            emailTextView.setText(email);

            // Fetch username from the database
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userId = user.getUid();
            Log.d("FertiliSense", "Fetching data for user ID: " + userId);

            referenceProfile.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        if (username != null && !username.isEmpty()) {
                            usernameTextView.setText(username);
                        }

                        // Fetch the profile picture from FirebaseUser
                        Uri photoUri = user.getPhotoUrl();
                        if (photoUri != null) {
                            Picasso.with(MaleReproductiveSystemActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(MaleReproductiveSystemActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MaleReproductiveSystemActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Set a click listener to open UserProfileActivity
            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(MaleReproductiveSystemActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.d("FertiliSense", "No current user logged in");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.share_us) {
            Log.d("FertiliSense", "Share Us clicked");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Download this app: https://play.google.com/store/apps/details?id=" + appPackageName);
            startActivity(Intent.createChooser(intent, "Share this app"));
        } else if (id == R.id.rate_us) {
            Log.d("FertiliSense", "Rate Us clicked");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
        } else if (id == R.id.feedback) {
            Log.d("FertiliSense", "Feedback clicked");
            Intent intent = new Intent(MaleReproductiveSystemActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.privacy) {
            Log.d(TAG, "Privacy Policy clicked");
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.disclaimer_content) {
            Log.d(TAG, "Disclaimer clicked");
            Intent intent = new Intent(this, DisclaimerContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.terms_condition) {
            Log.d("FertiliSense", "Terms and Condition clicked");
            Intent intent = new Intent(this, TermsAndConditionContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            // Handle "LOG OUT" action
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MaleReproductiveSystemActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

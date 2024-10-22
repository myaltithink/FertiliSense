package com.fertilisense.fertilisense;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class FemaleReproductiveSystemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth authProfile;
    private BottomNavigationView bottomNavigationView;
    private String appPackageName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_female_reproductive_system); // Use the correct layout for this activity

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                    Log.d("FertiliSense", "Already on the Genital Screen");
                } else if (id == R.id.calendar) {
                    Log.d("FertiliSense", "Checking cycle logs before navigating to CalendarActivity");
                    checkForLoggedCyclesAndShowDialog();
                } else if (id == R.id.home) {
                    Log.d("FertiliSense", "Home clicked");
                    Intent intent = new Intent(FemaleReproductiveSystemActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(FemaleReproductiveSystemActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    Log.d("FertiliSense", "User report details clicked");
                    Intent intent = new Intent(FemaleReproductiveSystemActivity.this, ReportActivity.class);
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
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, MaleReproductiveSystemActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Set the onClickListener for the next button
        nextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Male Reproductive System");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, MaleReproductiveSystemActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Link for Female Reproductive Parts 17

        //Fundus 1
        TextView fundusLink = findViewById(R.id.fundus_link);
        fundusLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to FundusActivity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, FundusActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Developing Follicles 2
        TextView developingFolliclesLink = findViewById(R.id.developing_follicles_link);
        developingFolliclesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Developing Follicles Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, DevelopingFolliclesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Endometrium 3
        TextView endometriumLink = findViewById(R.id.endometrium_link);
        endometriumLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Endometrium Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, EndometriumActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Isthmus of Uterine Tube 4
        TextView isthmusOfUterineTubeLink = findViewById(R.id.isthmus_of_uterine_tube_link);
        isthmusOfUterineTubeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Isthmus of Uterine Tube Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, IsthmusOfUterineTubeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Isthmus of Uterus 5
        TextView isthmusOfUterusLink = findViewById(R.id.isthmus_of_uterus_link);
        isthmusOfUterusLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Isthmus of Uterus Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, IsthmusOfUterusActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Ovarian Ligament 6
        TextView ovarianLigamentLink = findViewById(R.id.ovarian_ligament_link);
        ovarianLigamentLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Ovarian Ligament Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, OvarianLigamentActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Myometrium 7
        TextView myometriumLink = findViewById(R.id.myometrium_link);
        myometriumLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Myometrium Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, MyometriumActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Fallopian Tube 8
        TextView fallopianTubeLink = findViewById(R.id.fallopian_tube_link);
        fallopianTubeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Fallopian Tube Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, FallopianTubeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Infundibulum 9
        TextView infundibulumLink = findViewById(R.id.infundibulum_link);
        infundibulumLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Infundibulum Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, InfundibulumActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Corpus Luteum 10
        TextView corpusLuteumLink = findViewById(R.id.corpus_luteum_link);
        corpusLuteumLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Corpus Luteum Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, CorpusLuteumActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Fimbriae 11
        TextView fimbriaeLink = findViewById(R.id.fimbriae_link);
        fimbriaeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Fimbriae Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, FimbriaeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Ampulla 12
        TextView ampullaLink = findViewById(R.id.ampulla_link);
        ampullaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Ampulla Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, AmpullaActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Uterus 13
        TextView uterusLink = findViewById(R.id.uterus_link);
        uterusLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Uterus Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, UterusActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Perimetrium 14
        TextView perimetriumLink = findViewById(R.id.perimetrium_link);
        perimetriumLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Perimetrium Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, PerimetriumActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Ovary 15
        TextView ovaryLink = findViewById(R.id.ovary_link);
        ovaryLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Ovary Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, OvaryActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Vagina 16
        TextView vaginaLink = findViewById(R.id.vagina_link);
        vaginaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Vagina Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, VaginaActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        //Cervix 17
        TextView cervixLink = findViewById(R.id.cervix_link);
        cervixLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FertiliSense", "Navigating to Cervix Activity");
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, CervixActivity.class);
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
                            Picasso.with(FemaleReproductiveSystemActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .transform(new CircleTransform()) // Make the image circular
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(FemaleReproductiveSystemActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FemaleReproductiveSystemActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Set a click listener to open UserProfileActivity
            headerView.setOnClickListener(v -> {
                Intent intent = new Intent(FemaleReproductiveSystemActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.d("FertiliSense", "No current user logged in");
        }
    }

    private void checkForLoggedCyclesAndShowDialog() {
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("menstrual_cycles")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            showCycleLogDialog();
                        } else {
                            navigateToCalendarActivity();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FemaleReproductiveSystemActivity.this, "Error checking cycle data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCycleLogDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Menstrual Cycle")
                .setMessage("You haven't logged any menstrual cycle yet. Would you like to log it now?")
                .setPositiveButton("OK", (dialog, which) -> navigateToChatBotActivity())
                .setNegativeButton("Cancel", (dialog, which) -> navigateToCalendarActivity())
                .setCancelable(false)
                .show();
    }

    private void navigateToCalendarActivity() {
        Intent intent = new Intent(FemaleReproductiveSystemActivity.this, CalendarActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void navigateToChatBotActivity() {
        Intent intent = new Intent(FemaleReproductiveSystemActivity.this, ChatBotActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
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
            Intent intent = new Intent(FemaleReproductiveSystemActivity.this, FeedbackActivity.class);
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
        } else if (id == R.id.user_manual) {
            Log.d("FertiliSense", "User Manual clicked");
            Intent intent = new Intent(FemaleReproductiveSystemActivity.this, UserManualActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            // Handle "LOG OUT" action
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FemaleReproductiveSystemActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

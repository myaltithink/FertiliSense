package com.fertilisense.fertilisense;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MaleDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private String appPackageName;

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private int[] images = {
            R.drawable.environment1,
            R.drawable.sex1,
            R.drawable.supplements1,
            R.drawable.nutritions1,
            R.drawable.stress1,
            R.drawable.exercise
    };

    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_male_dashboard);

        // Initialize ViewPager2 and TabLayout
        viewPager2 = findViewById(R.id.imageSlider);
        tabLayout = findViewById(R.id.tab_layout);

        // Initialize the appPackageName inside onCreate
        appPackageName = getPackageName();

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();

        // Setup ImageSlider
        setupImageSlider();

        // Initialize DrawerLayout, NavigationView, and BottomNavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set navigation item selected listener for NavigationView
        navigationView.setNavigationItemSelectedListener(this);

        // Set bottom navigation item selected listener
        setupBottomNavigation();

        // Set "Home" as the default selected item in BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Load user information in the navigation header
        loadUserInformation();

        // Handle drawer icon click
        findViewById(R.id.ic_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupImageSlider() {
        // Arrays for descriptions and URLs
        String[] descriptions = {
                "",
                "",
                "",
                "",
                "",
                ""
        };

        String[] urls = {
                "https://rbej.biomedcentral.com/",
                "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3443750/",
                "https://www.mdpi.com/2072-6643/12/5/1472",
                "https://www.health.harvard.edu/blog/fertility-and-diet-is-there-a-connection-2018053113949#:~:text=Making%20Sense%20of%20Vitamins%20and%20Minerals&text=Studies%20of%20men%20have%20found,little%20effect%2C%20good%20or%20bad.",
                "https://rbej.biomedcentral.com/articles/10.1186/s12958-018-0436-9",
                "https://pubmed.ncbi.nlm.nih.gov/25847854/"
        };

        // Setup ImageSlider Adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, images, descriptions, urls);
        viewPager2.setAdapter(adapter);

        // Apply a smooth PageTransformer
        viewPager2.setPageTransformer(new ZoomOutPageTransformer());

        // Setup TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            View tabView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            TextView tabText = tabView.findViewById(R.id.tab_text);
            switch (position) {
                case 0:
                    tabText.setText("Environment");
                    break;
                case 1:
                    tabText.setText("Sex");
                    break;
                case 2:
                    tabText.setText("Supplements");
                    break;
                case 3:
                    tabText.setText("Nutrition");
                    break;
                case 4:
                    tabText.setText("Stress");
                    break;
                case 5:
                    tabText.setText("Exercise");
                    break;
            }
            tab.setCustomView(tabView);
        }).attach();

        // Auto-slide functionality
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            if (currentPage == images.length) {
                viewPager2.setCurrentItem(0, false); // Reset to the first image
                currentPage = 0;
            } else {
                viewPager2.setCurrentItem(currentPage++, true); // Slide with animation
            }
        };

        // Timer for auto-sliding
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 4000, 4000); // Slide every 4 seconds
    }


    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.genital) {
                Log.d("FertiliSense", "Navigating to MaleReproductiveSystemActivity");
                startActivity(new Intent(MaleDashboardActivity.this, MaleReproductiveSystemMenActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.home) {
                Log.d("FertiliSense", "Already on the Home screen");
            } else if (id == R.id.chatbot) {
                Log.d("FertiliSense", "Navigating to ChatBotActivity");
                startActivity(new Intent(MaleDashboardActivity.this, MaleChatBotActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }

            return true;
        });
    }

    private void loadUserInformation() {
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_drawer_username);
        TextView emailTextView = headerView.findViewById(R.id.nav_drawer_email);
        ImageView profilePictureView = findViewById(R.id.nav_header_profile_picture);

        FirebaseUser user = authProfile.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            emailTextView.setText(email);

            // Fetch username from Firebase Realtime Database
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

                        Uri photoUri = user.getPhotoUrl();
                        if (photoUri != null) {
                            Picasso.with(MaleDashboardActivity.this)
                                    .load(photoUri)
                                    .placeholder(R.drawable.ic_user) // Fallback image
                                    .transform(new CircleTransform()) // Circular transformation
                                    .into(profilePictureView);
                        } else {
                            Log.d("FertiliSense", "User does not have a profile picture");
                        }
                    } else {
                        Toast.makeText(MaleDashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MaleDashboardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

            // Open UserProfileActivity when the header is clicked
            headerView.setOnClickListener(v -> startActivity(new Intent(MaleDashboardActivity.this, UserProfileActivity.class)));
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
            finish();
        } else if (id == R.id.rate_us) {
            Log.d("FertiliSense", "Rate Us clicked");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
            finish();
        } else if (id == R.id.feedback) {
            Log.d("FertiliSense", "Feedback clicked");
            Intent intent = new Intent(MaleDashboardActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.privacy) {
            Log.d(TAG, "Privacy Policy clicked");
            Intent intent = new Intent(MaleDashboardActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.disclaimer_content) {
            Log.d(TAG, "Disclaimer clicked");
            Intent intent = new Intent(MaleDashboardActivity.this, DisclaimerContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.terms_condition) {
            Log.d(TAG, "Terms and Condition clicked");
            Intent intent = new Intent(MaleDashboardActivity.this, TermsAndConditionContentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.user_manual) {
            Log.d("FertiliSense", "User Manual clicked");
            Intent intent = new Intent(MaleDashboardActivity.this, UserManualActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.logout) {
            // Handle "LOG OUT" action
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MaleDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

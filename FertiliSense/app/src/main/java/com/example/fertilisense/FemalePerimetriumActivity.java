package com.example.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FemalePerimetriumActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private FirebaseUser currentUser;
    private static final String TAG = "FemalePerimetriumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_female_perimetrium);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Perimetrium Activity");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); // Custom back button drawable
        }

        // Set custom overflow icon
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        currentUser = authProfile.getCurrentUser();

        // Log currently logged-in user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            Log.d(TAG, "Logged in as: " + email);
        }

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Log.d(TAG, "Navigating to FemaleReproductiveSystemActivity");
                    Intent intent = new Intent(FemalePerimetriumActivity.this, FemaleReproductiveSystemFemaleActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.home) {
                    Log.d(TAG, "Dashboard clicked");
                    Intent intent = new Intent(FemalePerimetriumActivity.this, MaleDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(FemalePerimetriumActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                return true;
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.female_menu, menu);
        return true;
    }

    // Handle action bar item clicks here.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle different menu item clicks based on their IDs
        if (id == android.R.id.home) {
            // Back Button
            Intent intent = new Intent(FemalePerimetriumActivity.this, FemaleReproductiveSystemFemaleActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        } else if (id == R.id.female_menu_refresh) {
            // Refresh Activity
            Log.d(TAG, "onOptionsItemSelected: Refresh selected.");
            recreate(); // Recreate the activity to simulate refresh
            return true;
        } else if (id == R.id.female_menu_developing_follicles) {
            Log.d(TAG, "onOptionsItemSelected: Developing Follicles selected.");
            Intent intent = new Intent(this, FemaleDevelopingFolliclesActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fundus) {
            Log.d(TAG, "onOptionsItemSelected: Fundus selected.");
            Intent intent = new Intent(this, FemaleFundusActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_endometrium) {
            Log.d(TAG, "onOptionsItemSelected: Endometrium selected.");
            Intent intent = new Intent(this, FemaleEndometriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_isthmus_of_uterine_tube) {
            Log.d(TAG, "onOptionsItemSelected: Isthmus of Uterine Tube selected.");
            Intent intent = new Intent(this, FemaleIsthmusOfUterineTubeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ovarian_ligament) {
            Log.d(TAG, "onOptionsItemSelected: Ovarian Ligament selected.");
            Intent intent = new Intent(this, FemaleOvarianLigamentActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_myometrium) {
            Log.d(TAG, "onOptionsItemSelected: Myometrium selected.");
            Intent intent = new Intent(this, FemaleMyometriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_isthmus_of_uterus) {
            Log.d(TAG, "onOptionsItemSelected: Isthmus of Uterus selected.");
            Intent intent = new Intent(this, FemaleIsthmusOfUterusActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fallopian_tube) {
            Log.d(TAG, "onOptionsItemSelected: Fallopian Tube selected.");
            Intent intent = new Intent(this, FemaleFallopianTubeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_infundibulum) {
            Log.d(TAG, "onOptionsItemSelected: Infundibulum selected.");
            Intent intent = new Intent(this, FemaleInfundibulumActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_corpus_luteum) {
            Log.d(TAG, "onOptionsItemSelected: Corpus Luteum selected.");
            Intent intent = new Intent(this, FemaleCorpusLuteumActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fimbriae) {
            Log.d(TAG, "onOptionsItemSelected: Fimbriae selected.");
            Intent intent = new Intent(this, FemaleFimbriaeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ampulla) {
            Log.d(TAG, "onOptionsItemSelected: Ampulla selected.");
            Intent intent = new Intent(this, FemaleAmpullaActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_uterus) {
            Log.d(TAG, "onOptionsItemSelected: Uterus selected.");
            Intent intent = new Intent(this, FemaleUterusActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_perimetrium) {
            Log.d(TAG, "onOptionsItemSelected: Perimetrium selected.");
            Intent intent = new Intent(this, FemalePerimetriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ovary) {
            Log.d(TAG, "onOptionsItemSelected: Ovary selected.");
            Intent intent = new Intent(this, FemaleOvaryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_vagina) {
            Log.d(TAG, "onOptionsItemSelected: Vagina selected.");
            Intent intent = new Intent(this, FemaleVaginaActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.female_menu_cervix) {
            Log.d(TAG, "onOptionsItemSelected: Cervix selected.");
            Intent intent = new Intent(this, FemaleCervixActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        }

        // If none of the above conditions are met, delegate to superclass for handling
        return super.onOptionsItemSelected(item);
    }
}
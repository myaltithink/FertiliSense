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

public class EndometriumActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth authProfile;
    private FirebaseUser currentUser;
    private static final String TAG = "EndometriumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endometrium);
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Endometrium Activity");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button); //custom back button drawable
        }

        // Set custom overflow icon
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dot_menu));

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize FirebaseAuth
        authProfile = FirebaseAuth.getInstance();
        currentUser = authProfile.getCurrentUser();

        // Log currently logged-in user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            Log.d(TAG, "Logged in as: " + email);
        }

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.genital) {
                    Log.d("FertiliSense", "Navigating to EndometriumActivity");
                    Intent intent = new Intent(EndometriumActivity.this, FemaleReproductiveSystemActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.calendar) {
                    Log.d(TAG, "Calendar clicked");
                    // Uncomment and complete as needed
                    // Intent intent = new Intent(EndometriumActivity.this, CalendarActivity.class);
                    // startActivity(intent);
                    // overridePendingTransition(0,0);
                    // finish();
                } else if (id == R.id.home) {
                    Log.d(TAG, "Dashboard clicked");
                    Intent intent = new Intent(EndometriumActivity.this, FertiliSenseDashboardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else if (id == R.id.chatbot) {
                    Log.d("FertiliSense", "Chatbot clicked");
                    Intent intent = new Intent(EndometriumActivity.this, ChatBotActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (id == R.id.report) {
                    Log.d("FertiliSense", "User report details clicked");
//                    Intent intent = new Intent(ReportActivity.this, ChatBotActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                    finish();
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
            Intent intent = new Intent(EndometriumActivity.this, FemaleReproductiveSystemActivity.class);
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
            Intent intent = new Intent(this, DevelopingFolliclesActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fundus) {
            Log.d(TAG, "onOptionsItemSelected: Fundus selected.");
            Intent intent = new Intent(this, FundusActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_endometrium) {
            Log.d(TAG, "onOptionsItemSelected: Endometrium selected.");
            Intent intent = new Intent(this, EndometriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_isthmus_of_uterine_tube) {
            Log.d(TAG, "onOptionsItemSelected: Isthmus of Uterine Tube  selected.");
            Intent intent = new Intent(this, IsthmusOfUterineTubeActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ovarian_ligament) {
            Log.d(TAG, "onOptionsItemSelected: Ovarian Ligament selected.");
            Intent intent = new Intent(this, OvarianLigamentActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_myometrium) {
            Log.d(TAG, "onOptionsItemSelected: Myometrium selected.");
            Intent intent = new Intent(this, MyometriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_isthmus_of_uterus) {
            Log.d(TAG, "onOptionsItemSelected: Isthmus of Uterus selected.");
            Intent intent = new Intent(this, IsthmusOfUterusActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fallopian_tube) {
            Log.d(TAG, "onOptionsItemSelected: Fallopian Tube selected.");
            Intent intent = new Intent(this, FallopianTubeActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_infundibulum) {
            Log.d(TAG, "onOptionsItemSelected: Infundibulum selected.");
            Intent intent = new Intent(this, InfundibulumActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_corpus_luteum) {
            Log.d(TAG, "onOptionsItemSelected: Corpus Luteum selected.");
            Intent intent = new Intent(this, CorpusLuteumActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_fimbriae) {
            Log.d(TAG, "onOptionsItemSelected: Fimbriae selected.");
            Intent intent = new Intent(this, FimbriaeActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ampulla) {
            Log.d(TAG, "onOptionsItemSelected: Ampulla selected.");
            Intent intent = new Intent(this, AmpullaActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_uterus) {
            Log.d(TAG, "onOptionsItemSelected: Uterus selected.");
            Intent intent = new Intent(this, UterusActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_perimetrium) {
            Log.d(TAG, "onOptionsItemSelected: Perimetrium selected.");
            Intent intent = new Intent(this, PerimetriumActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_ovary) {
            Log.d(TAG, "onOptionsItemSelected: Ovary selected.");
            Intent intent = new Intent(this, OvaryActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_vagina) {
            Log.d(TAG, "onOptionsItemSelected: Vagina selected.");
            Intent intent = new Intent(this, VaginaActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.female_menu_cervix) {
            Log.d(TAG, "onOptionsItemSelected: Cervix selected.");
            Intent intent = new Intent(this, CervixActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
            return true;
        }

        // If none of the above conditions are met, delegate to superclass for handling
        return super.onOptionsItemSelected(item);
    }
}

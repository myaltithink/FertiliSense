package com.fertilisense.fertilisense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TermsAndCondition extends AppCompatActivity {

    // Terms and Condition Start Up Registration
    private RadioButton checkbox1, checkbox2;
    private Button acceptAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_condition);

        checkbox1 = findViewById(R.id.checkbox1);
        checkbox2 = findViewById(R.id.checkbox2);
        acceptAllButton = findViewById(R.id.acceptAllButton);

        acceptAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox1.setChecked(true);
                checkbox2.setChecked(true);

                Toast.makeText(TermsAndCondition.this, "Accepted all", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(TermsAndCondition.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
    }
}
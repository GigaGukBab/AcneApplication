package com.example.acneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AcneTreatmentActivity extends AppCompatActivity {

    private TextView acneTreatmentInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_treatment);


        Button acne_comedonia_treat_btn = findViewById(R.id.acne_comedonia_treat_btn);
        acne_comedonia_treat_btn.setOnClickListener(view -> {
            Intent i = new Intent(AcneTreatmentActivity.this, AcneComedoniaTreatActivity.class);
            startActivity(i);
        });

        Button acne_papules_treat_btn = findViewById(R.id.acne_papules_treat_btn);
        acne_papules_treat_btn.setOnClickListener(view -> {
            Intent i = new Intent(AcneTreatmentActivity.this, AcnePapulesTreatActivity.class);
            startActivity(i);
        });

        Button acne_pustular_treat_btn = findViewById(R.id.acne_pustular_treat_btn);
        acne_pustular_treat_btn.setOnClickListener(view -> {
            Intent i = new Intent(AcneTreatmentActivity.this, AcnePustularTreatActivity.class);
            startActivity(i);
        });


    }
}

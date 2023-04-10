package com.example.acneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private TextView welcome_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname"); //GoogleLoginActivity로부터 nickname 전달받음

        welcome_tv = findViewById(R.id.mainMenuNickname);
        welcome_tv.setText(nickname); // 닉네임 text를 텍스트뷰에 세팅


        Button galleryBtn = findViewById(R.id.gallaryBtn);
        galleryBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, GalleryActivity.class);
            startActivity(i);
        });

        Button cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, CameraActivity.class);
            startActivity(i);
        });

        Button histBtn = findViewById(R.id.histBtn);
        histBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, HistoryMenuActivity.class);
            startActivity(i);
        });

        Button fireStoreExBtn = findViewById(R.id.fireStoreExampleBtn);
        fireStoreExBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, FireStoreEx.class);
            startActivity(i);
        });



    }

}


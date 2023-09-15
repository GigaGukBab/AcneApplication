package com.example.acneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class AcneTreatmentActivity extends AppCompatActivity {

    private TextView acneTreatmentInfoTextView;

    // 클래스 내 멤버 변수 추가
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private String userNickname;
    private String userProfilePictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_treatment);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname"); //GoogleLoginActivity로부터 nickname 전달받음
        String profilePictureUrl = intent.getStringExtra("profile_picture");

        // 사용자 이름 및 프로필 사진 가져오기
        String displayName = getIntent().getStringExtra("displayName");
        String photoUrl = getIntent().getStringExtra("photoUrl");

        // NavigationView에서 헤더 뷰 참조 가져오기
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTextView = headerView.findViewById(R.id.nav_header_nickname);
        ImageView navHeaderImageView = headerView.findViewById(R.id.nav_header_profile_picture);

        navHeaderTextView.setText(nickname);

        // 이미지 로딩 라이브러리인 Glide를 사용하여 프로필 사진을 로드하고 ImageView에 설정
        Glide.with(this)
                .load(profilePictureUrl)
                .circleCrop()
                .into(navHeaderImageView);

        // 사용자 이름 및 프로필 사진 설정
        if (displayName != null) {
            navHeaderTextView.setText(displayName);
        }
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(navHeaderImageView);
        }

        // onCreate 메서드 내에 아래 코드 추가
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // 홈 버튼을 사용하여 Drawer를 열고 닫을 수 있도록 설정
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                Intent intent;
                switch (id) {
                   case R.id.nav_mypage:
                        Intent MyPageIntent = new Intent(AcneTreatmentActivity.this, MyPageActivity.class);
                        MyPageIntent.putExtra("nickname", nickname);
                        MyPageIntent.putExtra("profile_picture", profilePictureUrl);
                        MyPageIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        MyPageIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(MyPageIntent);
                        break;
                    case R.id.nav_history:
                        Intent historyMenuIntent = new Intent(AcneTreatmentActivity.this, HistoryMenuActivity.class);
                        historyMenuIntent.putExtra("nickname", nickname);
                        historyMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        historyMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        historyMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(historyMenuIntent);
                        break;
                    case R.id.nav_acne_treatment:
                        // 이미 AcneTreatmentActivity에 있으므로 아무 작업도 수행하지 않음
                        break;
//                    case R.id.nav_bookmark:
//                        intent = new Intent(AcneTreatmentActivity.this, BookmarkActivity.class);
//                        startActivity(intent);
//                        break;
                    case R.id.nav_clinicRecommend:
                        Intent AcneClinicRecommendationtMenuIntent = new Intent(AcneTreatmentActivity.this, AcneClinicRecommendationOnGoogleMapActivity.class);
                        AcneClinicRecommendationtMenuIntent.putExtra("nickname", nickname);
                        AcneClinicRecommendationtMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneClinicRecommendationtMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneClinicRecommendationtMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneClinicRecommendationtMenuIntent);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        Button acne_comedonia_treat_btn = findViewById(R.id.acne_comedonia_treat_btn);
        acne_comedonia_treat_btn.setOnClickListener(view -> {
            Intent i = new Intent(AcneTreatmentActivity.this, AcneComedoniaTreatActivity.class);
            i.putExtra("nickname", nickname);
            i.putExtra("profile_picture", profilePictureUrl);
            i.putExtra("displayName", getIntent().getStringExtra("displayName"));
            i.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
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

    // onOptionsItemSelected 메서드를 추가하거나 변경
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // 기존 코드 유지
        return super.onOptionsItemSelected(item);
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

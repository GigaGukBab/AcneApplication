package com.example.acneapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AcnePustularTreatActivity extends AppCompatActivity {
    // 여기에 TextView 변수를 선언
    private TextView acneTreatmentInfoTextView;

    // 클래스 내 멤버 변수 추가
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_pustular_treat);

        acneTreatmentInfoTextView = findViewById(R.id.acneTreatmentInfoTextView);

        // 사용자 프로필 이미지, 닉네임 처리 부분 코드
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
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            Intent intent1;
            switch (id) {
//                    case R.id.nav_mypage:
//                        intent = new Intent(AcneClinicRecommendationActivity.this, MyPageActivity.class);
//                        startActivity(intent);
//                        break;
                case R.id.nav_history:
                    Intent historyMenuIntent = new Intent(AcnePustularTreatActivity.this, HistoryMenuActivity.class);
                    historyMenuIntent.putExtra("nickname", nickname);
                    historyMenuIntent.putExtra("profile_picture", profilePictureUrl);
                    historyMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                    historyMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                    startActivity(historyMenuIntent);
                    break;
                case R.id.nav_acne_treatment:
                    Intent AcneTreatmentMenuIntent = new Intent(AcnePustularTreatActivity.this, AcneTreatmentActivity.class);
                    AcneTreatmentMenuIntent.putExtra("nickname", nickname);
                    AcneTreatmentMenuIntent.putExtra("profile_picture", profilePictureUrl);
                    AcneTreatmentMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                    AcneTreatmentMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                    startActivity(AcneTreatmentMenuIntent);
                    break;
//                    case R.id.nav_bookmark:
//                        intent = new Intent(AcneClinicRecommendationActivity.this, BookmarkActivity.class);
//                        startActivity(intent);
//                        break;
                case R.id.nav_clinicRecommend:
                    Intent AcneClinicRecommendationtMenuIntent = new Intent(AcnePustularTreatActivity.this, AcneClinicRecommendationOnGoogleMapActivity.class);
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
        });
        // 사용자 프로필 이미지, 닉네임 처리 부분 코드 종료료

        // Firestore 인스턴스 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String documentID = "acne_pustular_treatment_doc";

        // 이 부분이 뷰를 참조하기 전에 실행되어야 합니다. 이 경우, 뷰가 아직 초기화되지 않았기 때문에 null을 반환할 수 있습니다.
        TextView acneTypeTextView = findViewById(R.id.acneTypeTextView);

        db.collection("acne_treatments").document(documentID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String acneType = document.getString("acne_type");
                            String acneTreatmentMethod = document.getString("acne_treatment_method");
                            String source = document.getString("source");

                            // 데이터가 null이 아니면 TextView에 설정합니다.
                            if (acneType != null) {
                                acneTypeTextView.setText(acneType);
                            }

                            // 아래는 기존 코드를 유지하면서, TextView 참조 코드를 제거했습니다.
                            TextView acneTreatmentInfoTextView = findViewById(R.id.acneTreatmentInfoTextView);
                            String displayText = "관리법: \n" + acneTreatmentMethod;
                            acneTreatmentInfoTextView.setText(displayText);

                            TextView acneTreatmentInfoSourecTextView = findViewById(R.id.acneTreatmentInfoSourecTextView);
                            String displaySourceText = source;
                            acneTreatmentInfoSourecTextView.setText(displaySourceText);

                            acneTreatmentInfoSourecTextView = findViewById(R.id.acneTreatmentInfoSourecTextView);

                            acneTreatmentInfoSourecTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(source));
                                    startActivity(intent);
                                }
                            });
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // 기존 코드 유지
        return super.onOptionsItemSelected(item);
    }

    //드로어 관련 함수
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
package com.example.acneapplication.BookMarkFunc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acneapplication.AcneClinicRecommendationOnGoogleMapActivity;
import com.example.acneapplication.AcneTreatmentActivity;
import com.example.acneapplication.HistoryMenuActivity;
import com.example.acneapplication.MainMenuActivity;
import com.example.acneapplication.MyPageActivity;
import com.example.acneapplication.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BookMarkActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DermatologyAdapter adapter;
    private ArrayList<Dermatology> dermatologyList;
    private String TAG;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark);

//        드로워 관련 코드 시작
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                Intent intent;
                switch (id) {
                    case R.id.nav_mypage:
                        Intent MyPageIntent = new Intent(BookMarkActivity.this, MyPageActivity.class);
                        MyPageIntent.putExtra("nickname", nickname);
                        MyPageIntent.putExtra("profile_picture", profilePictureUrl);
                        MyPageIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        MyPageIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(MyPageIntent);
                        break;
                    case R.id.nav_history:
                        Intent historyMenuIntent = new Intent(BookMarkActivity.this, HistoryMenuActivity.class);
                        historyMenuIntent.putExtra("nickname", nickname);
                        historyMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        historyMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        historyMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(historyMenuIntent);
                        break;
                    // 구현 중
                    case R.id.nav_acne_treatment:
                        Intent AcneTreatmentMenuIntent = new Intent(BookMarkActivity.this, AcneTreatmentActivity.class);
                        AcneTreatmentMenuIntent.putExtra("nickname", nickname);
                        AcneTreatmentMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneTreatmentMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneTreatmentMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneTreatmentMenuIntent);
                        break;
                    case R.id.nav_clinicRecommend:
                        Intent AcneClinicRecommendationtMenuIntent = new Intent(BookMarkActivity.this, AcneClinicRecommendationOnGoogleMapActivity.class);
                        AcneClinicRecommendationtMenuIntent.putExtra("nickname", nickname);
                        AcneClinicRecommendationtMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneClinicRecommendationtMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneClinicRecommendationtMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneClinicRecommendationtMenuIntent);
                        break;
                    case R.id.nav_bookmark:
                        Intent AcneClinicBookmarkIntent = new Intent(BookMarkActivity.this, BookMarkActivity.class);
                        AcneClinicBookmarkIntent.putExtra("nickname", nickname);
                        AcneClinicBookmarkIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneClinicBookmarkIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneClinicBookmarkIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneClinicBookmarkIntent);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        recyclerView = findViewById(R.id.recyclerView_bookmark);
        dermatologyList = new ArrayList<>();
        adapter = new DermatologyAdapter(dermatologyList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Firestore에서 데이터 불러오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookmark_places").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Dermatology dermatology = document.toObject(Dermatology.class);
                    dermatologyList.add(dermatology);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // 네비게이션 버튼 클릭 처리
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

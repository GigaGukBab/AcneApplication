package com.example.acneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acneapplication.BookMarkFunc.BookMarkActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


public class HistoryMenuActivity extends AppCompatActivity {

    public static final String TAG = "[IC]HistoryMenuActivity";
    private HistoryAdapter historyAdapter; // Declare historyAdapter as a class member
    private HistoryAdapter.OnDeleteClickListener onDeleteClickListener;
    private FirebaseFirestore firestore;
    private List<Classification> classifications;
    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private String userNickname;
    private String userProfilePictureUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_menu);

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
                    case R.id.nav_history:
                        // 이미 HistoryMenuActivity에 있으므로 아무 작업도 수행하지 않음
                        break;
                    case R.id.nav_acne_treatment:
                        Intent AcneTreatmentMenuIntent = new Intent(HistoryMenuActivity.this, AcneTreatmentActivity.class);
                        AcneTreatmentMenuIntent.putExtra("nickname", nickname);
                        AcneTreatmentMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneTreatmentMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneTreatmentMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneTreatmentMenuIntent);
                        break;
                    case R.id.nav_bookmark:
                        Intent AcneClinicBookmarkIntent = new Intent(HistoryMenuActivity.this, BookMarkActivity.class);
                        AcneClinicBookmarkIntent.putExtra("nickname", nickname);
                        AcneClinicBookmarkIntent.putExtra("profile_picture", profilePictureUrl);
                        AcneClinicBookmarkIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        AcneClinicBookmarkIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(AcneClinicBookmarkIntent);
                        break;
                    case R.id.nav_clinicRecommend:
                        Intent AcneClinicRecommendationtMenuIntent = new Intent(HistoryMenuActivity.this, AcneClinicRecommendationOnGoogleMapActivity.class);
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

        // 이름순 버튼 클릭 리스너 설정
        Button nameBtn = findViewById(R.id.nameBtn);
        Button dateBtn = findViewById(R.id.dateBtn);
        nameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromFirestore("result", Query.Direction.ASCENDING);
                nameBtn.setBackgroundColor(ContextCompat.getColor(HistoryMenuActivity.this, R.color.selected_button_text));
                dateBtn.setBackgroundColor(ContextCompat.getColor(HistoryMenuActivity.this, R.color.default_button_text));
            }
        });
        // 날짜순 버튼 클릭 리스너 설정
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromFirestore("timestamp", Query.Direction.DESCENDING); // 날짜 필드의 이름을 "date"라고 가정합니다.
                nameBtn.setBackgroundColor(ContextCompat.getColor(HistoryMenuActivity.this, R.color.default_button_text));
                dateBtn.setBackgroundColor(ContextCompat.getColor(HistoryMenuActivity.this, R.color.selected_button_text));
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classifications = new ArrayList<>();

        // Set an empty adapter by default
        onDeleteClickListener = new HistoryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Classification classification) {
                // Firestore에서 문서 삭제
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("classifications")
                        .document(classification.getDocumentId()) // Firestore 문서 ID를 얻습니다.
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // 삭제 성공 시, 로컬 데이터에서 삭제하고 RecyclerView 업데이트
                                classifications.remove(classification);
                                historyAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
                                Toast.makeText(HistoryMenuActivity.this, "분류 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HistoryMenuActivity.this, "데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        };
        // historyAdapter 초기화
        historyAdapter = new HistoryAdapter(classifications, onDeleteClickListener);


        recyclerView.setAdapter(historyAdapter);

        // Initialize the Firestore instance
        firestore = FirebaseFirestore.getInstance();

        loadDataFromFirestore("timestamp", Query.Direction.DESCENDING);

    }

    private void loadDataFromFirestore(String orderByField, Query.Direction direction) {
        firestore.collection("classifications")
                .orderBy(orderByField, direction) // 정렬 기준을 인자로 사용합니다.
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        classifications.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Classification classification = document.toObject(Classification.class);
                            if (classification != null) {
                                // Set the document ID to the Classification object
                                classification.setDocumentId(document.getId());
                                classifications.add(classification);
                            }
                        }
                        historyAdapter.setClassifications(classifications);
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("HistoryMenuActivity", "Error getting documents: ", task.getException());
                    }
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
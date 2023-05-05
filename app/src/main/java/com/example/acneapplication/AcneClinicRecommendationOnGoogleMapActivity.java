package com.example.acneapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class AcneClinicRecommendationOnGoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mapView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        // FusedLocationProviderClient 초기화
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 지도 객체 초기화
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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
                    Intent historyMenuIntent = new Intent(AcneClinicRecommendationOnGoogleMapActivity.this, HistoryMenuActivity.class);
                    historyMenuIntent.putExtra("nickname", nickname);
                    historyMenuIntent.putExtra("profile_picture", profilePictureUrl);
                    historyMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                    historyMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                    startActivity(historyMenuIntent);
                    break;
                case R.id.nav_acne_treatment:
                    Intent AcneTreatmentMenuIntent = new Intent(AcneClinicRecommendationOnGoogleMapActivity.this, AcneTreatmentActivity.class);
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
                    break;
                default:
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        // 사용자 프로필 이미지, 닉네임 처리 부분 코드 종료
    }

    private void fetchNearbySkinClinics(LatLng userLocation) {
        // Places API 초기화
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

        // 검색 반경 설정
        int radius = 5000; // 5km 반경 내 피부과 검색
        String[] placeTypes = {"doctor"};
        String[] keywords = {"피부과", "피부과 전문의", "피부 클리닉", "피부과 의원", "여드름 치료", "여드름 흉터치료"};
        String locationString = userLocation.latitude + "," + userLocation.longitude;

        // RequestQueue 생성
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // 모든 placeType과 키워드에 대해 요청 수행
        for (String placeType : placeTypes) {
            for (String keyword : keywords) {
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationString + "&radius=" + radius + "&type=" + placeType + "&keyword=" + keyword + "&key=" + getString(R.string.google_maps_key);

                // 사용자 위치를 기반으로 피부과 검색
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                JSONObject geometry = result.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double lat = location.getDouble("lat");
                                double lng = location.getDouble("lng");
                                String name = result.getString("name");

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(name));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Places API", "Error fetching nearby skin clinics", error);
                    }
                });

                // 병렬 요청을 위해 requestQueue에 request 추가
                requestQueue.add(request);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 사용자 위치 활성화
        mMap.setMyLocationEnabled(true);

        // 줌 인/아웃 버튼 활성화
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // 사용자 위치 가져오기
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // 사용자 위치 설정 및 지도 줌
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                            // 사용자 위치 기반으로 주변 피부과 검색 및 마커 추가
                            fetchNearbySkinClinics(userLocation);
                        }
                    }
                });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String clinicName = marker.getTitle();
                try {
                    // 네이버 지도 앱으로 검색
                    String uri = "nmap://search?query=" + Uri.encode(clinicName) + "&appname=" + getPackageName();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setPackage("com.nhn.android.nmap");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // 네이버 지도 앱이 설치되어 있지 않다면 Google Play 스토어로 이동
                    Uri playStoreUri = Uri.parse("market://details?id=com.nhn.android.nmap");
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);
                    try {
                        startActivity(playStoreIntent);
                    } catch (ActivityNotFoundException ex) {
                        // Google Play 스토어가 없으면 웹에서 열기
                        playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=com.nhn.android.nmap");
                        playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);
                        startActivity(playStoreIntent);
                    }
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //드로어 관련 함수
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

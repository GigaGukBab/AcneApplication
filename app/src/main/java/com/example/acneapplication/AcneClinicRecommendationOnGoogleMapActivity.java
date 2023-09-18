package com.example.acneapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AcneClinicRecommendationOnGoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private MapView mapView;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String TAG;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HashMap<String, Marker> markerMap = new HashMap<>();

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
                case R.id.nav_mypage:
                    Intent MyPageIntent = new Intent(AcneClinicRecommendationOnGoogleMapActivity.this, MyPageActivity.class);
                    MyPageIntent.putExtra("nickname", nickname);
                    MyPageIntent.putExtra("profile_picture", profilePictureUrl);
                    MyPageIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                    MyPageIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                    startActivity(MyPageIntent);
                    break;
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

                            // Handler를 이용하여 북마크된 장소를 가져오는 메서드를 지연 실행합니다.
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Firestore에서 북마크된 장소 확인하여 노란색 마커로 표시
                                    fetchBookmarkedPlaces();
                                }
                            }, 1500);  // 3000은 3초를 의미합니다.
                        }
                    }
                });

        // 마커 정보 창 클릭 시 동작 정의
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                final String clinicName = marker.getTitle();

                // Firestore에서 해당 마커의 정보를 찾습니다.
                db.collection("bookmark_places")
                        .document(clinicName)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // 마커가 이미 북마크된 경우, '북마크 삭제' 옵션을 보여줍니다.
                                        showAlertDialog(marker, true);
                                    } else {
                                        // 마커가 북마크되지 않은 경우, '북마크 추가' 옵션만 보여줍니다.
                                        showAlertDialog(marker, false);
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void fetchNearbySkinClinics(LatLng userLocation) {
        // Places API 초기화
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

        // 검색 반경 설정
        int radius = 5000; // 5km 반경 내 피부과 검색
        String[] placeTypes = {"doctor"};
        String[] keywords = {"피부", "피부과", "피부 클리닉", "여드름 치료", "여드름 흉터치료"};
        String locationString = userLocation.latitude + "," + userLocation.longitude;

        // RequestQueue 생성
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // 중복된 마커를 추적하기 위한 HashSet
        Set<String> addedClinics = new HashSet<>();

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

                                // 중복된 마커를 제거하기 위해 이름과 위치를 기반으로 키 생성
                                String uniqueKey = name + "-" + lat + "-" + lng;

                                if (!addedClinics.contains(uniqueKey)) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name));
                                    addedClinics.add(uniqueKey);
                                }
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

    private void changeMarkerToCustomIcon(Marker marker) {
        BitmapDescriptor customIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_favorite);
        marker.setIcon(customIcon);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        db.collection("bookmark_places")
                .document(marker.getTitle())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 이미 북마크된 장소인 경우, 대화상자에 '북마크 삭제' 옵션을 추가합니다.
                                showAlertDialog(marker, true);
                                // 선택된 북마크된 장소의 마커를 사용자 정의 아이콘으로 변경합니다.
                                changeMarkerToCustomIcon(marker);
                            } else {
                                // 아직 북마크되지 않은 장소인 경우, 대화상자에 '북마크 추가' 옵션만 있습니다.
                                showAlertDialog(marker, false);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
        return false;
    }

    private void fetchBookmarkedPlaces() {
        db.collection("bookmark_places")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                String name = document.getString("name");

                                // 사용자 정의 아이콘을 비트맵으로 가져옵니다.
                                BitmapDescriptor customIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_favorite);

                                // 각 북마크된 장소를 지도에 사용자 정의 아이콘 마커로 추가합니다.
                                MarkerOptions options = new MarkerOptions();
                                options.position(new LatLng(latitude, longitude));
                                options.title(name);
                                options.icon(customIcon); // 사용자 정의 아이콘 설정
                                Marker marker = mMap.addMarker(options);
                                marker.setTag(name);
                                markerMap.put(name, marker);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void showAlertDialog(final Marker marker, boolean isBookmarked) {
        // 대화상자를 만듭니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(AcneClinicRecommendationOnGoogleMapActivity.this);
        builder.setTitle(marker.getTitle());
        if (isBookmarked) {
            // 마커가 이미 북마크된 경우, '북마크 삭제' 옵션과 '네이버 지도에서 보기' 옵션을 추가합니다.
            builder.setItems(R.array.options_with_unbookmark_and_naver_map, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        // '북마크 삭제'를 선택했을 때
                        removeBookmark(marker.getTitle());
                    } else if (which == 1) {
                        // '네이버 지도에서 보기'를 선택했을 때
                        openNaverMap(marker.getTitle());
                    }
                }
            });
        } else {
            // 마커가 북마크되지 않은 경우, '북마크 추가' 옵션과 '네이버 지도에서 보기' 옵션을 추가합니다.
            builder.setItems(R.array.options_and_naver_map, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        // '북마크에 추가'를 선택했을 때
                        saveBookmark(marker.getPosition(), marker.getTitle());
                    } else if (which == 1) {
                        // '네이버 지도에서 보기'를 선택했을 때
                        openNaverMap(marker.getTitle());
                    }
                }
            });
        }
        builder.create().show();
    }

    private static final String NAVER_MAP_PACKAGE_NAME = "com.nhn.android.nmap";
    private static final String NAVER_MAP_URI_FORMAT = "geo:0,0?q=%s";
    private static final String STORE_URI_FORMAT = "market://details?id=%s";

    public void openNaverMap(String placeName) {
        try {
            // 네이버 지도 앱에서 주어진 장소명으로 검색
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(NAVER_MAP_URI_FORMAT, Uri.encode(placeName))));
            intent.setPackage(NAVER_MAP_PACKAGE_NAME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 해당 Intent를 처리할 수 있는 액티비티가 있는지 확인
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // 네이버 지도 앱이 설치되어 있지 않은 경우, 토스트 메시지 출력 후 Google Play 스토어 앱으로 네이버 지도 앱 설치 페이지로 이동
                Toast.makeText(this, "네이버 지도 앱이 설치되어 있지 않습니다. 설치 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(STORE_URI_FORMAT, NAVER_MAP_PACKAGE_NAME)));
                startActivity(storeIntent);
            }
        } catch (Exception e) {
            Log.e("openNaverMap", "네이버 지도를 여는 중 오류 발생", e);
            Toast.makeText(this, "네이버 지도를 여는 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBookmark(LatLng position, String name) {
        // Firestore 인스턴스를 가져옵니다.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 저장할 데이터를 만듭니다.
        Map<String, Object> bookmark = new HashMap<>();
        bookmark.put("latitude", position.latitude);
        bookmark.put("longitude", position.longitude);
        bookmark.put("name", name); // 장소의 이름을 추가합니다.

        // Firestore 데이터베이스에 데이터를 추가합니다.
        db.collection("bookmark_places")
                .document(name)
                .set(bookmark)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 북마크를 추가한 후에는 지도를 새로고침하여 모든 마커를 다시 로드합니다.
                        //mMap.clear(); // 지도상의 모든 마커를 삭제합니다.
                        markerMap.clear(); // markerMap을 비웁니다.
                        fetchBookmarkedPlaces(); // Firestore에서 북마크된 장소 확인하여 노란색 마커로 표시

                        // 토스트 메시지를 출력합니다.
                        Toast.makeText(AcneClinicRecommendationOnGoogleMapActivity.this,  "해당 피부과가 북마크에 추가되었습니다 :)", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void removeBookmark(String name) {
        // Firestore 인스턴스를 가져옵니다.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore 데이터베이스에서 데이터를 삭제합니다.
        db.collection("bookmark_places")
                .document(name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 북마크를 삭제한 후에는 해당 마커의 색상을 변경하고 markerMap에서 제거합니다.
                        Marker marker = changeMarkerColor(name, BitmapDescriptorFactory.HUE_RED);
                        if (marker != null) {
                            markerMap.remove(name);
                        }
                        // 토스트 메시지를 출력합니다.
                        Toast.makeText(AcneClinicRecommendationOnGoogleMapActivity.this, "해당 피부과가 북마크에서 삭제되었습니다 :/", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private Marker changeMarkerColor(String name, float color) {
        Marker marker = markerMap.get(name);
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(color));
        }
        return marker;
    }

    private void changeMarkerColor(Marker marker, float color) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(color));
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

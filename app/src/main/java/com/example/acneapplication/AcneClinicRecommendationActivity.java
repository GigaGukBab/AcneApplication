package com.example.acneapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// LOCATION_PERMISSION_REQUEST_CODE 상수를 생성하여 위치 권한 요청 코드를 정의합니다.
// onCreate 메서드에서 위치 권한이 이미 부여되었는지 확인하고, 그렇지 않은 경우 권한을 요청합니다.
// onRequestPermissionsResult 메서드에서 위치 권한 요청 결과를 처리합니다. 권한이 승인되면 토스트 메시지로 알립니다.
// onMapReady 메서드에서 Naver 지도의 위치 추적 모드를 사용자의 위치를 따르도록 설정합니다.
// 사용자의 위치가 변경될 때마다 NaverMap.OnLocationChangeListener를 사용하여 콜백을 설정하고, 사용자 위치에 마커를 추가합니다.
//  addMarkerAtLocation 메서드는 지정된 위치에 마커를 추가하는 역할을 합니다.

public class AcneClinicRecommendationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    private Button searchNearbyClinicsButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.5665, 126.9780); // Seoul, South Korea
    private FusedLocationProviderClient fusedLocationClient;

    private NaverApiService naverApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_clinic_recommendation);

        searchNearbyClinicsButton = findViewById(R.id.search_nearby_clinics_button);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Naver API Service 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverApiService = retrofit.create(NaverApiService.class);

        Button searchNearbyClinicsButton = findViewById(R.id.search_nearby_clinics_button);

        searchNearbyClinicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AcneClinicRecommendationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(AcneClinicRecommendationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.getLastLocation().addOnSuccessListener(AcneClinicRecommendationActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                searchNearbyClinics(naverMap, latLng);
                            } else {
                                Toast.makeText(AcneClinicRecommendationActivity.this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    ActivityCompat.requestPermissions(AcneClinicRecommendationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인된 경우
                Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
                // 버튼 클릭 이벤트를 다시 호출합니다.
                searchNearbyClinicsButton.callOnClick();
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "위치 권한이 거부되었습니다. 지도 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraPosition cameraPosition = new CameraPosition(latLng, 15);
                            naverMap.setCameraPosition(cameraPosition);
                        } else {
                            CameraPosition cameraPosition = new CameraPosition(DEFAULT_LOCATION, 15);
                            naverMap.setCameraPosition(cameraPosition);
                        }
                    }
                });

        // 사용자 위치 변경 리스너 설정
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // addMarkerAtLocation(naverMap, latLng);
            }
        });

    }

    private void searchNearbyClinics(NaverMap naverMap, LatLng latLng) {
        String clientId = "qqpxkmsi4j";
        String clientSecret = "iDB7s0f1Wa2tMBr9Kr91SoPzf3BWatdtRW5Rps5N";
        String query = "피부과";
        int display = 10;
        int start = 1;
        String sort = "distance";
        int radius = 20000; // 검색 범위를 미터 단위로 설정합니다. 여기서는 10000 미터로 설정했습니다.

        naverApiService.searchLocal(clientId, clientSecret, query, display, start, sort, latLng.longitude, latLng.latitude, radius).enqueue(new Callback<NaverSearchResult>() {
            @Override
            public void onResponse(Call<NaverSearchResult> call, Response<NaverSearchResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 로그를 추가하여 API 요청이 성공적으로 이루어졌음을 확인합니다.
                    Log.d("API_SUCCESS", "API 요청 성공");

                    for (NaverSearchResult.NaverSearchItem item : response.body().getItems()) {
                        double mapX = item.getMapX();
                        double mapY = item.getMapY();
                        LatLng clinicLatLng = new LatLng(mapY, mapX);
                        addClinicMarker(naverMap, clinicLatLng, item.getTitle());
                    }
                }
            }

            @Override
            public void onFailure(Call<NaverSearchResult> call, Throwable t) {
                // 로그를 추가하여 오류 메시지를 확인할 수 있도록 합니다.
                Log.e("API_ERROR", "피부과 검색 실패: ", t);
                Toast.makeText(AcneClinicRecommendationActivity.this, "피부과 검색 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addClinicMarker(NaverMap naverMap, LatLng latLng, String title) {
        Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setMap(naverMap);
        marker.setCaptionText(title);
        marker.setCaptionRequestedWidth(200);
        marker.setCaptionColor(Color.BLUE);
        marker.setCaptionHaloColor(Color.WHITE);
        marker.setCaptionTextSize(12);
    }

    private void addMarkerAtLocation(NaverMap naverMap, LatLng latLng) {
        Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setMap(naverMap);
    }
}

package com.example.acneapplication.NaverMapsFiles;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.acneapplication.R;
import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AcneClinicRecommendationOnNaverMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private NaverMap naverMap;
    private Location lastSearchLocation = null;
    private long lastSearchTime = 0;
    private static final long SEARCH_TIME_INTERVAL = 30000; // 30초
    private static final float SEARCH_DISTANCE_INTERVAL = 100; // 100m


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_clinic_recommendation);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        checkLocationPermission();

//      사용자 프로필 이미지, 닉네임 처리 부분 코드 시작    //

//        사용자 프로필 이미지, 닉네임 처리 부분 코드 종료      //

        // 지도초기화
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    public static class SkinClinic {
        String title;
        String link;
        String category;
        String description;
        String telephone;
        String address;
        String roadAddress;
        double mapx;
        double mapy;
    }

    public static class SkinClinicSearchResult {
        String lastBuildDate;
        int total;
        int start;
        int display;
        List<SkinClinic> items;
    }

    private void searchSkinClinics(double latitude, double longitude) {
        String clientId = "OLHj8Um3kgdqHaFA1xiF";
        String clientSecret = "Qc0bvWFohc";
        String query = "피부과";
        int display = 20; // 반환 결과 개수 설정
        int radius = 5000; // 검색 반경 설정 (단위: m)

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NaverPlaceApiService service = retrofit.create(NaverPlaceApiService.class);

        Call<ResponseBody> call = service.searchSkinClinics(clientId, clientSecret, query, display, radius, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        SkinClinicSearchResult searchResult = gson.fromJson(responseBody, SkinClinicSearchResult.class);

                        // 여기에 로그 출력 추가
                        for (SkinClinic clinic : searchResult.items) {
                            Log.d("SkinClinicSearch", "Clinic: " + clinic.title + ", Coordinates: (" + clinic.mapx + ", " + clinic.mapy + ")");
                        }

                        // 지도에 피부과 마커 추가
                        for (SkinClinic clinic : searchResult.items) {

                            LatLng latLng = utmToLatLng(clinic.mapx, clinic.mapy);

                            Marker marker = new Marker();

                            marker.setPosition(latLng);
                            marker.setCaptionText(Html.fromHtml(clinic.title, Html.FROM_HTML_MODE_LEGACY).toString());
                            marker.setMap(naverMap);

                            Log.d("SkinClinicSearch", "Adding marker at: (" + latitude + ", " + longitude + ")");
                        }


                        // JSON 처리 및 UI 업데이트
                        Log.d("SkinClinicSearch", responseBody);
                    } catch (IOException e) {
                        Log.e("SkinClinicSearch", "응답 처리 중 오류 발생", e);
                    }
                } else {
                    Log.e("SkinClinicSearch", "API 호출 실패: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("SkinClinicSearch", "API 호출 실패", t);
            }
        });
    }


    public LatLng utmKToWGS84(double utmK_x, double utmK_y) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem utmKCRS = crsFactory.createFromName("EPSG:5178"); // UTM-K 좌표계
        CoordinateReferenceSystem wgs84CRS = crsFactory.createFromName("EPSG:4326"); // WGS84 좌표계

        BasicCoordinateTransform transform = new BasicCoordinateTransform(utmKCRS, wgs84CRS);
        ProjCoordinate utmKCoord = new ProjCoordinate(utmK_x, utmK_y);
        ProjCoordinate wgs84Coord = new ProjCoordinate();

        transform.transform(utmKCoord, wgs84Coord);

        return new LatLng(wgs84Coord.y, wgs84Coord.x);
    }

    private LatLng utmToLatLng(double x, double y) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem srcCRS = crsFactory.createFromName("EPSG:5178");
        CoordinateReferenceSystem destCRS = crsFactory.createFromName("EPSG:4326");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(srcCRS, destCRS);

        ProjCoordinate srcCoord = new ProjCoordinate(x, y);
        ProjCoordinate destCoord = new ProjCoordinate();

        transform.transform(srcCoord, destCoord);

        return new LatLng(destCoord.y, destCoord.x);
    }


//    private void addMarkersAndInfoWindows(List<SkinClinic> skinClinics) {
//        for (SkinClinic clinic : skinClinics) {
//            double lat = Double.parseDouble(clinic.mapy);
//            double lng = Double.parseDouble(clinic.mapx);
//            LatLng latLng = new LatLng(lat, lng);
//
//            Marker marker = new Marker();
//            marker.setPosition(latLng);
//            marker.setMap(naverMap);
//            marker.setCaptionText(clinic.title);
//            marker.setCaptionRequestedWidth(200);
//            marker.setCaptionTextSize(16);
//            marker.setCaptionColor(Color.BLACK);
//            marker.setCaptionAligns(Align.Top);
//        }
//    }



    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        NaverMap.OnLocationChangeListener onLocationChangeListener = new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // 현재 시간을 가져옵니다.
                long currentTime = System.currentTimeMillis();

                // 마지막 검색 위치와 거리를 계산합니다.
                float distance = lastSearchLocation != null ? location.distanceTo(lastSearchLocation) : SEARCH_DISTANCE_INTERVAL + 1;

                // 시간과 거리 기준을 만족하는 경우에만 검색을 실행합니다.
                if ((currentTime - lastSearchTime) >= SEARCH_TIME_INTERVAL && distance >= SEARCH_DISTANCE_INTERVAL) {
                    searchSkinClinics(latitude, longitude);

                    // 마지막 검색 시간 및 위치를 업데이트합니다.
                    lastSearchTime = currentTime;
                    lastSearchLocation = location;
                }
            }
        };

        naverMap.addOnLocationChangeListener(onLocationChangeListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

}

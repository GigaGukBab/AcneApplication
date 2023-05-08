package com.example.acneapplication;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NaverPlaceApiService {
    @GET("v1/search/local")
    Call<ResponseBody> searchSkinClinics(
            @Header("X-Naver-Client-Id") String clientId,
            @Header("X-Naver-Client-Secret") String clientSecret,
            @Query("query") String query,
            @Query("display") int display,
            @Query("radius") int radius,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude
    );
}
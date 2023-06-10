//package com.example.acneapplication.kakaoMapFiles;
//
//import com.example.acneapplication.ResultSearchKeyword.ResultSearchKeyword;
//
//import retrofit2.Call;
//import retrofit2.http.GET;
//import retrofit2.http.Header;
//import retrofit2.http.Query;
//
//public interface KakaoAPI {
//    @GET("v2/local/search/keyword.json")
//    Call<ResultSearchKeyword> getSearchKeyword(  // 받아온 정보가 ResultSearchKeyword 클래스의 구조로 담김
//            @Header("Authorization") String key, // 카카오 API 인증키 [필수]
//            @Query("query") String query,        // 검색을 원하는 질의어 [필수]
//            @Query("latitude") String latitude,
//            @Query("longitude") String longitude,
//            @Query("radius") int radius
//
//             // 매개변수 추가 가능
//            // @Query("category_group_code") category: String
//    );
//}

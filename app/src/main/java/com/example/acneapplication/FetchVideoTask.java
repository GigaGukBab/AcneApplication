package com.example.acneapplication;

import android.os.AsyncTask;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

class FetchVideoTask extends AsyncTask<String, Void, List<SearchResult>> {
    private static final String API_KEY = "AIzaSyCK1j6BJCME6fcj2PSvWIHP1RXr4QMgP7I";

    private WebView webView;
    private TextView channelSource;


    @Override
    protected List<SearchResult> doInBackground(String... keywords) {
        try {
            // YouTube Data API 클라이언트 초기화
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new GsonFactory(), null)
                    .setApplicationName("your-app-name")
                    .build();

            // 검색 요청 생성
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(API_KEY);
            search.setQ(keywords[0]);
            search.setType("video");
            search.setFields("items(id/videoId,snippet/publishedAt,snippet/title,snippet/channelId,snippet/channelTitle)");
            search.setMaxResults(10L);

            // 검색 요청 실행
            SearchListResponse searchResponse = search.execute();
            return searchResponse.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<SearchResult> searchResults) {
        if (searchResults != null && !searchResults.isEmpty()) {
            // 매일 다른 동영상 선택 (예: 오늘 날짜와 동영상 개수를 모듈로 사용)
            int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            int index = today % searchResults.size();

            // 선택한 동영상 정보 가져오기
            SearchResult selectedVideo = searchResults.get(index);
            String videoId = selectedVideo.getId().getVideoId();
            String channelTitle = selectedVideo.getSnippet().getChannelTitle();
            String channelId = selectedVideo.getSnippet().getChannelId();

            // WebView 업데이트
            String embedUrl = "https://www.youtube.com/embed/" + videoId;

            webView.loadData("<iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>", "text/html", "utf-8");

            // 출처 TextView 업데이트
            channelSource.setText(channelTitle);
        } else {
            // 동영상이 없거나 검색 결과가 없는 경우 처리
            webView.loadData("동영상을 불러오지 못했습니다.", "text/html", "utf-8");
            channelSource.setText("");
        }
    }

}



package com.example.acneapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;


public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    // 멤버 변수 추가
    private ImageView profileImage;
    private TextView profileName;
    private WebView webView;
    private TextView channelSource;

    // 멤버 변수 추가
    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View customView;

    // FetchVideoTask를 내부 클래스로 이동합니다.
    private class FetchVideoTask extends AsyncTask<String, Void, List<SearchResult>> {
        // 기존 구현은 그대로 둡니다.
        private static final String API_KEY = "AIzaSyCK1j6BJCME6fcj2PSvWIHP1RXr4QMgP7I";
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
                webView.loadData("<iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "\" frameborder=\"0\" allowfullscreen></iframe>", "text/html", "utf-8");

                // 출처 TextView 업데이트
                channelSource.setText("출처: " + channelTitle);

//                // YouTube 앱으로 이동하는 버튼 생성
//                Button openYouTubeButton = new Button(webView.getContext());
//                openYouTubeButton.setText("YouTube 앱에서 보기");
//                openYouTubeButton.setOnClickListener(view -> {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    view.getContext().startActivity(intent);
//                });
//
//                // 기존 레이아웃에 버튼 추가
//                ((ViewGroup) webView.getParent()).addView(openYouTubeButton);

                // YouTube 앱으로 이동하는 버튼 찾기
                Button openYouTubeButton = ((ViewGroup) webView.getParent()).findViewById(R.id.open_youtube_button);

                // 버튼에 클릭 리스너 추가
                openYouTubeButton.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                });
            } else {
                // 동영상이 없거나 검색 결과가 없는 경우 처리
                webView.loadData("동영상을 불러오지 못했습니다.", "text/html", "utf-8");
                channelSource.setText("");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // customViewContainer 초기화
        customViewContainer = findViewById(R.id.custom_view_container);

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
            profileName.setText(displayName);
        }
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .into(profileImage);
        }

        // Drawer setup
        drawer = findViewById(R.id.drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // WebView 및 channelSource 초기화
        webView = findViewById(R.id.web_view);
        channelSource = findViewById(R.id.channel_source);

        // WebView 설정
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowUniversalAccessFromFileURLs(true); // 이 부분 추가
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        Button galleryBtn = findViewById(R.id.gallaryBtn);
        galleryBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, GalleryActivity.class);
            startActivity(i);
        });

        Button cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, CameraActivity.class);
            startActivity(i);
        });

        // FetchVideoTask 실행
        String keyword = generateDailyKeyword(); // 매일 다른 검색어 생성
        new FetchVideoTask().execute(keyword);

        // WebView 설정 및 웹뷰 클라이언트 설정
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                customViewContainer.addView(view);
                customView = view;
                customViewCallback = callback;
                webView.setVisibility(View.GONE);
                customViewContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHideCustomView() {
                webView.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                customViewContainer.removeView(customView);
                customViewCallback.onCustomViewHidden();
                customView = null;
            }
        });
    }

    private String generateDailyKeyword() {
        String[] keywords = {
                "여드름 관리",
                "지성 피부 관리 루틴",
                "지성 여드름 피부 관리 루틴",
                "여드름 피부 관리 루틴",
                "구진성 여드름 피부 관리",
                "면포성 여드름 피부 관리",
                "결정성 여드름 피부 관리",
                "여드름 짜는 법",
                "여드름 짜야하는 시기",
                "노란색 고름 처리법"
                // 더 많은 검색어를 추가할 수 있습니다
        };

        // 오늘의 날짜를 기준으로 인덱스 생성
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = today % keywords.length;

        // 해당 인덱스의 검색어 반환
        return keywords[index];
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_history:
                startActivity(new Intent(MainMenuActivity.this, HistoryMenuActivity.class));
                break;
                // 구현 중
            case R.id.nav_acne_treatment:
                startActivity(new Intent(MainMenuActivity.this, AcneTreatmentActivity.class));
                break;
            case R.id.nav_clinicRecommend:
                startActivity(new Intent(MainMenuActivity.this, AcneClinicRecommendationActivity.class));
                break;
                // 미구현
//            case R.id.nav_mypage:
//                startActivity(new Intent(MainMenuActivity.this, HistoryMenuActivity.class));
//                break;
//            case R.id.nav_bookmark:
//                startActivity(new Intent(MainMenuActivity.this, AcneTreatmentActivity.class));
//                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}




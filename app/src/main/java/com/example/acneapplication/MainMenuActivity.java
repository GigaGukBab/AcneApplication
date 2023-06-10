package com.example.acneapplication;

import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.acneapplication.BookMarkFunc.BookMarkActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private String userNickname;
    private String userProfilePictureUrl;
    // 종료를 위한 플래그
    private boolean isExitFlag = false;


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

        customViewContainer = findViewById(R.id.custom_view_container);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname");
        String profilePictureUrl = intent.getStringExtra("profile_picture");

        userNickname = getIntent().getStringExtra("nickname");
        userProfilePictureUrl = getIntent().getStringExtra("profile_picture");

        String displayName = getIntent().getStringExtra("displayName");
        String photoUrl = getIntent().getStringExtra("photoUrl");

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTextView = headerView.findViewById(R.id.nav_header_nickname);
        ImageView navHeaderImageView = headerView.findViewById(R.id.nav_header_profile_picture);

        navHeaderTextView.setText(nickname);

        Glide.with(this)
                .load(profilePictureUrl)
                .circleCrop()
                .into(navHeaderImageView);

        if (displayName != null) {
            profileName.setText(displayName);
        }
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .into(profileImage);
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = findViewById(R.id.web_view);
        channelSource = findViewById(R.id.channel_source);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        Button galleryBtn = findViewById(R.id.gallaryBtn);
        galleryBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainMenuActivity.this, AcneClassifyFunctionActivity.class);
            startActivity(i);
        });

//        Button cameraBtn = findViewById(R.id.cameraBtn);
//        cameraBtn.setOnClickListener(view -> {
//            Intent i = new Intent(MainMenuActivity.this, CameraActivity.class);
//            startActivity(i);
//        });

        String keyword = generateDailyKeyword();
        new FetchVideoTask().execute(keyword);

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_mypage:
                        Intent MyPageIntent = new Intent(MainMenuActivity.this, MyPageActivity.class);
                        MyPageIntent.putExtra("nickname", nickname);
                        MyPageIntent.putExtra("profile_picture", profilePictureUrl);
                        MyPageIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        MyPageIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(MyPageIntent);
                        break;
                    case R.id.navigation_home:
                        Intent MainMenuIntent = new Intent(MainMenuActivity.this, MainMenuActivity.class);
                        MainMenuIntent.putExtra("nickname", nickname);
                        MainMenuIntent.putExtra("profile_picture", profilePictureUrl);
                        MainMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                        MainMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                        startActivity(MainMenuIntent);
                        break;
//                    case R.id.navigation_searchPage:
//                        Intent SearchPageIntent = new Intent(MainMenuActivity.this, SearchActivity.class);
//                        SearchPageIntent.putExtra("nickname", nickname);
//                        SearchPageIntent.putExtra("profile_picture", profilePictureUrl);
//                        SearchPageIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
//                        SearchPageIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
//                        startActivity(SearchPageIntent);
//                        break;
                    default:
                        break;
                }
                return true;
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
                "여드름 노란 고름"
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
                Intent historyMenuIntent = new Intent(MainMenuActivity.this, HistoryMenuActivity.class);
                historyMenuIntent.putExtra("nickname", userNickname);
                historyMenuIntent.putExtra("profile_picture", userProfilePictureUrl);
                historyMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                historyMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                startActivity(historyMenuIntent);
                break;
            // 구현 중
            case R.id.nav_acne_treatment:
                Intent AcneTreatmentMenuIntent = new Intent(MainMenuActivity.this, AcneTreatmentActivity.class);
                AcneTreatmentMenuIntent.putExtra("nickname", userNickname);
                AcneTreatmentMenuIntent.putExtra("profile_picture", userProfilePictureUrl);
                AcneTreatmentMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                AcneTreatmentMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                startActivity(AcneTreatmentMenuIntent);
                break;
            case R.id.nav_clinicRecommend:
                Intent AcneClinicRecommendationtMenuIntent = new Intent(MainMenuActivity.this, AcneClinicRecommendationOnGoogleMapActivity.class);
                AcneClinicRecommendationtMenuIntent.putExtra("nickname", userNickname);
                AcneClinicRecommendationtMenuIntent.putExtra("profile_picture", userProfilePictureUrl);
                AcneClinicRecommendationtMenuIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                AcneClinicRecommendationtMenuIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                startActivity(AcneClinicRecommendationtMenuIntent);
                break;
            case R.id.nav_bookmark:
                Intent AcneClinicBookmarkIntent = new Intent(MainMenuActivity.this, BookMarkActivity.class);
                AcneClinicBookmarkIntent.putExtra("nickname", userNickname);
                AcneClinicBookmarkIntent.putExtra("profile_picture", userProfilePictureUrl);
                AcneClinicBookmarkIntent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                AcneClinicBookmarkIntent.putExtra("photoUrl", getIntent().getStringExtra("photoUrl"));
                startActivity(AcneClinicBookmarkIntent);
                break;
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
            // Custom View를 만든다
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            // ImageView를 추가한다
            ImageView image = new ImageView(this);
            image.setImageResource(R.drawable.acne_searching); // 여기서 R.drawable.acne_check는 예시로, 실제로 사용하려면 해당 이미지 리소스가 필요합니다
            layout.addView(image);

            // TextView를 추가한다
            TextView text = new TextView(this);
            text.setText("여드름 한 번 더 체크해 보세요~");
            layout.addView(text);

            // AlertDialog를 만든다
            new AlertDialog.Builder(this)
                    .setView(layout)  // 여기서 만든 Custom View를 설정한다
                    .setCancelable(false)
                    .setPositiveButton("화면으로 돌아가기", null)
                    .setNegativeButton("종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 앱을 종료시킨다
                            finishAffinity();  // 모든 액티비티를 종료시킨다
                            System.exit(0);  // 시스템을 종료시킨다
                        }
                    })
                    .show();
        }
    }
}
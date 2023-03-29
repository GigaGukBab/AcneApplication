package com.example.acneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SignInButton google_lgbtn; // 구글 로그인 버튼
    private FirebaseAuth auth; // 파이버베이스 인증 객체
    private GoogleApiClient googleApiClient; // 구글 API 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE = 100; // 구글 로그인 결과 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 앱이 실행될 때 수행되는 곳
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        auth = FirebaseAuth.getInstance(); // 파이어베이스 인증 객체 초기화

        google_lgbtn = findViewById(R.id.google_lgbtn);
        google_lgbtn.setOnClickListener(new View.OnClickListener() { // 구글 로그인 버튼을 클릭했을 때 이곳을 수행
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, REQ_SIGN_GOOGLE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // 구글 로그인 인증을 요청했을 때 결과 값을 되돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_SIGN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) { // 인증 결과가 성공하면
                GoogleSignInAccount account = result.getSignInAccount(); // account 라는 데이터는 구글로그인 정보를 담고 있음 (ex. 닉네임, 프로필 사진, 이메일 주소... 등)
                resultLogin(account); // 로그인 결과 값 출력하라는 메소드
            }
        }
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) { // 로그인이 성공했으면 ...
                            Toast.makeText(GoogleLoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                            intent.putExtra("nickname", account.getDisplayName());
                            // String.valueOf() : 특정 자료형을 String 형태로 변환
                            // intent.putExtra(("video", String.valueOf(account.getDisplayName(MainMenuActivity))))

                            startActivity(intent);
                        } else { // 로그인이 실패했으면
                            Toast.makeText(GoogleLoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
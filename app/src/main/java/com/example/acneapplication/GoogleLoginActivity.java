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

    private FirebaseAuth auth; // 파이버베이스 인증 객체
    private GoogleApiClient googleApiClient; // 구글 API 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE = 100; // 구글 로그인 결과 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 앱이 실행될 때 수행되는 곳
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        // getHashKey();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        auth = FirebaseAuth.getInstance(); // 파이어베이스 인증 객체 초기화

        // 구글 로그인 버튼
        SignInButton google_lgbtn = findViewById(R.id.google_lgbtn);
        google_lgbtn.setOnClickListener(new View.OnClickListener() { // 구글 로그인 버튼을 클릭했을 때 이곳을 수행
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, REQ_SIGN_GOOGLE);
            }
        });

    }

//    private void getHashKey() {
//        PackageInfo packageInfo = new PackageInfo();
//        try {
//            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        for (Signature signature : packageInfo.signatures) {
//            try {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.e("KEY_HASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            } catch (NoSuchAlgorithmException e) {
//                Log.e("KEY_HASH", "Unable to get MessageDigest. signature = " + signature, e);
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_GOOGLE) {
            if (data != null) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result != null && result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    if (account != null) {
                        resultLogin(account);
                    } else {
                        // account가 null인 경우 처리
                        Toast.makeText(GoogleLoginActivity.this, "계정 정보를 가져오지 못했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 로그인 실패 처리
                    Toast.makeText(GoogleLoginActivity.this, "로그인에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // data가 null인 경우 처리
                Toast.makeText(GoogleLoginActivity.this, "로그인 요청 결과가 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
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
                            intent.putExtra("profile_picture", account.getPhotoUrl().toString());
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
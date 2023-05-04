package com.example.acneapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final String TAG = "[IC]CameraActivity";
    public static final int CAMERA_IMAGE_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String KEY_SELECTED_URI = "KEY_SELECTED_URI";

    private ClassifierWithModel cls;
    private ImageView imageView;
    private TextView resultTextView;
    private TextView treatmentTextView;

    Uri selectedImageUri;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 카메라 권한이 없는 경우, 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            // 카메라 권한이 있는 경우, 카메라에서 이미지를 가져오는 기능을 호출
            getImageFromCamera();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        firestore = FirebaseFirestore.getInstance();

        Button takeBtn = findViewById(R.id.takeBtn);
        takeBtn.setOnClickListener(v -> getImageFromCamera());

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        treatmentTextView = findViewById(R.id.treatmentTextView);

        cls = new ClassifierWithModel(this);
        try {
            cls.init();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if(savedInstanceState != null) {
            Uri uri = savedInstanceState.getParcelable(KEY_SELECTED_URI);
            if (uri != null)
                selectedImageUri = uri;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 카메라에서 이미지를 가져오는 기능을 호출
                getImageFromCamera();
            } else {
                // 권한이 거부된 경우, 사용자에게 토스트 메시지를 표시
                Toast.makeText(this, "카메라 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_SELECTED_URI, selectedImageUri);
    }

    private void getImageFromCamera(){
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "picture.jpg");
        if(file.exists()) file.delete();
        selectedImageUri = FileProvider.getUriForFile(this, getPackageName(), file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK &&
                requestCode == CAMERA_IMAGE_REQUEST_CODE) {

            Bitmap bitmap = null;
            try {
                if(Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.Source src = ImageDecoder.createSource(
                            getContentResolver(), selectedImageUri);
                    bitmap = ImageDecoder.decodeBitmap(src);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
                }
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to read Image", ioe);
            }

            if(bitmap != null) {
                Pair<String, Float> output = cls.classify(bitmap);
                String resultStr = String.format(Locale.ENGLISH,
                        "여드름 종류 : %s, 확률 : %.2f%%",
                        output.first, output.second * 100);

                imageView.setImageBitmap(bitmap);
                resultTextView.setText(resultStr);

                // 이미지 파일 이름 추출
                String imageName = getImageNameFromUri(selectedImageUri);

                // 현재 시간을 문자열로 변환
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());


                // 결과를 Firestore에 저장
                Map<String, Object> classificationData = new HashMap<>();
                classificationData.put("imageName", imageName);
                classificationData.put("result", resultStr);
                classificationData.put("timestamp", timeStamp);

                firestore.collection("classifications")
                        .add(classificationData)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            }

        }
    }

    private String getImageNameFromUri(Uri uri) {
        String imageName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if(cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(columnIndex != -1) {
                        imageName = cursor.getString(columnIndex);
                    } else {
                        // 컬럼 인덱스를 찾지 못한 경우에 대한 처리
                        // 현재 시간을 기반으로 고유한 이미지 이름 생성
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        imageName = "IMG_" + timeStamp + ".jpg";
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return imageName;
    }

    @Override
    protected void onDestroy() {
        cls.finish();
        super.onDestroy();
    }
}
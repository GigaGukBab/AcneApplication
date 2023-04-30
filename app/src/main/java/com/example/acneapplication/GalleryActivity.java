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
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final String TAG = "[IC]GalleryActivity";
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private ClassifierWithModel cls;
    private ImageView imageView;
    private TextView resultTextView;

    private TextView treatmentTextView;


    private Runnable onPermissionGranted;

    private void requestReadExternalStoragePermission(Runnable onPermissionGranted) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // 권한 요청에 대한 설명을 제공해야 하는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 사용자에게 권한이 필요한 이유를 설명하는 대화상자를 표시한 후, 권한을 다시 요청하세요.
            } else {
                // 권한을 요청합니다.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            // 권한이 이미 허용되었을 경우, 콜백을 실행합니다.
            onPermissionGranted.run();
        }

        // 콜백을 저장합니다.
        this.onPermissionGranted = onPermissionGranted;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary);

        // 로그 추가
        Log.d(TAG, "onCreate()");

        firestore = FirebaseFirestore.getInstance();

        Button selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(v -> {
            Log.d(TAG, "selectBtn clicked"); // 버튼 클릭 로그 추가
            getImageFromGallery();
        });



        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        treatmentTextView = findViewById(R.id.treatmentTextView);

        cls = new ClassifierWithModel(this);
        try {
            cls.init();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되었습니다.
                    if (onPermissionGranted != null) {
                        onPermissionGranted.run();
                    }
                } else {
                    // 권한이 거부되었습니다.
                    // 앱의 기능에 필요한 경우, 사용자에게 이를 알리는 메시지를 표시하거나 기능을 비활성화하세요.
                }
                return;
            }
            // 다른 권한 요청에 대한 처리를 여기에 추가하세요.
        }
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();

                            // 이미지 처리 로직을 여기에 추가하세요.
                            processSelectedImage(selectedImageUri);
                        }
                    });


    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void processSelectedImage(Uri selectedImage) {
        Bitmap bitmap = null;

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ImageDecoder.Source src
                        = ImageDecoder.createSource(getContentResolver(), selectedImage);
                bitmap = ImageDecoder.decodeBitmap(src);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to read Image", ioe);
        }

        if (bitmap != null) {
            Pair<String, Float> output = cls.classify(bitmap);
            String resultStr = String.format(Locale.ENGLISH,
                    "여드름 종류 : %s, 확률 : %.2f%%",
                    output.first, output.second * 100);

            resultTextView.setText(resultStr);
            imageView.setImageBitmap(bitmap);

            // 분류명에 따른 처치법을 firestore에서 가져옴
            getTreatmentForAcne(output.first);

            // 이미지 파일 이름 추출
            String imageName = getImageNameFromUri(selectedImage);

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

    private void getTreatmentForAcne(String acneType) {
        firestore.collection("acne_treatments")
                .document(acneType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String treatment = document.getString("treatment");
                            treatmentTextView.append("\n\n처치법: " + treatment);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
    }



    @Override
    protected void onDestroy() {
        cls.finish();
        super.onDestroy();
    }
}
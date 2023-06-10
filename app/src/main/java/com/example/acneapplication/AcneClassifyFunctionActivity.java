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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AcneClassifyFunctionActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final String TAG = "[IC]GalleryActivity";
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    public static final int PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int CAMERA_IMAGE_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 3;
    private static final String KEY_SELECTED_URI = "KEY_SELECTED_URI";
    private ClassifierWithModel cls;
    private ImageView imageView;
    private TextView resultTextView;
    private TextView treatmentTextView;
    private Button treatBtn;
    private Runnable onPermissionGranted;
    private String classifiedAcneType;
    private Uri selectedImageUri;

    private Bitmap bitmap = null;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 카메라 권한이 없는 경우, 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            // 카메라 권한이 있는 경우, 카메라에서 이미지를 가져오는 기능을 호출
            getImageFromCamera();
        }
    }
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
            treatmentTextView.setText(""); // 처치법 출력뷰 초기화
            getImageFromGallery();
        });


        Button takeBtn = findViewById(R.id.takeBtn);

        takeBtn.setOnClickListener(v -> {
            treatmentTextView.setText(""); // 처치법 출력뷰 초기화
            getImageFromCamera();
        });


        treatBtn = findViewById(R.id.treatBtn);

        treatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (bitmap == null) {
//                    Toast.makeText(AcneClassifyFunctionActivity.this, "이미지가 없습니다. 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                Intent intent;

                // 여드름 종류에 따라 알맞는 액티비티를 선택합니다.
                switch (classifiedAcneType) {
                    case "acne_papules":
                        intent = new Intent(AcneClassifyFunctionActivity.this, AcnePapulesTreatActivity.class);
                        Log.d(TAG, "acne_papules classified"); // 레이아웃 이동 로그 추가
                        break;
                    case "acne_pustular":
                        intent = new Intent(AcneClassifyFunctionActivity.this, AcnePustularTreatActivity.class);
                        Log.d(TAG, "acne_pustular classified"); // 레이아웃 이동 로그 추가
                        break;
                    case "acne_comedonia":
                        intent = new Intent(AcneClassifyFunctionActivity.this, AcneComedoniaTreatActivity.class);
                        Log.d(TAG, "acne_comedonia classified"); // 레이아웃 이동 로그 추가
                        break;
                    // 다른 여드름 종류에 대한 처리를 여기에 추가하세요.
                    default:
                        // 알 수 없는 여드름 종류의 경우 처리하지 않습니다.
                        Toast.makeText(AcneClassifyFunctionActivity.this, "알 수 없는 여드름 종류입니다.", Toast.LENGTH_SHORT).show();
                        return;
                }

                startActivity(intent);
            }
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
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되면 카메라에서 이미지를 가져오는 기능을 호출
                    getImageFromCamera();
                } else {
                    // 권한이 거부된 경우, 사용자에게 토스트 메시지를 표시
                    Toast.makeText(this, "카메라 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
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
                classifiedAcneType = output.first;

                // 여드름 종류를 한글로 변환
                String acneTypeKorean = output.first;
                if (acneTypeKorean.contains("acne_comedonia")) {
                    acneTypeKorean = acneTypeKorean.replace("acne_comedonia", "면포성 여드름");
                } else if (acneTypeKorean.contains("acne_papules")) {
                    acneTypeKorean = acneTypeKorean.replace("acne_papules", "구진성 여드름");
                } else if (acneTypeKorean.contains("acne_pustular")) {
                    acneTypeKorean = acneTypeKorean.replace("acne_pustular", "농포성 여드름");
                }

                String resultStr = String.format(Locale.ENGLISH,
                        "여드름 종류 : %s, 확률 : %.2f%%",
                        acneTypeKorean, output.second * 100);


                resultTextView.setText(resultStr);
                imageView.setImageBitmap(bitmap);

                // 분류명에 따른 처치법을 firestore에서 가져옴
                getTreatmentForAcne(output.first+"_treatment_doc");
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
            classifiedAcneType = output.first;

            // 여드름 종류를 한글로 변환
            String acneTypeKorean = output.first;
            if (acneTypeKorean.contains("acne_comedonia")) {
                acneTypeKorean = acneTypeKorean.replace("acne_comedonia", "면포성 여드름");
            } else if (acneTypeKorean.contains("acne_papules")) {
                acneTypeKorean = acneTypeKorean.replace("acne_papules", "구진성 여드름");
            } else if (acneTypeKorean.contains("acne_pustular")) {
                acneTypeKorean = acneTypeKorean.replace("acne_pustular", "농포성 여드름");
            }

            String resultStr = String.format(Locale.ENGLISH,
                    "여드름 종류 : %s, 확률 : %.2f%%",
                    acneTypeKorean, output.second * 100);


            resultTextView.setText(resultStr);
            imageView.setImageBitmap(bitmap);

            // 분류명에 따른 처치법을 firestore에서 가져옴
            getTreatmentForAcne(output.first+"_treatment_doc");

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
                            String treatment = document.getString("short_treatment");
                            treatmentTextView.append("\n\n관리법: " + treatment);
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
package com.example.acneapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AcneComedoniaTreatActivity extends AppCompatActivity {

    // 여기에 TextView 변수를 선언
    private TextView acneTreatmentInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acne_comedonia_treat);

        acneTreatmentInfoTextView = findViewById(R.id.acneTreatmentInfoTextView);

        // Firestore 인스턴스 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore에서 여드름 관리법 정보 불러오기
        db.collection("acne_treatments").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder treatmentInfo = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            treatmentInfo.append(document.getString("여드름 종류")).append("\n")
                                    .append(document.getString("관리법")).append("\n\n")
                                    .append(document.getString("출처")).append("\n\n");
                        }
                        acneTreatmentInfoTextView.setText(treatmentInfo.toString());
                    } else {
                        Log.w("AcneTreatmentActivity", "Error getting documents.", task.getException());
                    }
                });
    }
}
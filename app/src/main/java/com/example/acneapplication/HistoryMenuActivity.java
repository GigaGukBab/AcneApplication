package com.example.acneapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HistoryMenuActivity extends AppCompatActivity {

    public static final String TAG = "[IC]HistoryMenuActivity";
    private HistoryAdapter historyAdapter; // Declare historyAdapter as a class member
    private HistoryAdapter.OnDeleteClickListener onDeleteClickListener;
    private FirebaseFirestore firestore;
    private List<Classification> classifications;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_menu);


        recyclerView = (RecyclerView) findViewById(R.id.histView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classifications = new ArrayList<>();

        // historyAdapter 초기화
        historyAdapter = new HistoryAdapter(classifications, onDeleteClickListener);

        // Set an empty adapter by default
        onDeleteClickListener = new HistoryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Classification classification) {
                // Firestore에서 문서 삭제
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("classifications")
                        .document(classification.getDocumentId()) // Firestore 문서 ID를 얻습니다.
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // 삭제 성공 시, 로컬 데이터에서 삭제하고 RecyclerView 업데이트
                                classifications.remove(classification);
                                historyAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
                                Toast.makeText(HistoryMenuActivity.this, "분류 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HistoryMenuActivity.this, "데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        };
        // historyAdapter 초기화
        historyAdapter = new HistoryAdapter(classifications, onDeleteClickListener);


        recyclerView.setAdapter(historyAdapter);

        // Initialize the Firestore instance
        firestore = FirebaseFirestore.getInstance();

        loadDataFromFirestore();

    }


    private void loadDataFromFirestore() {
        firestore.collection("classifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        classifications.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Classification classification = document.toObject(Classification.class);
                            if (classification != null) {
                                // Set the document ID to the Classification object
                                classification.setDocumentId(document.getId());
                                classifications.add(classification);
                            }
                        }
                        historyAdapter.setClassifications(classifications);
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("HistoryMenuActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

}
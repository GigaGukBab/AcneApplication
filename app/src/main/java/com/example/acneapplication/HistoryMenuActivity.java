package com.example.acneapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HistoryMenuActivity extends AppCompatActivity {

    public static final String TAG = "[IC]HistoryMenuActivity";
    private HistoryAdapter historyAdapter; // Declare historyAdapter as a class member

    private FirebaseFirestore firestore;
    private List<Classification> classifications;
    private HistoryAdapter.OnDeleteClickListener onDeleteClickListener;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_menu);


        recyclerView = (RecyclerView) findViewById(R.id.histView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classifications = new ArrayList<>();

        // Set an empty adapter by default
        historyAdapter = new HistoryAdapter(new ArrayList<>(), onDeleteClickListener);
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
                                classification.setId(document.getId());
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
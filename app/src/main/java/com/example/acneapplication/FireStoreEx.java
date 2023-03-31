package com.example.acneapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FireStoreEx extends AppCompatActivity {
    private static final String TAG = "FireStoreEx";

    private static final String KEY_DATE = "date";
    private static final String KEY_RESULT = "result";

    private EditText editTextDate;
    private EditText editTextResult;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_store_ex);

        editTextDate = findViewById(R.id.histroy_date);
        editTextResult = findViewById(R.id.histoy_result);
    }

    public void saveNote(View v) {
        String date = editTextDate.getText().toString();
        String result = editTextResult.getText().toString();

        Map<String, Object> note = new HashMap<>();
        note.put(KEY_DATE, date);
        note.put(KEY_RESULT, result);

        // db.document("ClassifyHist/User Acne Classify History");
        db.collection("ClassifyHist").document("User Acne Classify History").set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(FireStoreEx.this, "History Saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FireStoreEx.this, "History Saving Failed !", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }
}